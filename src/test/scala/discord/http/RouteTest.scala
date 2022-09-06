package discord.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import discord.model.{DiscordError, DiscordMessage, DiscordServiceError}
import discord.service.DiscordServiceImpl
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.implicits.{http4sLiteralsSyntax, _}
import org.scalatest.funsuite.AnyFunSuite

class RouteTest[F[_]] extends TestSuite {

  implicit val discordMessageEntityEncoder: EntityEncoder[IO, Json] = jsonEncoderOf[Json]

  private val discordMessage = DiscordMessage("hello")

  private val discordServiceSuccessful = new DiscordServiceImpl[IO] {
    override def sendMessage(discordMessage: DiscordMessage): IO[Either[DiscordError, String]] = IO.pure(Right("Message sent to Discord successfully"))
  }

  private val disCordServiceUnavailable = new DiscordServiceImpl[IO] {
    override def sendMessage(discordMessage: DiscordMessage): IO[Either[DiscordError, String]] = IO.pure(Left(DiscordServiceError("Discord Service Unavailable")))
  }

  test("successful - message is posted to Discord") {


    val route = new Route[IO](discordServiceSuccessful)

    val response: IO[Response[IO]] = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = uri"/chores").withEntity(discordMessage.asJson)
    )

    assert(check[String](response, Status.Ok, Option("Message sent to Discord successfully")))
  }

  test("fail with Bad Request - incoming message cannot be parsed") {

    val route = new Route[IO](discordServiceSuccessful)

    val response: IO[Response[IO]] = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = uri"/chores").withEntity("bad incoming body")
    )

    assert(check[String](response, Status.BadRequest, Option("Unable to parse the request body due to Malformed message body: Invalid JSON")))
  }

  test("fail with 500 - Discord service is unavailable") {

    val route = new Route[IO](disCordServiceUnavailable)

    val response = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = uri"/chores").withEntity(discordMessage.asJson)
    )

    assert(check[String](response, Status.ServiceUnavailable, Option("Discord Service Unavailable")))
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
