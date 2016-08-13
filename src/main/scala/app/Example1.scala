package app

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


//case class Followers(handle1Followers: Vector[Handle], handle2Followers: Vector[Handle])
//
//class Example1 {
//  def findMostInfluentialAccount(handle1: Handle, handle2: Handle): AppActionMonadic[Handle] = {
//    for {
//      followers <- findFollowers(handle1, handle2)
//      followerMostRecentTweets <- getUsersMostRecentTweet(followers.handle1Followers ++ followers.handle2Followers)
//      mostActive = calculateMostActiveAccount(followers, followerMostRecentTweets)
//    } yield mostActive
//  }
//
//  def findFollowers(handle1: Handle, handle2: Handle): AppActionMonadic[Followers] = {
//    val handle1Followers = getFollowersA(handle1)
//    val handle2Followers = getFollowersA(handle2)
//
//    val applicativeResult: AppActionApplicative[Followers] = (handle1Followers |@| handle2Followers).map(Followers)
//
//    noAction(applicativeResult)
//  }
//
//  def getUsersMostRecentTweet(users: Vector[Handle]): AppActionMonadic[Vector[(Handle, Tweet)]] = {
//    val result: AppActionApplicative[Vector[(Handle, Tweet)]] = users.traverse[AppActionApplicative, (Handle, Tweet)] { handle =>
//      val followerRecentTweet: AppActionApplicative[Tweet] = getMostRecentTweetA(handle)
//      followerRecentTweet.map(tweet => (handle, tweet))
//    }
//    noAction(result)
//  }
//
//  def calculateMostActiveAccount(handle1Followers: Vector[Handle], handle2Followers: Vector[Handle], followerMostRecentTweets: Vector[(Handle, Tweet)]) = {
//    val firstActiveFollowerCount = calculateActiveCount(handle1Followers, followerMostRecentTweets)
//    val secondActiveFollowerCount = calculateActiveCount(handle2Followers, followerMostRecentTweets)
//
//    val mostActive = if (firstActiveFollowerCount > secondActiveFollowerCount) lukeHandle else composeHandle
//    mostActive.handle
//  }
//
//  def calculateActiveCount(accountFollowers: Vector[Handle], followerMostRecentTweets: Vector[(Handle, Tweet)]): Int = {
//    val followersWithTweetInLastWeek = followerMostRecentTweets.filter { case (_, tweet) =>
//      tweet.timestamp > System.currentTimeMillis() - 7.days.toMillis
//    }.map(_._1)
//
//    accountFollowers.count(followersWithTweetInLastWeek.contains)
//  }
//}
