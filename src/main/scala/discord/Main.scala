package discord

import cats.effect.{ExitCode, IO, IOApp}
import discord.http.Route
import discord.service.{ApiClient, ApiClientImp, DiscordServiceImpl}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    AsyncHttpClientCatsBackend.resource[IO]().use {
      implicit backend => {
        val discordService = new DiscordServiceImpl[IO]
        val apiClient = new ApiClientImp[IO]
        val route = new Route[IO](discordService, apiClient)

        val apis = Router(
          "v1" -> route.choreRoute
        ).orNotFound

        BlazeServerBuilder[IO]
          .bindHttp(8080, "localhost")
          .withHttpApp(apis)
          .resource
          .use(_ => IO.never)
          .as(ExitCode.Success)
      }
    }
  }
}
