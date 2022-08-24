package discord

import cats.effect.{ExitCode, IO, IOApp}
import discord.http.Route
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val apis = Router(
      "v1" -> Route.choreRoute[IO]
    ).orNotFound

    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
