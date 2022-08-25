package discord.model

case class DiscordError(message: String, throwable: Throwable = null) extends Exception(message, throwable)
