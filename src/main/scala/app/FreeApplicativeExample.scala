package app

import cats.Apply
import cats.std.vector._
import cats.syntax.traverse._
import demo.Effects.S._
import demo.Effects.{AppActionApplicative, AppActionMonadic, noAction}
import model.{Handle, Tweet}

import scala.concurrent.duration._

object FreeApplicativeExample {

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

    val applicativeResult = Apply[AppActionApplicative]
      .map2(handle1ActiveFollowers, handle2ActiveFollowers)(determineMostActive)

    // equivalent (but intellij reports as an error)
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

