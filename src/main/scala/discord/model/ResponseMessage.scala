package discord.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class ResponseMessage(message: String)

object ResponseMessage {
  implicit val discordMessageCodec: Codec[ResponseMessage] = deriveCodec[ResponseMessage]
}