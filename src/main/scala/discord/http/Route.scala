package discord.http

import cats.data.EitherT
import cats.effect.Async
import cats.syntax.all._
import discord.model.{DiscordMessage, IncomingBadRequest}
import discord.service.{ApiClient, DiscordService}
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.circe.CirceInstances
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}


class Route[F[_] : Async](discordService: DiscordService[F], apiClient: ApiClient[F]) extends CirceInstances with Http4sDsl[F] {

  val choreRoute: HttpRoutes[F] = {

    implicit val discordMessageDecoder: EntityDecoder[F, DiscordMessage] = jsonOf[F, DiscordMessage]

    val respHandler = new RouteResponseHandler[F]

    HttpRoutes.of[F] {
      case request@POST -> Root / "chores" =>
        (for {
          req <- EitherT(request.as[DiscordMessage].attempt.map(_.leftMap(thr => IncomingBadRequest(thr.getMessage))))
          message <- EitherT(discordService.sendMessage(req, apiClient))
        } yield message).value.flatMap(respHandler.handler(_))
    }
  }
}
