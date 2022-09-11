package discord.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import discord.model.{DiscordDeserialisationError, DiscordError, DiscordMessage, ResponseMessage}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client3.impl.cats.implicits.asyncMonadError
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{BodySerializer, DeserializationException, StringBody, UriContext}
import sttp.model.Method
import sttp.monad.MonadAsyncError

class ApiTest[F[_]] extends AnyFunSuite with Matchers {

  val sttpBackendStub: SttpBackendStub[IO, Any] = SttpBackendStub[IO, Any](implicitly[MonadAsyncError[IO]])

  implicit val discordMessageBodySerialiser: BodySerializer[DiscordMessage] = {
    DiscordMessage => StringBody(s = s"content=${DiscordMessage.content}", encoding = "UTF-8")
  }

  val discordMessage: DiscordMessage = DiscordMessage("message")

  val uri: String = "https://test.com/test"

  test("Successful response and deserialisation") {
    implicit val testingBackend: SttpBackendStub[IO, Any] =
      sttpBackendStub
        .whenRequestMatches { req =>
          req.uri shouldBe uri"$uri"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "content=message"
          true
        }
        .thenRespond(
          """{
            |"message": "Successful"
            |}""".stripMargin
        )

    val apiClient = new ApiClientImp[IO]

    val resp: Either[DiscordError, ResponseMessage] = apiClient.post[DiscordMessage, ResponseMessage](uri, discordMessage).unsafeRunSync()
    resp shouldBe Right(ResponseMessage("Successful"))
  }

  test("Successful response but failed to deserialise") {
    implicit val testingBackend: SttpBackendStub[IO, Any] =
      sttpBackendStub
        .whenRequestMatches { req =>
          req.uri shouldBe uri"$uri"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "content=message"
          true
        }
        .thenRespond(
          """{
            |"wrongField": "wrong value"
            |}""".stripMargin
        )

    val apiClient = new ApiClientImp[IO]

    val Left(resp) = apiClient.post[DiscordMessage, ResponseMessage](uri, discordMessage).unsafeRunSync()
    resp shouldBe a[DiscordDeserialisationError]
  }

  test("Response with non 2xx response") {

  }

  test("Fail to get a response") {

  }

  test("Fail with timeout error") {

  }
}
