package demo

import cats.data.Coproduct
import cats.free.{Free, Inject}
import model.{Handle, Tweet}

sealed trait SocialNetworkAction[A]
case class GetFollowers(handle: Handle) extends SocialNetworkAction[Vector[Handle]]
case class GetMostRecentTweet(handle: Handle) extends SocialNetworkAction[Tweet]

object Effects {
  type AppAction[A] = Coproduct[SocialNetworkAction, ConfigAction, A]
  type AppActionMonadic[A] = Free[AppAction, A]

  val S = implicitly[SocialNetworkActions[AppAction]]
  val C = implicitly[ConfigActions[AppAction]]
}

class SocialNetworkActions[F[_]](implicit I: Inject[SocialNetworkAction, F]) {
  def getFollowers(handle: Handle): Free[F, Vector[Handle]] =
    Free.inject[SocialNetworkAction, F](GetFollowers(handle))

  def getMostRecentTweet(handle: Handle): Free[F, Tweet] =
    Free.inject[SocialNetworkAction, F](GetMostRecentTweet(handle))
}

object SocialNetworkActions {
  implicit def socialNetworkActions[F[_]](implicit I: Inject[SocialNetworkAction, F]): SocialNetworkActions[F] = new SocialNetworkActions[F]()
}

sealed trait ConfigAction[A]
case class GetConfig(key: String) extends ConfigAction[String]

class ConfigActions[F[_]](implicit I: Inject[ConfigAction, F]) {
  def getConfig(key: String) = Free.inject[ConfigAction, F](GetConfig(key))
}

object ConfigActions {
  implicit def configActions[F[_]](implicit I: Inject[ConfigAction, F]): ConfigActions[F] = new ConfigActions[F]()
}


