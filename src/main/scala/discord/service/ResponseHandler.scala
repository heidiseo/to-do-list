package discord.service

import cats.Applicative
import discord.model.{DiscordError, IncomingBadRequest}
import io.circe.Encoder
import io.circe.Encoder.AsArray.importedAsArrayEncoder
import io.circe.Encoder.AsObject.importedAsObjectEncoder
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

class ResponseHandler[F[_]: Applicative] extends Http4sDsl[F] {

  def handler[T: Encoder](resp: Either[DiscordError, T]): F[Response[F]] = {
    resp match {
      case Right(value) => Ok(value.asJson.deepDropNullValues)
      case Left(err: IncomingBadRequest) => BadRequest(s"Unable to parse - $err")

    }

  }

}
