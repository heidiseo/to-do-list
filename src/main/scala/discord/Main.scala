package discord

import cats.effect.{ExitCode, IO, IOApp}
import discord.http.Route
import discord.service.DiscordServiceImpl
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val discordService = new DiscordServiceImpl[IO]
    val route = new Route[IO](discordService)

    val apis = Router(
      "v1" -> route.choreRoute
    ).orNotFound

    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
