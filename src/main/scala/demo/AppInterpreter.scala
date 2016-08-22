package demo

import cats._
import demo.Effects.{AppAction, AppActionMonadic}
import model.{Handle, Tweet}
import monix.cats._
import monix.eval.Task

import scala.concurrent.duration._

abstract class AppInterpreter[F[_] : Monad] {
  def interpret: (AppAction ~> F)

  def run[A](script: AppActionMonadic[A]): F[A] = script.foldMap(interpret)
}

object TaskInterpreter extends AppInterpreter[Task] {
  implicitly[Monad[Task]]

  val interpret: AppAction ~> Task = SocialNetworkActionInterpreter or ConfigActionInterpreter
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
