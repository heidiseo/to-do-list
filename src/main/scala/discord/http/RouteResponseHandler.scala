package discord.http

import cats.effect.Sync
import cats.syntax.all._
import discord.model.{DiscordError, DiscordHttpError, DiscordServiceError, IncomingBadRequest}
import io.circe.Encoder
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

class RouteResponseHandler[F[_]: Sync] extends Http4sDsl[F] {

  private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger

  def handler[T: Encoder](resp: Either[DiscordError, T]): F[Response[F]] = {
    resp match {
      case Right(value) => logger.info("") *> Ok(value)
      case Left(err: IncomingBadRequest) =>
        logger.error(s"Message was not sent as the request body couldn't be parsed - ${err.getMessage}") *> BadRequest(s"Unable to parse the request body due to ${err.getMessage}")
      case Left(err: DiscordHttpError) =>
        logger.error(s"Message couldn't be sent due to ${err.getMessage}") *> ServiceUnavailable(err.getMessage)
    }
  }
}
