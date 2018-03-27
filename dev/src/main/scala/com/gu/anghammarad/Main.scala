package com.gu.anghammarad

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.ArgParser.argParser
import com.gu.anghammarad.models.Notification
import com.gu.anghammarad.serialization.Serialization
import io.circe.parser._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Success, Try}


object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    argParser.parse(args, InitialArgs) match {
      case Some(arguments) =>
        val stage = Config.getStage()
        val devConfig = Config.loadConfig(stage)
        val devMappings = Serialization.parseAllMappings(devConfig.getOrElse(""))

        logger.info(s"Loaded configuration from S3: ${devConfig.isSuccess}")
        logger.info(s"Config parsing succeeded: ${devMappings.isSuccess}")

        val result = for {
          config <- Config.loadConfig(stage)
          configuration <- Serialization.parseConfig(config)
          notification <- notificationFromArguments(arguments)
          _ <- Anghammarad.run(notification, configuration)
        } yield ()

        result.fold(
          { err =>
            logger.error("Failed to send notification", err)
          },
          _ =>
            logger.info("Ok")
        )
      case None =>
    }
  }

  def notificationFromArguments(args: Arguments): Try[Notification] = {
    args match {
      case Json(subject, jsonStr) =>
        for {
          json <- parse(jsonStr).toTry
          notification <- Serialization.generateNotification(subject, json)
        } yield notification
      case Specified(source, Some(channel), targets, subject, message, actions) =>
        Success(Notification(source, channel, targets, subject, message, actions))
      case s: Specified =>
        Fail("No channel provided")
      case InitialArgs =>
        Fail("No arguments provided, cannot make a notification")
    }
  }
}
