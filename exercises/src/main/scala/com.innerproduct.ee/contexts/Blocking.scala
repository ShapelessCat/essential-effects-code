package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._

object Blocking extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use { blocker =>
      withBlocker(blocker).as(ExitCode.Success)
    }

  def withBlocker(blocker: Blocker): IO[Unit] =
    for {
      _ <- IO("on default").debug
      _ <- blocker.blockOn(IO("on blocker").debug("inside blockOn")).debug("from blockOn")  // <2>
      _ <- IO("where am I?").debug // <3>
    } yield ()
}
