package demo

import cats.data.Coproduct
import cats.free.{Free, FreeApplicative, Inject}
import demo.Effects.AppActionApplicative
import model.{Handle, Tweet}

object Effects {
  type AppActionApplicative[A] = FreeApplicative[SocialNetworkAction, A]

  type CP[B] = Coproduct[SocialNetworkAction, AppActionApplicative, B]
  type AppActionMonadic[A] = Free[CP, A]

  val S = implicitly[SocialNetworkActions[CP]]

  def noAction[A](a: A): AppActionMonadic[A] =
    Free.pure[CP, A](a)

  def noAction[A](p: AppActionApplicative[A]): AppActionMonadic[A] =
    Free.liftF[CP, A](Coproduct.right(p))
}

sealed trait SocialNetworkAction[A]
case class GetFollowers(handle: Handle) extends SocialNetworkAction[Vector[Handle]]
case class GetMostRecentTweet(handle: Handle) extends SocialNetworkAction[Tweet]

class SocialNetworkActions[F[_]](implicit I: Inject[SocialNetworkAction, F]) {
  def getFollowersA(handle: Handle): AppActionApplicative[Vector[Handle]] =
    FreeApplicative.lift[SocialNetworkAction, Vector[Handle]](GetFollowers(handle))

  def getFollowersM(handle: Handle): Free[F, Vector[Handle]] =
    Free.inject[SocialNetworkAction, F](GetFollowers(handle))

  def getMostRecentTweetA(handle: Handle): AppActionApplicative[Tweet] =
    FreeApplicative.lift[SocialNetworkAction, Tweet](GetMostRecentTweet(handle))

  def getMostRecentTweetM(handle: Handle): Free[F, Tweet] =
    Free.inject[SocialNetworkAction, F](GetMostRecentTweet(handle))
}

object SocialNetworkActions {
  implicit def socialNetworkActions[F[_]](implicit I: Inject[SocialNetworkAction, F]): SocialNetworkActions[F] = new SocialNetworkActions[F]()
}


