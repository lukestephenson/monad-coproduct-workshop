package app

import cats.Apply
import cats.instances.vector._
import cats.syntax.traverse._
import demo.Effects.S._
import demo.Effects.{AppActionApplicative, AppActionMonadic, noAction}
import model.{Handle, Tweet}

import scala.concurrent.duration._

object FreeApplicativeExample {

  def findMostInfluentialAccount(handle1: Handle, handle2: Handle): AppActionMonadic[Handle] = ???

}

