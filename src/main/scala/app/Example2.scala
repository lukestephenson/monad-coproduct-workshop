package app

import cats.Apply
import demo.Effects._
import model.{Handle, Tweet}
import demo.Effects._
import model.Handle
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

object Example2 {
  def findMostInfluentialAccount(handle1: Handle, handle2: Handle): AppActionMonadic[Handle] = {
    val handle1ActiveFollowers = getFollowersA(handle1).map(getUsersActiveTweetCount)
    val handle2ActiveFollowers = getFollowersA(handle2).map(getUsersActiveTweetCount)

    def determineMostActive(handle1Script: AppActionMonadic[Int], handle2Script: AppActionMonadic[Int]): AppActionMonadic[Handle] = {
      for {
        handle1Active <- handle1Script
        handle2Active <- handle2Script
        mostActive = if (handle1Active > handle2Active) handle1 else handle2
      } yield mostActive
    }

    val applicativeResult = Apply[AppActionApplicative].map2(handle1ActiveFollowers, handle2ActiveFollowers)(determineMostActive)

    // equivalent (but intellij reports as an error
//    val applicativeResult: AppActionApplicative[AppActionMonadic[Handle]] =
//      (handle1ActiveFollowers |@| handle2ActiveFollowers).map(determineMostActive)

    noAction(applicativeResult).flatMap(m => m)
  }

  def getUsersActiveTweetCount(users: Vector[Handle]): AppActionMonadic[Int] = {
    val result: AppActionApplicative[Int] = users.traverse[AppActionApplicative, Tweet] { handle =>
      getMostRecentTweetA(handle)
    }.map(calculateActiveCount)

    noAction(result)
  }

  def calculateActiveCount(followerMostRecentTweets: Vector[Tweet]): Int = {
    followerMostRecentTweets.count { tweet =>
      tweet.timestamp > System.currentTimeMillis() - 7.days.toMillis
    }
  }
}

//def findMostInfluentialAccount(handle1: Handle, handle2: Handle): AppActionMonadic[Handle] = {
//  val handle1Followers = getFollowersA(handle1).map(getUsersMostRecentTweet)
//  val handle2Followers = getFollowersA(handle2).map(getUsersMostRecentTweet)
//
//  val applicativeResult: AppActionApplicative[AppActionMonadic[Handle]] =
//  (handle1Followers |@| handle2Followers).map(foo(handle1, handle2))
//
//  noAction(applicativeResult).flatMap(m => m)
//}
//
//  def foo(handle1: Handle, handle2: Handle)(handle1Script: AppActionMonadic[Vector[Tweet]], handle2Script: AppActionMonadic[Vector[Tweet]]): AppActionMonadic[Handle] = {
//  for {
//  handle1RecentTweets <- handle1Script
//  handle2RecentTweets <- handle2Script
//  handle1Active = calculateActiveCount(handle1RecentTweets)
//  handle2Active = calculateActiveCount(handle2RecentTweets)
//  mostActive = if (handle1Active > handle2Active) handle1 else handle2
//} yield mostActive
//}
//
//  def getUsersMostRecentTweet(users: Vector[Handle]): AppActionMonadic[Vector[Tweet]] = {
//  val result: AppActionApplicative[Vector[Tweet]] = users.traverse[AppActionApplicative, Tweet] { handle =>
//  getMostRecentTweetA(handle)
//}
//  noAction(result)
//}
//
//  def calculateActiveCount(followerMostRecentTweets: Vector[Tweet]): Int = {
//  followerMostRecentTweets.count { tweet =>
//  tweet.timestamp > System.currentTimeMillis() - 7.days.toMillis
//}
//}
