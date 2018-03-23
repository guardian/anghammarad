package com.gu.anghammarad.models

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord
import io.circe.Json
import io.circe.parser._
import org.scalatest.{EitherValues, FreeSpec, Matchers}

import scala.collection.JavaConverters._
import scala.io.Source


class SerializationTest extends FreeSpec with Matchers with EitherValues {

  "parseNotification" - {
    val validJsonString = Source.fromURL(getClass.getResource("/notification.json")).mkString
    val testJson = parse(validJsonString).right.value

    val expectedResult = Notification(
      "Terry Pratchett",
      Email,
      List(Stack("postal-service"), App("clacks-overhead")),
      "GNU Terry Pratchett",
      "Words are important. And when there is a critical mass of them, they change the nature of the universe.",
      List(Action("keep that name moving in the Overhead", "http://www.gnuterrypratchett.com/"))
    )

    val testSNS =  new SNSEvent.SNS()
    testSNS.setSubject("GNU Terry Pratchett")
    testSNS.setMessage(validJsonString)
    val testRecord =  new SNSRecord()
    testRecord.setSns(testSNS)
    val testEvent = new SNSEvent()
    testEvent.setRecords(List(testRecord).asJava)

    "will parse a SNSEvent into a complete notification" in {
      Serialization.parseNotification(testEvent).get shouldEqual expectedResult
    }

    "private method will parse valid json into a complete notification" in {
      Serialization.parseNotification("GNU Terry Pratchett", testJson).get shouldEqual expectedResult
    }
  }

  "parseChannel" - {
    "will correct determine the channel" in {
      Serialization.parseChannel("email").get shouldEqual Email
      Serialization.parseChannel("hangouts").get  shouldEqual HangoutsChat
      Serialization.parseChannel("all").get  shouldEqual All
    }

    "will return a failure if no match is found" in {
      Serialization.parseChannel("unknown").isFailure shouldEqual true
    }
  }

  "parseAction" - {
    "will correct parse an action from valid json" in {
      val testJson = parse(
        """{"cta": "keep that name moving in the Overhead","url": "http://www.gnuterrypratchett.com/"}"""
      ).right.value

      Serialization.parseAction(testJson).get shouldEqual Action("keep that name moving in the Overhead", "http://www.gnuterrypratchett.com/")
    }

    "will return a failure if either the cta or the url is unavailable" in {
      val testJson = parse(
        """{"cta": "keep that name moving in the Overhead"}"""
      ).right.value

      Serialization.parseAction(testJson).isFailure shouldEqual true
    }
  }

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
