package discord.http

import cats.effect._
import cats.implicits._
import discord.model.DiscordMessage
import io.circe.generic.auto._
import org.http4s.{HttpRoutes, _}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._


object Route {

  def choreRoute[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val discordMessageDecoder: EntityDecoder[F, DiscordMessage] = jsonOf[F, DiscordMessage]

    HttpRoutes.of[F] {
      case req@POST -> Root / "chores" => {
        for {
          msg <- req.as[DiscordMessage]
          res <- Ok(s"msg $msg")
        } yield res
      }
    }
  }

  def routeNotFound[F[_]: Concurrent]: HttpApp[F] = choreRoute[F].orNotFound
}
