package com.gu.anghammarad.models

import com.gu.anghammarad.Enrichments._
import io.circe.Decoder.Result
import io.circe._
import io.circe.parser._

import scala.util.{Failure, Success, Try}


object Serialization {

  def parseAllMappings(jsonStr: String): Try[List[Mapping]] = {
    val allMappings = for {
      json <- parse(jsonStr)
      rawMappings <- json.hcursor.downField("mappings").as[List[Json]]
      mappings <- rawMappings.traverseE(parseMapping)
    } yield mappings

    allMappings.fold(
      err => Failure(err),
      suc => Success(suc)
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
      key.toLowerCase match {
        case "stack" => Some(Stack(value))
        case "stage" => Some(Stage(value))
        case "app" => Some(App(value))
        case "awsaccount" => Some(AwsAccount(value))
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
      key.toLowerCase match {
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
