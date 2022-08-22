package discord

import cats.effect.{IO, IOApp}
import discord.http.HelloWorld

object Main extends IOApp.Simple {

  // This is your new "main"!
  def run: IO[Unit] =
    HelloWorld.say().flatMap(IO.println)
}
