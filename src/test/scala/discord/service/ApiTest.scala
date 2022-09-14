package discord.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import discord.model._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client3.impl.cats.implicits.asyncMonadError
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{BodySerializer, Response, StringBody, SttpClientException, UriContext, basicRequest}
import sttp.model.Method
import sttp.model.StatusCode.NoContent
import sttp.monad.MonadAsyncError

import scala.concurrent.TimeoutException

class ApiTest[F[_]] extends AnyFunSuite with Matchers {

  val sttpBackendStub: SttpBackendStub[IO, Any] = SttpBackendStub[IO, Any](implicitly[MonadAsyncError[IO]])

  implicit val discordMessageBodySerialiser: BodySerializer[DiscordMessage] = {
    DiscordMessage => StringBody(s = s"content=${DiscordMessage.content}", encoding = "UTF-8")
  }

  val discordMessage: DiscordMessage = DiscordMessage("message")

  val uri: String = "https://test.com/test"

  test("Successful response with body and deserialisation") {
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

  test("Successful response with no content") {
    implicit val testingBackend: SttpBackendStub[IO, Any] =
      sttpBackendStub
        .whenRequestMatches { req =>
          req.uri shouldBe uri"$uri"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "content=message"
          true
        }
        .thenRespond(Response(None.asRight, NoContent))

    val apiClient = new ApiClientImp[IO]

    val resp: Either[DiscordError, Option[String]] = apiClient.post[DiscordMessage, Option[String]](uri, discordMessage).unsafeRunSync()
    resp shouldBe Right(None)
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
    implicit val testingBackend: SttpBackendStub[IO, Any] =
      sttpBackendStub
        .whenRequestMatches { req =>
          req.uri shouldBe uri"$uri"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "content=message"
          true
        }
        .thenRespondServerError()

    val apiClient = new ApiClientImp[IO]

    val Left(resp) = apiClient.post[DiscordMessage, ResponseMessage](uri, discordMessage).unsafeRunSync()
    resp shouldBe a[DiscordHttpError]
    resp.getMessage shouldBe "Internal server error - 500"
  }

  test("Fail to get a response by sttp backend") {
    implicit val testingBackend: SttpBackendStub[IO, Any] =
      sttpBackendStub
        .whenRequestMatches { _ => true }
        .thenRespond(
          throw new SttpClientException.ReadException(
            basicRequest.post(uri"$uri"), new RuntimeException
          )
        )

    val apiClient = new ApiClientImp[IO]
    val Left(resp) = apiClient.post[DiscordMessage, ResponseMessage](uri, discordMessage).unsafeRunSync()
    resp shouldBe a[DiscordHttpError]
    resp.getMessage shouldBe "Unable to send the message - Exception when sending request: POST https://test.com/test - no status code available"
  }

  test("Fail with timeout error") {
    implicit val testingBackend: SttpBackendStub[IO, Any] = {
      sttpBackendStub
        .whenRequestMatches { req =>
          req.uri shouldBe uri"$uri"
          req.method shouldBe Method.POST
          req.body.asInstanceOf[StringBody].s shouldBe "content=message"
          true
        }
        .thenRespond(
          throw new TimeoutException
        )
    }

    val apiClient = new ApiClientImp[IO]
    val Left(resp) = apiClient.post[DiscordMessage, ResponseMessage](uri, discordMessage).unsafeRunSync()
    resp shouldBe a[DiscordServiceTimeout]
    resp.getMessage shouldBe "Unable to send message - service timeout"
  }
}
