package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object DebugExample extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    seq.as(ExitCode.Success)

  val hello: IO[String] = IO("hello").debug // <1>
  val world: IO[String] = IO("world").debug // <1>

  val seq: IO[String] =
    (hello, world)
      .mapN((h, w) => s"$h $w")
      .debug // <1>
}
