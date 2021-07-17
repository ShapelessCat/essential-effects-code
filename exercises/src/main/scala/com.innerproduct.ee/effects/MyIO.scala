package com.innerproduct.ee.effects

case class MyIO[A](unsafeRun: () => A) {  // <1>
  def map[B](f: A => B): MyIO[B] =
    MyIO(() => f(unsafeRun()))

  def flatMap[B](f: A => MyIO[B]): MyIO[B] =
    MyIO(() => f(unsafeRun()).unsafeRun())
}

object MyIO {
  def putStr(s: => String): MyIO[Unit] =  // <2>
    MyIO(() => println(s))
}

object Printing extends App { // <3>
  val hello = MyIO.putStr("hello!")

  hello.unsafeRun()
}