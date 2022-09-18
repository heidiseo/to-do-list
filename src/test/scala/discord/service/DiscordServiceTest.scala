package discord.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import discord.model.{DiscordHttpError, DiscordMessage, DiscordServiceError, ResponseMessage}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client3.{StringBody, UriContext}
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError
import sttp.client3.impl.cats.implicits.asyncMonadError
import sttp.model.{Method, StatusCode}


class DiscordServiceTest extends AnyFunSuite with Matchers {

  private val discordMessage = DiscordMessage("hello")

  private val sttpBackendBase: SttpBackendStub[IO, Any] = SttpBackendStub[IO, Any](implicitly[MonadAsyncError[IO]])

  test("Successful - message to Discord was successful") {
    implicit val sttpBackendTest: SttpBackendStub[IO, Any] =
      sttpBackendBase
        .whenRequestMatches { req =>
          req.uri shouldBe uri"https://discord.com/message"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "{\"content\":\"hello\"}"
          true
        }
        .thenRespondWithCode(StatusCode.NoContent)

    val apiClient = new ApiClientImp[IO]
    val discordService = new DiscordServiceImpl[IO]
    val resp = discordService.sendMessage(discordMessage, apiClient).unsafeRunSync()
    resp shouldBe ResponseMessage("Message successfully delivered.").asRight
  }

  test("Fail - message to Discord failed") {
    implicit val sttpBackendTest: SttpBackendStub[IO, Any] =
      sttpBackendBase
        .whenRequestMatches { req =>
          req.uri shouldBe uri"https://discord.com/message"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "{\"content\":\"hello\"}"
          true
        }
        .thenRespondServerError()

    val apiClient = new ApiClientImp[IO]
    val discordService = new DiscordServiceImpl[IO]
    val Left(resp) = discordService.sendMessage(discordMessage, apiClient).unsafeRunSync()
    resp shouldBe a[DiscordHttpError]

  }
}
