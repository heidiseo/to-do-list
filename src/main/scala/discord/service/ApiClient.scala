package discord.service

import cats.effect.Sync
import discord.model.{DiscordDeserialisationError, DiscordError, DiscordHttpError, DiscordServiceTimeout}
import io.circe
import io.circe.Decoder
import sttp.client3.circe.asJson
import sttp.client3.{BodySerializer, DeserializationException, HttpError, Response, ResponseException, SttpBackend, UriContext, basicRequest}
import cats.syntax.all._

import scala.concurrent.TimeoutException

trait ApiClient[F[_]] {
  def post[A: BodySerializer, B: Decoder](uri: String, body: A)(implicit sttpBackend: SttpBackend[F, Any]): F[Either[DiscordError, B]]
}

class ApiClientImp[F[_] : Sync] extends ApiClient[F] {
  override def post[A: BodySerializer, B: Decoder](uri: String, body: A)(implicit sttpBackend: SttpBackend[F, Any]): F[Either[DiscordError, B]] = {

    val response: F[Either[DiscordError, B]] = basicRequest
      .post(uri"$uri")
      .contentType("application/json")
      .body(body)
      .response(asJson[B])
      .send(sttpBackend)
      .attempt
      .map(responseHandler)

    response
  }

  def responseHandler[B](resp: Either[Throwable, Response[Either[ResponseException[String, circe.Error], B]]]): Either[DiscordError, B] = {
    resp
      .leftMap {
        case e: TimeoutException => DiscordServiceTimeout(s"Unable to send message - $e")
        case e => DiscordHttpError(s"Unable to send message - $e", None)
      }
      .flatMap {
        _.body.leftMap {
          case HttpError(body, statusCode) => DiscordHttpError(body, Option(statusCode.code))
          case DeserializationException(body, error) => DiscordDeserialisationError(error, body)
        }
      }
  }
}
