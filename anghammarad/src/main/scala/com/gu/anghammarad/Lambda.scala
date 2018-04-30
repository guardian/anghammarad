package com.gu.anghammarad

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.anghammarad.models.{EmailAddress, HangoutsRoom}
import com.gu.anghammarad.serialization.Serialization

import scala.util.{Failure, Success}

class Lambda extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {
    val sentMessages = for {
      stage <- Config.getStage()
      config <- Config.loadConfig(stage)
      configuration <- Serialization.parseConfig(config)
      notification <- Serialization.parseNotification(input)
      sent <- Anghammarad.run(notification, configuration)
    } yield sent

    // send notification if result is a failure
    sentMessages match {
      case Failure(err) =>
        context.getLogger.log(s"Failed to send message ${err.getMessage}")
        throw err
      case Success(sent) =>
        sent.map {
          case (_, EmailAddress(email)) => s"Email: $email"
          case (_, HangoutsRoom(webhook)) => s"Hangouts webhook: $webhook"
        }
        context.getLogger.log(s"sent ${sent.mkString(",")}")
    }
  }
}
