package com.innerproduct.ee.petstore

import cats.effect._
import com.innerproduct.ee.debug._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.http4sLiteralsSyntax

import scala.concurrent.ExecutionContext

object Client extends IOApp {
  private val scruffles = Pet("Scruffles", "dog")

  def run(args: List[String]): IO[ExitCode] =
    pets.use { pets =>
      for {
        id <- pets.give(scruffles)
        pet <- pets.find(id)
        _ <- IO(pet.contains(scruffles)).debug
      } yield ExitCode.Success
    }

  def pets: Resource[IO, PetService[IO]] =
    for {
      client <- BlazeClientBuilder[IO](ExecutionContext.global).resource
    } yield ClientResources.pets(client, uri"http://localhost:8080")
}
