package discord.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import discord.model.{DiscordError, DiscordMessage}
import discord.service.DiscordServiceImpl
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s._
import org.scalatest.funsuite.AnyFunSuite



class RouteTest[F[_]] extends TestSuite {

  test("successful - message is posted to Discord") {
    val discordService = new DiscordServiceImpl[IO] {
      override def sendMessage(discordMessage: DiscordMessage): IO[Either[DiscordError, String]] = IO.pure(Right("Message sent to Discord"))
    }

    val route = new Route[IO](discordService)

    val discordMessage = DiscordMessage("hello")

    implicit val discordMessageEntityEncoder: EntityEncoder[IO, Json] = jsonEncoderOf[Json]

    val response: IO[Response[IO]] = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = Uri.uri("/chores")).withEntity(discordMessage.asJson)
    )

    assert(check[String](response, Status.Ok, Option("Successful")))
  }

  test("fail - incoming message cannot be parsed") {

  }

  test("fail - Discord service is unavailable") {

  }

}

trait TestSuite extends AnyFunSuite {
  def check[A](
                actual: IO[Response[IO]],
                status: Status,
                expectedBody: Option[A]
              )(implicit entityDecoder: EntityDecoder[IO, A]): Boolean = {
    val actualResp = actual.unsafeRunSync()
    val statusCheck = actualResp.status == status
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty)(
      body => actualResp.as[A].unsafeRunSync() == body
    )
    statusCheck && bodyCheck
  }
}
