package com.gu.anghammarad

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.ArgParser.argParser
import com.gu.anghammarad.models.{EmailAddress, HangoutsRoom, Notification}
import com.gu.anghammarad.serialization.Serialization
import io.circe.parser._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {

  def sendNotificationUsingService(notification: Notification, stage: String): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    val sentMessages = for {
      config <- Config.loadConfig(stage)
      configuration <- Serialization.parseConfig(config)
      sent <- AnghammaradService.run(notification, configuration)
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
  }

  def sendNotificationUsingClient(notification: Notification, topicArn: String): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    val sendNotice = Anghammarad.notify(notification, topicArn)

    sendNotice onComplete {
      case Success(messageId) =>
        logger.info(s"sent notification to ${topicArn}, got id: ${messageId}")
      case Failure(err) =>
        logger.error("Failed to send notification", err)
    }

    Await.ready(
      sendNotice,
      Duration("5 seconds")
    )
  }

  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    argParser.parse(args, InitialArgs) match {
      case Some(arguments) =>
        for {
          notification <- notificationFromArguments(arguments)
          stage <- stageFromArguments(arguments)
          useClient <- useClientFromArguments(arguments)
        } yield useClient match {
          case Some(topicArn) => sendNotificationUsingClient(notification, topicArn)
          case None => sendNotificationUsingService(notification, stage)
        }

      case None =>
      // arguments were not valid, help will have been printed
    }
  }

  def notificationFromArguments(args: Arguments): Try[Notification] = {
    args match {
      case JsonArgs(subject, jsonStr, _) =>
        for {
          json <- parse(jsonStr).toTry
          notification <- Serialization.generateNotification(subject, json)
        } yield notification
      case Specified(subject, message, actions, targets, Some(channel), source, _, _, threadKey) =>
        Success(Notification(subject, message, actions, targets, channel, source, threadKey))
      case s: Specified =>
        Fail("No channel provided")
      case InitialArgs =>
        argParser.showUsageOnError
        Fail("No arguments provided, cannot make a notification")
    }
  }

  def stageFromArguments(args: Arguments): Try[String] = {
    args match {
      case JsonArgs(_, _, configStage) =>
        Success(configStage)
      case Specified(_, _, _, _, _, _, configStage, _, _) =>
        Success(configStage)
      case InitialArgs =>
        argParser.showUsageOnError
        Fail("No arguments provided, cannot obtain a configuration stage")
    }
  }

  def useClientFromArguments(args: Arguments): Try[Option[String]] = {
    args match {
      case JsonArgs(_, _, _) =>
        Success(None)
      case Specified(_, _, _, _, _, _, _, useClient, _) =>
        Success(useClient)
      case InitialArgs =>
        argParser.showUsageOnError
        Fail("No arguments provided, cannot obtain a configuration stage")
    }
  }
}
