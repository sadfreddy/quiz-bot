package quiz.bot

import cats.effect._
import cats.syntax.applicativeError._
import ch.qos.logback.classic.LoggerContext
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import javax.net.ssl.SSLContext
import org.http4s.client.blaze.BlazeClientBuilder
import org.slf4j.LoggerFactory
import quiz.bot.dao.{CategoryDao, DaoInit}
import quiz.bot.http.{BotClient, BotService, HttpServer}

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  private val httpClient =
    BlazeClientBuilder[IO](ExecutionContext.global)
      .withSslContext(SSLContext.getDefault)
      .resource

  private val flushLogs = IO(LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].stop())

  override def run(args: List[String]): IO[ExitCode] = {
    val app = for {
      config <- AppConfig.load[IO]
      _      <- logStart(config)
      _      <- startServer(config)
    } yield ExitCode.Success

    Slf4jLogger.create[IO].flatMap( logger =>
      app.onError {
        case error: CustomError => logger.error(error.message)
        case error              => logger.error(error)("Failed to start app")
      }
    ).guarantee(flushLogs)
  }

  private def startServer(config: AppConfig): IO[Unit] = {
    val resources = for {
      client     <- httpClient
      transactor <- DaoInit.transactor[IO](config.db)
    }  yield (client, transactor)

    resources.evalMap {
      case (client, transactor) =>
        for {
          _          <- DaoInit.initialize[IO](transactor)
          botClient  <- BotClient(client, config.telegramBot.token)
          _          <- botClient.setWebhook(config.telegramBot.webhookUri)
          dao         = new CategoryDao[IO](transactor)
          botService <- BotService(botClient, dao)
        } yield HttpServer[IO](botClient, dao, botService)
    }.use { server =>
      server.start(ExecutionContext.global, config.http)
    }
  }

  private def logStart(config: AppConfig): IO[Unit] = {
    for {
      logger   <- Slf4jLogger.create[IO]
      maxMemory = Runtime.getRuntime.maxMemory / (1024 * 1024)
      _        <- logger.info(s"Starting app with maxMemory=$maxMemory MB, config: $config")
    } yield ()
  }
}
