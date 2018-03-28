package com.gu.anghammarad.serialization

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.Enrichments._
import com.gu.anghammarad.models._
import io.circe._
import io.circe.parser._

import scala.collection.JavaConverters._
import scala.util.{Success, Try}

object Serialization {
  def parseConfig(config: String): Try[Configuration] = {
    for {
      emailDomain <- parseEmailDomain(config)
      emailSender <- parseEmailSender(config)
      mappings <- parseAllMappings(config)
    } yield Configuration(emailDomain, emailSender, mappings)
  }

  private def parseEmailDomain(jsonStr: String): Try[String] = {
    val emailDomain = for {
      json <- parse(jsonStr)
      domain <- json.hcursor.downField("emailDomain").as[String]
    } yield domain

    emailDomain.toTry
  }

  private def parseEmailSender(jsonStr: String): Try[String] = {
    val emailSender = for {
      json <- parse(jsonStr)
      sender <- json.hcursor.downField("emailSender").as[String]
    } yield sender

    emailSender.toTry
  }

  def parseNotification(snsEvent: SNSEvent): Try[Notification] = {
    val maybeSns = snsEvent.getRecords.asScala.toList match {
      case singleRecord :: Nil => Success(singleRecord.getSNS)
      case _ => Fail(s"Found multiple SNSRecords")
    }

    for {
      sns <- maybeSns
      subject = sns.getSubject
      message = sns.getMessage
      jsonMsg <- parse(message).toTry
      notification <- generateNotification(subject, jsonMsg)
    } yield notification
  }

  def generateNotification(subject: String, content: Json): Try[Notification] = {
    val hCursor = content.hcursor
    val parsingResult = for {
      sourceSystem <- hCursor.downField("sender").as[String]
      rawTargets <- hCursor.downField("target").as[Json]
      rawChannel <- hCursor.downField("channel").as[String]
      rawActions <- hCursor.downField("actions").as[List[Json]]
      message <- hCursor.downField("message").as[String]
      channel <- parseRequestedChannel(rawChannel).toEither
      targets <- parseAllTargets(rawTargets).toEither
      actions <- rawActions.traverseT(parseAction).toEither
    } yield Notification(sourceSystem, channel, targets, subject, message, actions)

    parsingResult.toTry
  }

  private[serialization] def parseRequestedChannel(channel: String): Try[RequestedChannel] = {
    channel match {
      case "email" => Success(Email)
      case "hangouts" => Success(HangoutsChat)
      case "all" => Success(All)
      case _ => Fail(s"Parsing error: Unable to match RequestedChannel to known options")
    }
  }

  private[serialization] def parseAction(json: Json): Try[Action] = {
    val hCursor = json.hcursor
    val parsingResult = for {
      cta <- hCursor.downField("cta").as[String]
      url <- hCursor.downField("url").as[String]
    } yield Action(cta, url)

    parsingResult.toTry
  }

  def parseAllMappings(jsonStr: String): Try[List[Mapping]] = {
    val allMappings = for {
      json <- parse(jsonStr)
      rawMappings <- json.hcursor.downField("mappings").as[List[Json]]
      mappings <- rawMappings.traverseT(parseMapping).toEither
    } yield mappings

    allMappings.toTry
  }

  private[serialization] def parseMapping(json: Json): Try[Mapping] = {
    val hCursor = json.hcursor
    val mappings = for {
      rawTargets <- hCursor.downField("target").as[Json]
      rawContacts <- hCursor.downField("contacts").as[Json]
      targets <- parseAllTargets(rawTargets).toEither
      contacts <- parseAllContacts(rawContacts).toEither
    } yield Mapping(targets, contacts)

    mappings.toTry
  }

  private[serialization] def parseTarget(key: String, value: String): Try[Target] = {
    key match {
      case "Stack" => Success(Stack(value))
      case "Stage" => Success(Stage(value))
      case "App" => Success(App(value))
      case "AwsAccount" => Success(AwsAccount(value))
      case _ => Fail(s"Unable to match keys to known targets")
    }
  }

  private[serialization] def parseAllTargets(jsonTargets: Json): Try[List[Target]] = {
    val c: HCursor = jsonTargets.hcursor
    val allTargets = for {
      key <- c.keys.map(k => k.toList).getOrElse(List.empty)
      address <- c.downField(key).as[String].toOption
      parsedTarget = parseTarget(key, address).toEither
    } yield parsedTarget

    allTargets match {
      case _ :: _ => allTargets.traverseE(identity).toTry
      case Nil => Fail(s"Parsing error: List of targets is empty")
    }
  }

  private[serialization] def parseContact(key: String, value: String): Try[Contact] = {
    key match {
      case "email" => Success(EmailAddress(value))
      case "hangouts" => Success(HangoutsRoom(value))
      case _ => Fail(s"Unable to match keys to known contact methods")
    }
  }

  private[serialization] def parseAllContacts(jsonContacts: Json): Try[List[Contact]] = {
    val c: HCursor = jsonContacts.hcursor
    val allContacts = for {
      key <- c.keys.map(k => k.toList).getOrElse(List.empty)
      address <- c.downField(key).as[String].toOption
      parsedContact = parseContact(key, address).toEither
    } yield parsedContact

    allContacts match {
      case _ :: _ => allContacts.traverseE(identity).toTry
      case Nil => Fail(s"Parsing error: List of contacts is empty")
    }
  }
}
