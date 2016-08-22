package app

import cats.free.Free
import cats.std.vector._
import cats.syntax.traverse._
import demo.Effects.C._
import demo.Effects.S._
import demo.Effects.{AppAction, AppActionMonadic}
import demo.TaskInterpreter
import model.{Handle, Tweet}

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
      lukeFollowers <- getFollowersWithRecentTweets(lukeHandle)
      composeFpFollowers <- getFollowersWithRecentTweets(composeHandle)
      mostActive = if (lukeFollowers > composeFpFollowers) lukeHandle else composeHandle
    } yield s"$baseUrl/details/${mostActive.handle}"
  }

  def getFollowersWithRecentTweets(handle: Handle): AppActionMonadic[Int] = {
    val followers: AppActionMonadic[Vector[Handle]] = getFollowers(handle)

    followers.flatMap { handles =>
      val followerRecentTweet: Vector[AppActionMonadic[Tweet]] = handles.map(getMostRecentTweet)
      val m: AppActionMonadic[Vector[Tweet]] = followerRecentTweet.sequence
      m.map { followerTweets =>
        // TODO consider the time
        followerTweets
          .filter(_.timestamp > System.currentTimeMillis() - 7.days.toMillis)
          .size
      }
    }
  }
}
