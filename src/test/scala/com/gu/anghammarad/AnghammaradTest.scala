package com.gu.anghammarad

import org.scalatest.{FreeSpec, Matchers}
import Anghammarad._
import models.Serialization._
import com.gu.anghammarad.models._

import scala.io.Source


class AnghammaradTest extends FreeSpec with Matchers {
  "resolveContact" - {
    val configJson = Source.fromResource("contacts.json").mkString
    val testMapping = parseMapping(configJson).get

    "resolves app and stack to matching contacts" in {
      resolveContact(List(Stack("stack1"), App("app")), testMapping) shouldEqual List(
        Email("app.email"),
        HangoutsChat("app.channel")
      )
    }

    "resolves just stack to matching stack (not more specific stuff)" in {
      resolveContact(List(Stack("stack1")), testMapping) shouldEqual List(
        Email("stack.email"),
        HangoutsChat("stack.channel")
      )
    }
  }

  "TEST!!!" - {
    parse("""{"foo": 1, "bar": 2}""").cursor.downField("foo").as[Int]
  }
}
