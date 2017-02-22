package demo

import cats._
import monix.eval.Task


object SystemActionInterpreter extends (SystemAction ~> Task) {
  def apply[A](action: SystemAction[A]): Task[A] = action match {
    case GetTime => Task.now(100L)
  }
}
