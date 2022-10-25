package discord.model

import cats.effect.kernel.Sync
import pureconfig.generic.auto._
import pureconfig.module.catseffect._

case class DiscordConfig(port: Int, host: String, webhookUrl: String)

object DiscordConfig {
  def load[F[_] : Sync]: F[DiscordConfig] = loadConfigF[F, DiscordConfig]
}
