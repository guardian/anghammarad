package com.gu.anghammarad.models

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.Enrichments._
import io.circe.Decoder.Result
import io.circe._
import io.circe.parser._

import scala.util.{Failure, Success, Try}


object Serialization {

    def parseAllNotifications(snsEvent: SNSEvent): Try[List[Notification]] = {
      ???
    }

  private[models] def parseNotification(subject: String, content: Json): Try[Notification] = {
    val hCursor = content.hcursor
    val parsingResult = for {
      sourceSystem <- hCursor.downField("sender").as[String]
      rawTargets <- hCursor.downField("target").as[Json]
      rawChannel <- hCursor.downField("channel").as[String]
      rawActions <- hCursor.downField("actions").as[List[Json]]
      message <- hCursor.downField("message").as[String]
      channel <- parseChannel(rawChannel).toEither
      targets = parseTargets(rawTargets)
      actions <- rawActions.traverseT(parseAction).toEither
    } yield Notification(sourceSystem, channel, targets, subject, message, actions)

    parsingResult.fold(
      err => Failure(err),
      notification => Success(notification)
    )
  }

  private[models] def parseChannel(channel: String): Try[RequestedChannel] = {
    channel match {
      case "email" => Success(Email)
      case "hangouts" => Success(HangoutsChat)
      case "all" => Success(All)
      case _ => Fail(s"Cannot parse RequestedChannel")
    }
  }

  private[models] def parseAction(json: Json): Try[Action] = {
    val hCursor = json.hcursor
    val parsingResult = for {
      cta <- hCursor.downField("cta").as[String]
      url <- hCursor.downField("url").as[String]
    } yield Action(cta, url)

    parsingResult.fold(
      err => Failure(err),
      action => Success(action)
    )
  }

  def parseAllMappings(jsonStr: String): Try[List[Mapping]] = {
    val allMappings = for {
      json <- parse(jsonStr)
      rawMappings <- json.hcursor.downField("mappings").as[List[Json]]
      mappings <- rawMappings.traverseE(parseMapping)
    } yield mappings

    allMappings.fold(
      err => Failure(err),
      mappings => Success(mappings)
    )
  }

  private[models] def parseMapping(json: Json): Result[Mapping] = {
    val hCursor = json.hcursor
    for {
      rawTargets <- hCursor.downField("target").as[Json]
      rawContacts <- hCursor.downField("contacts").as[Json]
      targets = parseTargets(rawTargets)
      contacts = parseContacts(rawContacts)
    } yield Mapping(targets, contacts)
  }

  private[models] def parseTargets(jsonTargets: Json): List[Target] = {
    def parseTarget(key: String, value: String): Option[Target] = {
      key match {
        case "Stack" => Some(Stack(value))
        case "Stage" => Some(Stage(value))
        case "App" => Some(App(value))
        case "AwsAccount" => Some(AwsAccount(value))
        case _ => None
      }
    }

    val c: HCursor = jsonTargets.hcursor
    // TODO: do we want to return an empty list, or throw a error here?
    val keys: List[String] = c.keys.map(k => k.toList).getOrElse(List.empty)
    keys.flatMap { key =>
      val address = c.downField(key).as[String].getOrElse("")
      parseTarget(key, address)
    }
  }

  private[models] def parseContacts(jsonContacts: Json): List[Contact] = {
    def parseContact(key: String, value: String): Option[Contact] = {
      key match {
        case "email" => Some(EmailAddress(value))
        case "hangouts" => Some(HangoutsRoom(value))
        case _ => None
      }
    }

    val c: HCursor = jsonContacts.hcursor
    // TODO: do we want to return an empty list, or throw a error here?
    val keys: List[String] = c.keys.map(k => k.toList).getOrElse(List.empty)

    keys.flatMap { key =>
      val address = c.downField(key).as[String].getOrElse("")
      parseContact(key, address)
    }
  }
}
