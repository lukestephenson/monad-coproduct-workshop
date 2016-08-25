package demo

import cats.free.Free
import cats.free.Free._
import demo.AppAction.AppActionMonadic
import model.{Handle, Tweet}

sealed trait AppAction[A] {
  def lift: AppActionMonadic[A] = liftF(this)
}

sealed trait SocialNetworkAction[A]
case class GetFollowers(handle: Handle) extends AppAction[Vector[Handle]] with SocialNetworkAction[Vector[Handle]]
case class GetMostRecentTweet(handle: Handle) extends AppAction[Tweet] with SocialNetworkAction[Tweet]

object AppAction {
  type AppActionMonadic[A] = Free[AppAction, A]
}

object SocialNetworkAction {
  def getFollowers(handle: Handle) = GetFollowers(handle).lift
  def getMostRecentTweet(handle: Handle) = GetMostRecentTweet(handle).lift
}

sealed trait ConfigAction[A]
case class GetConfig(key: String) extends AppAction[String] with ConfigAction[String]

object ConfigAction {
  def getConfig(key: String) = GetConfig(key).lift
}


