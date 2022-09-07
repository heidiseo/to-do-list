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
      .post(uri"https://discord.com/api/webhooks/934915188770091049/_84SE2gSxp67ciNY3enYdpGb6-Q1osbrMMyXLcNrb2yS_tKUeeeKtU2ObrAJYLaBeWnf")
      .contentType("application/json")
      .send(sttpBackend)

  }
}
