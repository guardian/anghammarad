package com.gu.anghammarad

import cats.implicits._
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.anghammarad.models._
import com.gu.anghammarad.serialization.Serialization

import scala.util.{Failure, Success, Try}

class Lambda extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {

    val parseNotification = Serialization.parseNotification(input)

    val sentMessages = for {
      stage <- Config.getStage()
      config <- Config.loadConfig(stage)
      configuration <- Serialization.parseConfig(config)
      notification <- parseNotification
      sent <- AnghammaradService
        .run(notification, configuration)
        .attemptTap(notifyOnFailure(notification, configuration))
    } yield sent

    // send notification if result is a failure
    sentMessages match {
      case Failure(err) =>
        context.getLogger.log(s"Failed to send message ${err.getMessage}. " +
          s"Notification details: ${parseNotification.getOrElse("Could not parse notification")}")
        throw err
      case Success(sent) =>
        sent.map {
          case (_, EmailAddress(email)) => s"Email: $email"
          case (_, HangoutsRoom(webhook)) => s"Hangouts webhook: $webhook"
        }
        context.getLogger.log(s"sent ${sent.mkString(",")}")
    }
  }

  private def notifyOnFailure(
      originalNotification: Notification,
      configuration: Configuration
  )(
      result: Either[Throwable, List[(Message, Contact)]]
  ): Try[List[(Message, Contact)]] = {
    result match {
      case Left(err) =>
        // if the original notification failed, alert Anghammarad's admins
        val errorNotification = Notification(
          subject = "Anghammarad notification failure",
          message =
            s"""Failed to send Anghammarad notification from ${originalNotification.sourceSystem}:
              |
              |Original message: ${originalNotification.subject}
              |Targets: ${originalNotification.target.mkString(",")}
              |Channel: ${originalNotification.channel}
              |
              |${err.getMessage}
              |""".stripMargin,
          actions = List(
            Action(
              cta = "Check logs",
              url =
                "https://logs.gutools.co.uk/s/devx/app/r/s/CPXIT" // Today's Anghammarad logs
            )
          ),
          target = List(GithubTeamSlug("devx-reliability-and-ops")),
          channel = All,
          sourceSystem = "Anghammarad",
          threadKey = originalNotification.threadKey
        )
        AnghammaradService.run(errorNotification, configuration)
      case Right(_) =>
        Success(Nil)
    }
  }
}
