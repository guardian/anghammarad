package com.gu.anghammarad.models

import io.circe.Json
import io.circe.parser._
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source

class SerializationTest extends FreeSpec with Matchers {

  "parseAllMappings" - {

    "will parse all mappings when given a valid string" in {
      val validJsonString = Source.fromURL(getClass.getResource("/config.json")).mkString

      val expectedResult = List(
        Mapping(List(Stack("stack1")), List(EmailAddress("stack1.email"), HangoutsRoom("stack1.room"))),
        Mapping(List(Stack("stack1"), App("app1")), List(EmailAddress("app1.email"), HangoutsRoom("app1.room"))),
        Mapping(List(AwsAccount("123456789")), List(EmailAddress("awsAccount.email")))
      )

      Serialization.parseAllMappings(validJsonString).get shouldEqual expectedResult
    }

    "will return a failure if there are no mappings" in {
      val noMappingsString = """{"emailDomain":"example.com"}"""

      Serialization.parseAllMappings(noMappingsString).isFailure shouldEqual true
    }

    "will return a failure if the string is not valid json" in {
      val brokenJsonString =
        """{
          |"emailDomain":"example.com",
          |"mappings":[
          |{
          |"target":{"Stack":"stack1"},
          |"contacts":{"email":"stack1.email","hangouts":"stack1.room"}
          |},
          |{
          |"target":"Stack":"stack1","App":"app1"},
          |"contacts":{"email":"app1.email","hangouts":"app1.room"}
          |},
          |{
          |"target":{"AwsAccount":"123456789"},
          |"contacts":{"email":"awsAccount.email"}
          |}
          |]}""".stripMargin

      Serialization.parseAllMappings(brokenJsonString).isFailure shouldEqual true
    }
  }

  "parseMapping" - {
    "will parse valid json into a complete mapping" in {
      val testJson = parse(
        """{"target":{"AwsAccount":"123456789"},"contacts":{"email":"awsAccount.email"}}"""
      ).getOrElse(Json.Null)

      Serialization.parseMapping(testJson).getOrElse(Json.Null) shouldEqual Mapping(List(AwsAccount("123456789")), List(EmailAddress("awsAccount.email")))
    }

    "will match regardless of case" in {
      val testJson = parse(
        """{"target":{"AwsAccount":"123456789"},"contacts":{"email":"awsAccount.email"}}"""
      ).getOrElse(Json.Null)

      Serialization.parseMapping(testJson).getOrElse(Json.Null) shouldEqual Mapping(List(AwsAccount("123456789")), List(EmailAddress("awsAccount.email")))
    }
  }

  "parseContacts" - {
    "will correctly return the contacts" in {
      val testJson = parse(
        """{"email":"stack.email","hangouts":"stack.room"}"""
      ).getOrElse(Json.Null)

      Serialization.parseContacts(testJson) shouldEqual List(EmailAddress("stack.email"), HangoutsRoom("stack.room"))
    }

    "will match regardless of case" in {
      val testJson = parse(
        """{"email":"stack.email","Hangouts":"stack.room"}"""
      ).getOrElse(Json.Null)

      Serialization.parseContacts(testJson) shouldEqual List(EmailAddress("stack.email"), HangoutsRoom("stack.room"))
    }

    "will return a failure if it cannot parse the keys" ignore {

    }
  }

  "parseTargets" - {
    val testJson: Json = parse(
      """{"stack":"stack-1","app":"app-1"}"""
    ).getOrElse(Json.Null)

    "will correctly return the targets" in {
      Serialization.parseTargets(testJson) shouldEqual List(Stack("stack-1"), App("app-1"))
    }

    "will return a failure if it cannot parse the keys" ignore {

    }
  }
}
