package discord.service

import cats.effect.Sync
import discord.model.{DiscordError, DiscordMessage}
import sttp.client3._

trait DiscordService[F[_]] {
  def sendMessage(discordMessage: DiscordMessage): F[Either[DiscordError, String]]
}

class DiscordServiceImpl[F[_] : Sync](implicit sttpBackend: SttpBackend[F, Any]) extends DiscordService[F] {
  override def sendMessage(discordMessage: DiscordMessage): F[Either[DiscordError, String]] = {

    val response: F[Response[Either[String, String]]] = basicRequest
      .post(uri"test")
      .contentType("application/json")
      .send(sttpBackend)

  }
}
