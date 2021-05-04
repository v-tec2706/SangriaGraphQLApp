package benchmark

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import benchmark.BenchmarkQueries.Strategies
import io.circe.Json
import io.circe.parser._
import io.circe.syntax.EncoderOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

case class HttpClient() {
  implicit val system = ActorSystem()

  def send(uri: String)(data: String): Future[Json] = {
    Http()
      .singleRequest(HttpRequest(method = HttpMethods.POST, uri = uri, entity = HttpEntity(ContentTypes.`application/json`, data)))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String.asJson)
        case resp@HttpResponse(code, _, _, _) =>
          println("Request failed, response code: " + code)
          resp.discardEntityBytes()
          Future {
            Map("error code" -> code.intValue()).asJson
          }
      }
  }
}

object HttpClient extends App {

  private lazy val httpClient = HttpClient()
  val res = sendGraphQLRequest(port = "8081", data = benchmark.BenchmarkQueries.q1(Strategies.Batched).body)
    .map(response => response.asString.map(parse).flatMap(_.toOption).map(handleResponse(_, "q1", "async")))

  def sendGraphQLRequest(host: String = "localhost", port: String, data: String): Future[Json] = {
    println(s"Sending data to $host:$port \n data: \n $data")
    val clear = data.replaceAll("\n", "").replaceAll("  ", " ")
    httpClient
      .send(s"http://${host}:$port/graphql")(s"""{"query": "$clear"}""")
      .map(s => {
        println(s"Response is: \n ${s.toString()}");
        s
      })
  }

  def handleResponse(response: Json, queryName: String, strategy: String): String = {
    val isError = response.hcursor.downField("errors").focus.isDefined
    val tracing = response.hcursor.downField("extensions").downField("tracing").focus
    val totalTime: String =
      tracing.flatMap(_.hcursor.downField("duration").focus).map(_.toString()).getOrElse("Failed to fetch time")
    val allFields = tracing
      .flatMap(
        _.hcursor
          .downField("execution")
          .downField("resolvers")
          .values
          .map(_.map(x => (x.hcursor.downField("path").focus, x.hcursor.downField("duration").focus)))
      )
      .map(l =>
        l.map(r =>
          (
            r._1.flatMap(_.hcursor.values).map(_.map(_.asString).flatten.mkString(",")).getOrElse(""),
            r._2.map(_.toString()).getOrElse("")
          )
        )
      )
      .getOrElse(List.empty)

    if (!isError) {
      s"total time: $totalTime \n" +
        allFields.toList
          .sortWith(_._2.toLong > _._2.toLong)
          .map { case (s1, s2) => s"$s1: $s2" }
          .mkString("\n") + "\n"
    } else "Execution failed"
  }

  Await.result(res, 5.seconds)
}
