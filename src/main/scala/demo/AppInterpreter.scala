package demo

import cats._
import demo.Effects.{AppActionApplicative, AppActionMonadic}
import model.{Handle, Tweet}
import monix.cats._
import monix.eval.Task

import scala.concurrent.duration._

object TaskInterpreter {
  import monix.cats._
  import monix.eval.Task.nondeterminism
  implicitly[Monad[Task]]

  val interpret = SocialNetworkActionInterpreter or AppActionApplicativeInterpreter

  def run[A](script: AppActionMonadic[A]): Task[A] = script.foldMap(interpret)
}

object AppActionApplicativeInterpreter extends (AppActionApplicative ~> Task) {
  override def apply[A](fa: AppActionApplicative[A]): Task[A] = {
    import monix.eval.Task.nondeterminism
    val taskApplicative = implicitly(Applicative[Task])
    fa.foldMap(SocialNetworkActionInterpreter)(taskApplicative)
  }
}

object SocialNetworkActionInterpreter extends (SocialNetworkAction ~> Task) {
  def apply[A](action: SocialNetworkAction[A]): Task[A] = action match {
    case GetFollowers(handle) =>
      Task.now(Vector(Handle("abc"), Handle("xyz"), Handle("123"))).delayResult(1.second)
    case GetMostRecentTweet(handle) =>
      Task.now(Tweet("hello world", System.currentTimeMillis())).delayResult(1.second)
  }
}
