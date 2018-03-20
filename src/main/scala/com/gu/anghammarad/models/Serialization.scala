package com.gu.anghammarad.models

import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._

import scala.util.{Failure, Success, Try}
import com.gu.anghammarad.Enrichments._


object Serialization {
  def parseMapping(jsonStr: String): Try[Mapping] = {
    val tryJson = parse(jsonStr).fold(
      err => Failure(err),
      json => Success(json)
    )



    tryJson.map { json =>
      val mappings = json.hcursor.downField("mappings").as[List[Json]]
    }

    for {
      json <- parse(jsonStr)
      mappings <- json.hcursor.downField("mappings").as[List[Json]]
      tmp <- mappings.traverseE(parseMapping)
    } yield 1

    ???
  }

  def parseMapping(json: Json): Result[(List[Target], List[Contact])] = {
    ???
  }
}
