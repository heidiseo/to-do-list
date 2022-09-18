package discord.service

import cats.effect.Sync
import cats.syntax.all._
import discord.model.{DiscordDeserialisationError, DiscordError, DiscordHttpError, DiscordServiceTimeout}
import io.circe
import io.circe.Decoder
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}
import sttp.client3.circe.asJson
import sttp.client3.{BodySerializer, ConditionalResponseAs, DeserializationException, HttpError, Response, ResponseException, SttpBackend, UriContext, basicRequest, fromMetadata}
import sttp.model.StatusCode

import scala.concurrent.TimeoutException

trait ApiClient[F[_]] {
  def postWithoutRespBody[A: BodySerializer](uri: String, body: A)(implicit sttpBackend: SttpBackend[F, Any]): F[Either[DiscordError, String]]
  def postWithRespBody[A: BodySerializer, B: Decoder](uri: String, body: A)(implicit sttpBackend: SttpBackend[F, Any]): F[Either[DiscordError, B]]}

class ApiClientImp[F[_] : Sync] extends ApiClient[F] {

  private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger

  override def postWithoutRespBody[A: BodySerializer](uri: String, body: A)(implicit sttpBackend: SttpBackend[F, Any]): F[Either[DiscordError, String]] = {

    for {
      _ <- logger.info(s"Sending a request to $uri")
      resp <- basicRequest
        .post(uri"$uri")
        .contentType("application/json")
        .body(body)
        .send(sttpBackend)
        .attempt
        .map(responseHandlerWithoutRespBody)
    } yield resp
  }

  override def postWithRespBody[A: BodySerializer, B: Decoder](uri: String, body: A)(implicit sttpBackend: SttpBackend[F, Any]): F[Either[DiscordError, B]] = {

    for {
      _ <- logger.info(s"Sending a request to $uri")
      resp <- basicRequest
        .post(uri"$uri")
        .contentType("application/json")
        .body(body)
        .response(asJson[B])
        .send(sttpBackend)
        .attempt
        .map(responseHandlerWithRespBody)
    } yield resp
  }


  def responseHandlerWithoutRespBody[B](resp: Either[Throwable, Response[Either[String, String]]]): Either[DiscordError, String] = {
    resp
      .leftMap {
        case _: TimeoutException => DiscordServiceTimeout("Unable to send message - service timeout")
        case e => DiscordHttpError(s"Unable to send the message - ${e.getMessage}")
      }
      .flatMap {
        _.body.leftMap(err => DiscordHttpError(err))
      }
  }

  def responseHandlerWithRespBody[B](resp: Either[Throwable, Response[Either[ResponseException[String, circe.Error], B]]]): Either[DiscordError, B] = {
    resp
      .leftMap {
        case _: TimeoutException => DiscordServiceTimeout("Unable to send message - service timeout")
        case e => DiscordHttpError(s"Unable to send the message - ${e.getMessage}")
      }
      .flatMap {
        _.body.leftMap {
          case HttpError(body, statusCode) => DiscordHttpError(body, Option(statusCode.code))
          case DeserializationException(body, error) => DiscordDeserialisationError(error, body)
        }
      }
  }

}
