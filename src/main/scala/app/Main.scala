package app


import cats.free.Free
import demo.Effects.AppAction
import demo.Effects.C._
import demo.TaskInterpreter
import model.Handle

import scala.concurrent.Await
import scala.concurrent.duration._

object Main {
  implicit val scheduler = monix.execution.Scheduler.fixedPool("appThreadPool", 10)

  def main(args: Array[String]): Unit = timed {
    val program: Free[AppAction, String] = findMostInfluentialAccount()
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

  def findMostInfluentialAccount() = {
    val lukeHandle = Handle("lukestephenson")
    val composeHandle = Handle("compose")
    for {
      baseUrl <- getConfig("appUrl")
      mostActive <- FreeMonadExample.findMostInfluentialAccount(lukeHandle, composeHandle)
    } yield s"$baseUrl/details/${mostActive.handle}"
  }

}
