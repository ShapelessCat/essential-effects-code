package com.innerproduct.ee.petstore

import cats._
import cats.effect._
import cats.implicits._
import cats.laws._
import munit.ScalaCheckSuite
import org.scalacheck._

import scala.language.implicitConversions

class PetServiceTests extends ScalaCheckSuite with IOLawsForMunitScalaCheck {
  ServerResources
    .pets[IO]
    .use { ps =>
      findAfterGive(ps) *>
        giveIdempotent(ps)
    }
    .unsafeRunSync()

  def findAfterGive(ps: PetService[IO]): IO[Unit] = IO {
    property("findAfterGive") {
      Prop.forAll(genPet) { (pet: Pet) =>
        PetServiceLaws.findAfterGive(ps, pet)
      }
    }
  }

  def giveIdempotent(ps: PetService[IO]): IO[Unit] = IO {
    property("giveIdempotent") {
      Prop.forAll(genPet) { (pet: Pet) =>
        PetServiceLaws.giveIdempotent(ps, pet)
      }
    }
  }

  val genPet: Gen[Pet] =
    for {
      name <- Gen.alphaStr
      category <- Gen.oneOf("cat", "dog", "newt")
    } yield Pet(name, category)
}

object PetServiceLaws {
  def findAfterGive[F[_]: Monad](ps: PetService[F], pet: Pet): IsEq[F[Option[Pet]]] =
    (ps.give(pet) >>= ps.find) <-> (ps.give(pet) >> pet.some.pure[F])

  def giveIdempotent[F[_]: Monad](ps: PetService[F], pet: Pet): IsEq[F[Pet.Id]] =
    (ps.give(pet) >> ps.give(pet)) <-> ps.give(pet)
  // WRONG: doesn't detect not returning same id for same pet
  // (ps.give(pet) >> ps.give(pet) >>= ps.find) <-> (ps.give(pet) >>= ps.find)
}

/** "Laws" in cats are usually expressed with the [[cats.laws.IsEq]] class,
  * which can be written with the infix operator `<->` to mean "these two things are equal".
  *
  * This mixin lets you use `<->` to equate two `IO` values.
  */
trait IOLawsForMunitScalaCheck { self: ScalaCheckSuite =>
  implicit def ioIsEq[A](isEq: IsEq[IO[A]]): Prop =
    isEq match {
      case IsEq(lhs, rhs) => assertEquals(lhs.unsafeRunSync(), rhs.unsafeRunSync())
    }
}
