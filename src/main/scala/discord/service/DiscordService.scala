package discord.service

import cats.data.EitherT
import cats.effect.Sync
import discord.model.{DiscordError, DiscordMessage, ResponseMessage}
import sttp.client3._
import sttp.client3.circe._

trait DiscordService[F[_]] {
  def sendMessage(discordMessage: DiscordMessage, apiClient: ApiClient[F]): F[Either[DiscordError, Option[String]]]
}

class DiscordServiceImpl[F[_] : Sync](implicit sttpBackend: SttpBackend[F, Any]) extends DiscordService[F] {
  override def sendMessage(discordMessage: DiscordMessage, apiClient: ApiClient[F]): F[Either[DiscordError, ResponseMessage]] = {
    val uri = "https://discord.com/message"

//    for {
//    sdf <- EitherT(apiClient.post[DiscordMessage, Option[String]](uri, discordMessage))
//    message = Option.when(sdf.isEmpty)()
//
//    } yield
  }
}

