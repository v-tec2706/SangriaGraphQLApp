import benchmark.Utils.saveToFile
import io.circe.Json

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main extends App {

  //  private val app: Applicateion = new Application
  //  println(StarWarsSchema.renderPretty)

  //  private val res1: Future[Json] = app.graphql(baseQuery, None, None)
  //  private val res2: Future[Json] = app.graphql(humanDeferredQuery, None, None)
  //  private val res3: Future[Json] = app.graphql(humanCachedQuery, None, None)
  //  private val res4: Future[Json] = app.graphql(humanCachedBatchedQuery, None, None)

  //  handleTimeRes(res1, 1)
  //  handleTimeRes(res2, 2)
  //  handleTimeRes(res3, 3)
  //  handleTimeRes(res4, 4)

  def handleResVerbose(res: Future[Json]) = {
    println("====================")
    println(Await.ready(res, Duration.Inf).value.get)
  }

  def handleRes(res: Future[Json], id: Int) = {
    println("====================")
    Await
      .ready(res, Duration.Inf)
      .value
      .get
      //      .map(_.hcursor.downField("extensions")
      //        .downField("tracing")
      //        .downField("duration")
      //        .focus
      //        .foreach { x => {
      //          val hits = s"DB HITS COUNTER -- [${CreatureService.counter}]"
      //          println(hits)
      //          println(x)
      //      .map(x => saveToFile(hits ++ "\n" ++ x.toString() ++ "\n", id))
      .map(x => {
        println(s"[$id] Done");
        saveToFile(x.toString(), "", "", "")
      })
  }

  def handleTimeRes(res: Future[Json], id: Int) = {
    println("====================")
    val k = Await
      .ready(res, Duration.Inf)
      .value
      .get
      .map(
        _.hcursor
          .downField("extensions")
          .downField("tracing")
          .downField("execution")
          .downField("resolvers")
          .values
          .map(_.map(x => x.toString()))
          .map(x => {
            saveToFile(x.mkString("{\n  \"resolvers\": [\n", ",", "\n  ]\n}"), "id", "", "")
            println("Done")
          })
      )

    //        .drop()
    //        .values
    //        .drop(3)
    //      )

    //        .foreach { x => {
    //          val hits = s"DB HITS COUNTER -- [${CreatureService.counter}]"
    //          println(hits)
    //          println(x)
    //            .map(x => saveToFile(hits ++ "\n" ++ x.toString() ++ "\n", id))
    //            .map(x => {
    //              println(s"[$id] Done");
    //              saveToFile(x.toString(), id)
    //            })
    //        }
    //        }
  }

  //  val t1 = OffsetDateTime.parse("2021-03-02T17:15:20.467178Z")
  //  val t2 = OffsetDateTime.parse("2021-03-02T17:15:21.117714Z")
  //  val diff =   ChronoUnit.MILLIS.between(t1, t2)
  //  println(s"diff: $diff ms")
}
