package discord.model

sealed abstract class DiscordError(message: String, throwable: Throwable = null) extends Exception(message, throwable)

final case class IncomingBadRequest(error: String) extends DiscordError(error)

final case class DiscordServiceError(error: String) extends DiscordError(error)

final case class DiscordServiceTimeout(error: String) extends DiscordError(error)

final case class DiscordHttpError(error: String, statusCode: Option[Int] = None) extends DiscordError(s"$error - ${statusCode.getOrElse("no status code available")}")

final case class DiscordDeserialisationError(error: io.circe.Error, body: String) extends DiscordError(s"${error.getMessage} - $body")
