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

  // Failed attempt (Applicative[Monad[Applicative]])
//  def findMostInfluentialAccount(): AppActionMonadic[String] = {
//    val baseUrl: AppActionApplicative[String] = FreeApplicative.pure("http://baseurl")
//    val lukeFollowers: AppActionApplicative[Int] = getFollowersWithRecentTweets(lukeHandle)
//    val composeFpFollowers: AppActionApplicative[Int] = getFollowersWithRecentTweets(composeHandle)
//
//    val applicativeResult: AppActionApplicative[String] = (baseUrl |@| lukeFollowers |@| composeFpFollowers).map(combine)
//
//    noAction(applicativeResult)
//  }


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

    val firstActiveFollowerCount = followers.lukeFollowers.filter(follower => followersWithTweetInLastWeek.contains(follower)).size
    val secondActiveFollowerCount = followers.composeFollowers.filter(follower => followersWithTweetInLastWeek.contains(follower)).size

    val mostActive = if (firstActiveFollowerCount > secondActiveFollowerCount) lukeHandle else composeHandle
    mostActive.handle
  }


  def findFollowers(): AppActionMonadic[Followers] = {
    val lukeFollowers = getFollowersA(lukeHandle)
    val composeFpFollowers = getFollowersA(composeHandle)

    val applicativeResult: AppActionApplicative[Followers] = (lukeFollowers |@| composeFpFollowers).map(Followers)

    noAction(applicativeResult)
  }

//  def combine(lukeFollowers: Int, composeFpFollowers: Int): String = {
//    val mostActive = if (lukeFollowers > composeFpFollowers) lukeHandle else composeHandle
//    s"$baseUrl/details/${mostActive.handle}"
//  }
/*
  def findMostInfluentialAccount() = {
    val lukeHandle = Handle("lukestephenson")
    val composeHandle = Handle("compose")
    for {
      baseUrl <- getConfigM("appUrl")
      lukeFollowers <- getFollowersWithRecentTweets(lukeHandle)
      composeFpFollowers <- getFollowersWithRecentTweets(composeHandle)
      mostActive = if (lukeFollowers > composeFpFollowers) lukeHandle else composeHandle
    } yield s"$baseUrl/details/${mostActive.handle}"
  }
*/
  def getUserMostRecentTweet(users: Vector[Handle]): AppActionMonadic[Vector[(Handle, Tweet)]] = {
    val result: Vector[AppActionApplicative[(Handle, Tweet)]] = users.map { handle =>
      val followerRecentTweet: AppActionApplicative[Tweet] = getMostRecentTweetA(handle)
      followerRecentTweet.map(tweet => (handle, tweet))
//      val m: AppActionApplicative[Vector[Tweet]] = followerRecentTweet.sequence
//      m.map { followerTweets =>
//        // TODO consider the time
//        followerTweets
//          .filter(_.timestamp > System.currentTimeMillis() - 7.days.toMillis)
//          .size
//      }
    }
    noAction(result.sequence)
  }
}
