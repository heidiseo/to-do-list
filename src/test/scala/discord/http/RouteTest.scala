package discord.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import discord.model.{DiscordConfig, DiscordError, DiscordHttpError, DiscordMessage, ResponseMessage}
import discord.service.{ApiClient, ApiClientImp, DiscordServiceImpl}
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.implicits.{http4sLiteralsSyntax, _}
import org.scalatest.funsuite.AnyFunSuite
import sttp.capabilities.WebSockets
import sttp.client3.impl.cats.implicits.asyncMonadError
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError

class RouteTest[F[_]] extends TestSuite {

  implicit val discordMessageEntityEncoder: EntityEncoder[IO, Json] = jsonEncoderOf[Json]

  private val discordMessage = DiscordMessage("hello")

  implicit val sttpBackendStub: SttpBackendStub[IO, WebSockets] = SttpBackendStub[IO, WebSockets](implicitly[MonadAsyncError[IO]])

  private val apiClient = new ApiClientImp[IO]

  private val config = DiscordConfig.load[IO].unsafeRunSync()

  private val discordServiceSuccessful: DiscordServiceImpl[IO] = new DiscordServiceImpl[IO](config) {
    override def sendMessage(discordMessage: DiscordMessage, apiClient: ApiClient[IO]): IO[Either[DiscordError, ResponseMessage]] =
      IO.pure(ResponseMessage("Message sent to Discord successfully").asRight[DiscordError])
  }

  private val disCordServiceUnavailable: DiscordServiceImpl[IO] = new DiscordServiceImpl[IO](config) {
    override def sendMessage(discordMessage: DiscordMessage, apiClient: ApiClient[IO]): IO[Either[DiscordError, ResponseMessage]] =
      IO.pure(DiscordHttpError("Discord Service Unavailable").asLeft[ResponseMessage])
  }

  test("successful - message is posted to Discord") {


    val route = new Route[IO](discordServiceSuccessful, apiClient)

    val response: IO[Response[IO]] = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = uri"/chores").withEntity(discordMessage.asJson)
    )

    assert(check[ResponseMessage](response, Status.Ok, Option(ResponseMessage("Message sent to Discord successfully"))))
  }

  test("fail with Bad Request - incoming message cannot be parsed") {

    val route = new Route[IO](discordServiceSuccessful, apiClient)

    val response: IO[Response[IO]] = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = uri"/chores").withEntity("bad incoming body")
    )

    assert(check[String](response, Status.BadRequest, Option("Unable to parse the request body due to Malformed message body: Invalid JSON")))
  }

  test("fail with 500 - Discord service is unavailable") {

    val route = new Route[IO](disCordServiceUnavailable, apiClient)

    val response = route.choreRoute.orNotFound.run(
      Request(method = Method.POST, uri = uri"/chores").withEntity(discordMessage.asJson)
    )

    assert(check[String](response, Status.ServiceUnavailable, Option("Discord Service Unavailable - no status code available")))
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
