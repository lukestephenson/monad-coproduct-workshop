package app

import java.lang.reflect.Executable

import cats._
import cats.`~>`
import cats.data.Coproduct
import cats.free.FreeApplicative.FA
import cats.std.future._
import cats.std.list._
import cats.std.set._
import cats.syntax.cartesian._
import cats.syntax.traverse._
import cats.free.{Free, FreeApplicative}
import cats.std.vector._
import cats.syntax.traverse._
import demo.Effects.C._
import demo.Effects.S._
import demo.Effects.{AppAction, AppActionApplicative, AppActionMonadic, noAction}
import demo.{Effects, SocialNetworkAction, TaskInterpreter}
import model.{Handle, Tweet}

import scala.concurrent.Await
import scala.concurrent.duration._

case class Followers(lukeFollowers: Vector[Handle], composeFollowers: Vector[Handle])

object Main {
  implicit val scheduler = monix.execution.Scheduler.fixedPool("appThreadPool", 10)

  val lukeHandle = Handle("lukestephenson")
  val composeHandle = Handle("compose")

  def main(args: Array[String]): Unit = {
    timed {
      val program = findMostInfluentialAccount()
      val task = TaskInterpreter.run(program)
      println(Await.result(task.runAsync, 1.minute))
    }
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
      followers <- findFollowers()
      followerMostRecentTweets <- getUserMostRecentTweet(followers.lukeFollowers ++ followers.composeFollowers)
      mostActive = calculateMostActiveAccount(followers, followerMostRecentTweets)
    } yield s"$baseUrl/details/$mostActive"
  }

  def calculateMostActiveAccount(followers: Followers, followerMostRecentTweets: Vector[(Handle, Tweet)]) = {
    val followersWithTweetInLastWeek = followerMostRecentTweets.filter { case (_, tweet) =>
      tweet.timestamp > System.currentTimeMillis() - 7.days.toMillis
    }.map(_._1)

    val firstActiveFollowerCount = followers.lukeFollowers.count(follower => followersWithTweetInLastWeek.contains(follower))
    val secondActiveFollowerCount = followers.composeFollowers.count(follower => followersWithTweetInLastWeek.contains(follower))

    val mostActive = if (firstActiveFollowerCount > secondActiveFollowerCount) lukeHandle else composeHandle
    mostActive.handle
  }


  def findFollowers(): AppActionMonadic[Followers] = {
    val lukeFollowers = getFollowersA(lukeHandle)
    val composeFpFollowers = getFollowersA(composeHandle)

    val applicativeResult: AppActionApplicative[Followers] = (lukeFollowers |@| composeFpFollowers).map(Followers)

    noAction(applicativeResult)
  }

  def getUserMostRecentTweet(users: Vector[Handle]): AppActionMonadic[Vector[(Handle, Tweet)]] = {
    val result: Vector[AppActionApplicative[(Handle, Tweet)]] = users.map { handle =>
      val followerRecentTweet: AppActionApplicative[Tweet] = getMostRecentTweetA(handle)
      followerRecentTweet.map(tweet => (handle, tweet))
    }
    noAction(result.sequence)
  }
}
