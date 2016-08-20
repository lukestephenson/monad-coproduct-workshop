package app

import cats.free.Free
import cats.free.FreeApplicative.FA
import cats.std.vector._
import cats.syntax.cartesian._
import cats.syntax.traverse._
import demo.Effects.S._
import demo.Effects.{AppActionApplicative, AppActionMonadic, CP, noAction}
import demo.{SocialNetworkAction, TaskInterpreter}
import model.{Handle, Tweet}

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
    val program = findMostInfluentialAccount()
    val task = TaskInterpreter.run(program)
    println(Await.result(task.runAsync, 1.minute))
  }

  def timed(executable: => Unit): Unit = {
    val start = System.currentTimeMillis()
    executable
    val end = System.currentTimeMillis()
    val total = end - start
    println(s"Took $total ms to complete")
  }

  def findMostInfluentialAccount(): AppActionMonadic[String] = {
    for {
      baseUrl <- noAction("http://baseurl")
      mostActive <- FreeApplicativeExample.findMostInfluentialAccount(lukeHandle, composeHandle)
    } yield s"$baseUrl/details/$mostActive"
  }

}
