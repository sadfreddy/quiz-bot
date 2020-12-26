package quiz.bot

import AppConfig._
import cats.effect.Sync
import pureconfig.ConfigSource
import pureconfig.generic.auto._

final case class AppConfig(http: HttpConfig, telegramBot: TelegramBotConfig, db: DbConfig)

object AppConfig {
  final case class HttpConfig(host: String, port: Int)
  final case class TelegramBotConfig(token: String, webhookUri: String)
  final case class DbConfig(url: String, driverName: String, username: String, password: String)

  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay(ConfigSource.default.loadOrThrow[AppConfig])
}
