package quiz.bot.http

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Request, Uri}
import BotMsg.Out.{MessageResponse, WebhookResponse}
import quiz.bot.CustomError.WebhookError

trait BotClient[F[_]] {
  def sendMessage(chatId: Long, message: String): F[Unit]
  def setWebhook(webHookUri: String): F[Unit]
}

object BotClient {
  def apply[F[_]: Sync](client: Client[F], token: String): F[BotClient[F]] = {
    Slf4jLogger.create[F].map { implicit logger =>
      new BotClientImpl(client, uri"https://api.telegram.org" / s"bot$token")
    }
  }

  private final class BotClientImpl[F[_]: Sync: Logger](
    client: Client[F],
    botUri: Uri
  ) extends BotClient[F] {
    implicit private val messageResponseDecoder: EntityDecoder[F, MessageResponse] = jsonOf[F, MessageResponse]
    implicit private val webhookResponseDecoder: EntityDecoder[F, WebhookResponse] = jsonOf[F, WebhookResponse]

    override def sendMessage(chatId: Long, message: String): F[Unit] = {
      val uri = botUri / "sendMessage" =? Map(
        "chat_id" -> List(chatId.toString),
        "parse_mode" -> List("Markdown"),
        "text" -> List(message),
      )

      client.expect[MessageResponse](uri).map(_ =>())
    }

    override def setWebhook(webHookUri: String): F[Unit] = {
      val uri = botUri / s"setWebhook" =? Map(
        "url" -> List(webHookUri)
      )

      client.fetchAs[WebhookResponse](Request[F](uri = uri)).flatMap { result =>
        if (result.ok) Logger[F].info("Webhook is set")
        else Sync[F].raiseError(WebhookError(result.description))
      }
    }
  }
}
