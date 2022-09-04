package discord.model

sealed abstract class DiscordError(message: String, throwable: Throwable = null) extends Exception(message, throwable)

final case class IncomingBadRequest(error: String) extends DiscordError(error)

final case class DiscordServiceError(error: String) extends DiscordError(error)
