package demo

import cats._
import cats.data.Coproduct
import demo.Effects.{AppAction, AppActionApplicative, AppActionMonadic}
import model.{Handle, Tweet}
import monix.cats._
import monix.eval.Task
import scala.concurrent.duration._

object TaskInterpreter {
  implicitly[Monad[Task]]

  val interpret = SocialNetworkActionInterpreter or AppActionApplicativeInterpreter

  def run[A](script: AppActionMonadic[A]): Task[A] = script.foldMap(interpret)
}

object AppActionApplicativeInterpreter extends (AppActionApplicative ~> Task) {
  override def apply[A](fa: AppActionApplicative[A]): Task[A] = {
    fa.foldMap(SocialNetworkActionInterpreter)(TaskApplicativeInstance.TaskApplicative)
  }
}

object SocialNetworkActionInterpreter extends (SocialNetworkAction ~> Task) {
  def apply[A](action: SocialNetworkAction[A]): Task[A] = action match {
    case GetFollowers(handle) => Task.now(Vector(Handle("abc"), Handle("xyz"), Handle("123"))).delayResult(1.second)
    case GetMostRecentTweet(handle) => Task.now(Tweet("hello world", System.currentTimeMillis())).delayResult(1.second)
  }
}

object ConfigActionInterpreter extends (ConfigAction ~> Task) {
  def apply[A](action: ConfigAction[A]): Task[A] = action match {
    case GetConfig(_) => Task.now("http://localhost:9999/")
  }
}