package discord.http

import cats.effect._
import cats.implicits._
import discord.model.DiscordMessage
import discord.service.DiscordService
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

    HttpRoutes.of[F] {
      case req@POST -> Root / "chores" => {
        for {
          msg <- req.as[DiscordMessage]
          _ <- discordService.sendMessage
          res <- Ok(s"Message - '${msg.content}' has sent to Discord")
        } yield res
      }
    }
  }

  val routeNotFound: HttpApp[F] = choreRoute.orNotFound
}
