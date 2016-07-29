package app

import cats.std.vector._
import cats.syntax.traverse._
import demo.AppAction.AppActionMonadic
import demo.ConfigAction._
import demo.SocialNetworkAction._
import demo.{ConfigActionInterpreter, SocialNetworkActionInterpreter, TaskInterpreter}
import model.{Handle, Tweet}
import monix.reactive.MulticastStrategy.Async

import scala.concurrent.Await
import scala.concurrent.duration._

object Main {
  implicit val scheduler = monix.execution.Scheduler.fixedPool("appThreadPool", 10)

  def main(args: Array[String]): Unit = timed {
    val program = findMostInfluentialAccount()
    val interpreter = new TaskInterpreter(new SocialNetworkActionInterpreter(), new ConfigActionInterpreter())
    val task = interpreter.run(program)
    println(Await.result(task.runAsync, 1 minute))
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
