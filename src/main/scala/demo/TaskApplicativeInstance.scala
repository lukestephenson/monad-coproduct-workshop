package demo

import cats.Applicative
import monix.eval.Task

object TaskApplicativeInstance {
  implicit val TaskApplicative: Applicative[Task] = new Applicative[Task] {
    override def pure[A](x: A): Task[A] = Task.now(x)

    override def ap[A, B](ff: Task[A => B])(fa: Task[A]): Task[B] = Task.mapBoth(ff, fa)((f, a) => f(a))
  }
}
