package discord.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class DiscordMessage(content: String)

object DiscordMessage {
  implicit val discordMessageCodec: Codec[DiscordMessage] = deriveCodec[DiscordMessage]
}

