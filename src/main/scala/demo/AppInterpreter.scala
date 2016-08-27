package demo

import cats._
import demo.AppAction.AppActionMonadic
import model.{Handle, Tweet}
import monix.cats._
import monix.eval.Task

import scala.concurrent.duration._


class TaskInterpreter(socialNetworkActionInterpreter: SocialNetworkActionInterpreter,
                      configActionInterpreter: ConfigActionInterpreter)
  extends (AppAction ~> Task)  {

  implicitly[Monad[Task]]

  def run[A](script: AppActionMonadic[A]): Task[A] = script.foldMap(this)

  override def apply[A](action: AppAction[A]): Task[A] = action match {
    case action@GetFollowers(_) => socialNetworkActionInterpreter.apply(action)
    case action@GetMostRecentTweet(_) => socialNetworkActionInterpreter.apply(action)
    case action@GetConfig(_) => configActionInterpreter.apply(action)
  }
}

class SocialNetworkActionInterpreter {
  def apply[A](action: SocialNetworkAction[A]): Task[A] = action match {
    case GetFollowers(handle) => Task.now(Vector(Handle("abc"), Handle("xyz"), Handle("123"))).delayResult(1.second)
    case GetMostRecentTweet(handle) => Task.now(Tweet("hello world", System.currentTimeMillis())).delayResult(1.second)
  }
}

class ConfigActionInterpreter {
  def apply[A](action: ConfigAction[A]): Task[A] = action match {
    case GetConfig(_) => Task.now("http://localhost:9999/")
  }
}
