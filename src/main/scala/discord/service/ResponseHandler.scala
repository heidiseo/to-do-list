package discord.service

import cats.effect.kernel.Sync
import cats.syntax.all._
import discord.model.{DiscordError, DiscordServiceError, IncomingBadRequest}
import io.circe.Encoder
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

class ResponseHandler[F[_]: Sync] extends Http4sDsl[F] {

  private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger

  def handler[T: Encoder](resp: Either[DiscordError, T]): F[Response[F]] = {
    resp match {
      case Right(value) => logger.info(value.toString) *> Ok(value)
      case Left(err: IncomingBadRequest) =>
        logger.error(s"Message was not sent as the request body couldn't be parsed - ${err.getMessage}") *> BadRequest(s"Unable to parse the request body due to ${err.getMessage}")
      case Left(err: DiscordServiceError) =>
        logger.error(s"Message couldn't be sent due to ${err.getMessage}") *> ServiceUnavailable(err.getMessage)
    }
  }
}
