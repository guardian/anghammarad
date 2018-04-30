package com.gu.anghammarad

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.ArgParser.argParser
import com.gu.anghammarad.models.{EmailAddress, HangoutsRoom, Notification}
import com.gu.anghammarad.serialization.Serialization
import io.circe.parser._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}


object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    argParser.parse(args, InitialArgs) match {
      case Some(arguments) =>
        val stage = "DEV"
        val devConfig = Config.loadConfig(stage)
        val devMappings = Serialization.parseAllMappings(devConfig.getOrElse(""))

        logger.info(s"Loaded configuration from S3: ${devConfig.isSuccess}")
        logger.info(s"Config parsing succeeded: ${devMappings.isSuccess}")

        val sentMessages = for {
          notification <- notificationFromArguments(arguments)
          config <- Config.loadConfig(stage)
          configuration <- Serialization.parseConfig(config)
          sent <- Anghammarad.run(notification, configuration)
        } yield sent

        sentMessages match {
          case Failure(err) =>
            logger.error("Failed to send notification", err)
          case Success(sent) =>
            sent.map {
              case (_, EmailAddress(email)) => s"Email: $email"
              case (_, HangoutsRoom(webhook)) => s"Hangouts webhook: $webhook"
            }
            logger.info(s"sent notification ${sent.mkString(",")}")
        }
      case None =>
        // arguments were not valid, help will have been printed
    }
  }

  def notificationFromArguments(args: Arguments): Try[Notification] = {
    args match {
      case Json(subject, jsonStr) =>
        for {
          json <- parse(jsonStr).toTry
          notification <- Serialization.generateNotification(subject, json)
        } yield notification
      case Specified(subject, message, actions, targets, Some(channel), source) =>
        Success(Notification(subject, message, actions, targets, channel, source))
      case s: Specified =>
        Fail("No channel provided")
      case InitialArgs =>
        argParser.showUsageAsError()
        Fail("No arguments provided, cannot make a notification")
    }
  }
}
