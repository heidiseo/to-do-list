package discord.http

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import discord.model.{DiscordMessage, IncomingBadRequest}
import discord.service.{DiscordService, ResponseHandler}
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.circe.CirceInstances
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl


class Route[F[_]: Sync](discordService: DiscordService[F]) extends CirceInstances with Http4sDsl[F] {

  val choreRoute: HttpRoutes[F] = {

    implicit val discordMessageDecoder: EntityDecoder[F, DiscordMessage] = jsonOf[F, DiscordMessage]

    val respHandler = new ResponseHandler[F]

    HttpRoutes.of[F] {
      case request@POST -> Root / "chores" =>
        (for {
          req <- EitherT(request.as[DiscordMessage].attempt.map(_.leftMap(thr => IncomingBadRequest(thr.getMessage))))
          message <- EitherT(discordService.sendMessage(req))
        } yield message).value.flatMap(respHandler.handler(_))
    }
  }
}
