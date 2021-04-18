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
  val res = sendGraphQLRequest("8081", benchmark.BenchmarkQueries.q1(Strategies.Async))
    .map(response => handleResponse(response))

  def sendGraphQLRequest(port: String, data: String): Future[Json] = {
    val clear = data.replaceAll("\n", "").replaceAll("  ", " ")
    httpClient.send(s"http://localhost:$port/graphql")(s"""{"query": "$clear"}""")
  }

  private def handleResponse(response: Json): Unit = {
    val resJson = response.asString.map(parse)
    val isError = resJson.map(x => x.map(_.hcursor.downField("errors").focus)).flatMap(_.toOption.flatten).isDefined
    val tracing = resJson.map(_.map(_.hcursor.downField("extensions").downField("tracing").focus)).flatMap(_.toOption)
    val totalTime = tracing.flatMap(_.map(_.hcursor.downField("duration").focus)).flatten
    val allFields = tracing
      .flatMap(
        _.map(
          _.hcursor
            .downField("execution")
            .downField("resolvers")
            .values
            .map(_.map(x => (x.hcursor.downField("path").focus, x.hcursor.downField("duration").focus)))
        )
      )
      .flatten
      .map(l =>
        l.map(r =>
          (
            r._1.flatMap(_.hcursor.values).map(_.map(_.asString).flatten.mkString("->")).getOrElse(""),
            r._2.map(_.toString()).getOrElse("")
          )
        )
      )
      .getOrElse(List.empty)

    if (!isError) {
      totalTime.foreach(t => println(s"Total time: $t"))
      allFields.toList.sortWith(_._2.toLong > _._2.toLong).take(10).foreach(x => println(s"${x._1} ${" " * (60 - x._1.length)}${x._2}"))
    } else println(s"Execution failed")
  }

  Await.result(res, 5.seconds)
}
