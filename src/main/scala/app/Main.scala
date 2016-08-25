package app


import demo.Effects.{AppActionMonadic, noAction}
import demo.TaskInterpreter
import model.Handle

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Given two Twitter accounts, determine which one has the most followers
  * who have tweeted in the last week.  Then return a url in the form
  * `http://<app-url>/details/<twitter-handle>`.  Note that this endpoint
  * does not exist, but assume later we might develop one.
  */
object Main {
  implicit val scheduler = monix.execution.Scheduler.fixedPool("appThreadPool", 10)

  val lukeHandle = Handle("lukestephenson")
  val composeHandle = Handle("compose")

  def main(args: Array[String]): Unit = timed {
    val solution = if (args.length == 1) args(0) else "monad"

    val program = solution match {
      case "applicative" => FreeApplicativeExample.findMostInfluentialAccount(lukeHandle, composeHandle)
      case "monad" => FreeMonadExample.findMostInfluentialAccount(lukeHandle, composeHandle)
    }

    val task = TaskInterpreter.run(findMostInfluentialAccount(program))
    println(Await.result(task.runAsync, 1.minute))
  }

  def timed(executable: => Unit): Unit = {
    val start = System.currentTimeMillis()
    executable
    val end = System.currentTimeMillis()
    val total = end - start
    println(s"Took $total ms to complete")
  }

  def findMostInfluentialAccount(program: AppActionMonadic[Handle]): AppActionMonadic[String] = {
    for {
      baseUrl <- noAction("http://baseurl")
      mostActive <- program
    } yield s"$baseUrl/details/$mostActive"
  }

}
