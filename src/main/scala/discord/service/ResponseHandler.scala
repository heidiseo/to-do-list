package discord.service

import cats.Applicative
import cats.effect.kernel.Sync
import discord.model.{DiscordError, DiscordServiceError, IncomingBadRequest}
import io.circe.Encoder
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats._
import org.typelevel.log4cats.slf4j._
import cats.implicits._

class ResponseHandler[F[_]: Sync] extends Http4sDsl[F] {

  private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger

  def handler[T: Encoder](resp: Either[DiscordError, T]): F[Response[F]] = {
    resp match {
      case Right(value) => logger.info("Request successful") *> Ok(value)
      case Left(err: IncomingBadRequest) => BadRequest(s"Unable to parse the request body due to ${err.getMessage}")
      case Left(err: DiscordServiceError) => ServiceUnavailable(err.getMessage)
    }
  }

}
