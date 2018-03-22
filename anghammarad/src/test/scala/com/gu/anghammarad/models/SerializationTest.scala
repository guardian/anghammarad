package com.gu.anghammarad.models

import io.circe.Json
import io.circe.parser._
import org.scalatest.{EitherValues, FreeSpec, Matchers}

import scala.io.Source


class SerializationTest extends FreeSpec with Matchers with EitherValues {

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
          |"emailDomain",
          |"mappings":[
          |]}""".stripMargin

      Serialization.parseAllMappings(brokenJsonString).isFailure shouldEqual true
    }
  }

  "parseMapping" - {
    "will parse valid json into a complete mapping" in {
      val testJson = parse(
        """{"target":{"AwsAccount":"123456789"},"contacts":{"email":"awsAccount.email"}}"""
      ).right.value

      Serialization.parseMapping(testJson).right.value shouldEqual Mapping(List(AwsAccount("123456789")), List(EmailAddress("awsAccount.email")))
    }
  }

  "parseContacts" - {
    "will correctly return the contacts" in {
      val testJson = parse(
        """{"email":"stack.email","hangouts":"stack.room"}"""
      ).right.value

      Serialization.parseContacts(testJson) shouldEqual List(EmailAddress("stack.email"), HangoutsRoom("stack.room"))
    }

    "will return a failure if it cannot parse the keys" ignore {

    }
  }

  "parseTargets" - {
    "will correctly return the targets" in {
      val testJson: Json = parse(
        """{"Stack":"stack-1","App":"app-1"}"""
      ).right.value
      Serialization.parseTargets(testJson) shouldEqual List(Stack("stack-1"), App("app-1"))
    }

    "will return a failure if it cannot parse the keys" ignore {

    }
  }
}
