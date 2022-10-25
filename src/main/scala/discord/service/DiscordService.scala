package discord.service

import cats.effect.Sync
import cats.syntax.all._
import discord.model.{DiscordConfig, DiscordError, DiscordMessage, ResponseMessage}
import sttp.client3._
import sttp.client3.circe._

trait DiscordService[F[_]] {
  def sendMessage(discordMessage: DiscordMessage, apiClient: ApiClient[F]): F[Either[DiscordError, ResponseMessage]]
}

class DiscordServiceImpl[F[_] : Sync](discordConfig: DiscordConfig)(implicit sttpBackend: SttpBackend[F, Any]) extends DiscordService[F] {

  override def sendMessage(discordMessage: DiscordMessage, apiClient: ApiClient[F]): F[Either[DiscordError, ResponseMessage]] = {
    val discordUrl = discordConfig.webhookUrl
    for {
    resp <- apiClient.postWithoutRespBody[DiscordMessage](discordUrl, discordMessage)
    transformed = resp.map(_ => ResponseMessage("Message successfully delivered."))
    } yield transformed
  }
}

