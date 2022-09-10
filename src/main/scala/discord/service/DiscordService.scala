package discord.service

import cats.effect.Sync
import discord.model.{DiscordError, DiscordMessage, ResponseMessage}
import io.circe.Decoder
import sttp.client3._
import sttp.client3.circe._

trait DiscordService[F[_]] {
  def sendMessage[A: Decoder](discordMessage: DiscordMessage, apiClient: ApiClient[F]): F[Either[DiscordError, ResponseMessage]]
}

class DiscordServiceImpl[F[_] : Sync](implicit sttpBackend: SttpBackend[F, Any]) extends DiscordService[F] {
  override def sendMessage[A: Decoder](discordMessage: DiscordMessage, apiClient: ApiClient[F]): F[Either[DiscordError, ResponseMessage]] = {
    val uri = "test"
    apiClient.post[DiscordMessage, ResponseMessage](uri, discordMessage)
  }
}

