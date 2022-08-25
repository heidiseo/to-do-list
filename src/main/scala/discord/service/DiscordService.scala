package discord.service

import discord.model.DiscordError

trait DiscordService[F[_]] {
  def sendMessage: F[Either[DiscordError, Unit]]
}

class DiscordServiceImpl[F[_]] extends DiscordService[F] {
  override def sendMessage: F[Either[DiscordError, Unit]] = ???
}
