package app

import cats.instances.vector._
import cats.syntax.traverse._
import demo.Effects.AppActionMonadic
import demo.Effects.S._
import demo.Effects.Sys._
import model.{Handle, Tweet}

import scala.concurrent.duration._

object FreeMonadExample {

  def findMostInfluentialAccount(handle1: Handle, handle2: Handle): AppActionMonadic[Handle] = {
    for {
      time <- getTime
      _ = println(s"Time is $time")
      handle1Followers <- getFollowers(handle1)
      handle2Followers <- getFollowers(handle2)
      handle1ActiveFollowers <- getUsersActiveTweetCount(handle1Followers)
      handle2ActiveFollowers <- getUsersActiveTweetCount(handle2Followers)
      mostActive = if (handle1ActiveFollowers > handle2ActiveFollowers) handle1 else handle2
    } yield mostActive
  }

  def getUsersActiveTweetCount(users: Vector[Handle]): AppActionMonadic[Int] =
    users.traverse[AppActionMonadic, Tweet] { handle =>
      getMostRecentTweet(handle)
    }.map(calculateActiveCount)


  def calculateActiveCount(followerMostRecentTweets: Vector[Tweet]): Int =
    followerMostRecentTweets.count { tweet =>
      tweet.timestamp > System.currentTimeMillis() - 7.days.toMillis
    }
}
