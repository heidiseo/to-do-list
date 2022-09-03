package discord.http

import cats.Applicative
import cats.data.EitherT
import cats.effect._
import cats.implicits._
import discord.model.{DiscordMessage, IncomingBadRequest}
import discord.service.{DiscordService, ResponseHandler}
import io.circe.generic.auto._
import org.http4s.{HttpRoutes, _}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._


class Route[F[_]: Concurrent](discordService: DiscordService[F]) {

  val choreRoute: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
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
