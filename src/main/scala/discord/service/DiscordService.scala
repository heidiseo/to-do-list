package discord.service

import discord.model.{DiscordError, DiscordMessage}

trait DiscordService[F[_]] {
  def sendMessage(discordMessage: DiscordMessage): F[Either[DiscordError, String]]
}

class DiscordServiceImpl[F[_]] extends DiscordService[F] {
  override def sendMessage(discordMessage: DiscordMessage): F[Either[DiscordError, String]] = ???
}
