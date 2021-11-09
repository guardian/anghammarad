package com.gu.anghammarad.serialization

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord
import com.gu.anghammarad.models._
import io.circe.Json
import io.circe.parser._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import com.gu.anghammarad.testutils.TryValues
import org.scalatest.EitherValues

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.io.Source


class SerializationTest extends AnyFreeSpec with Matchers with EitherValues with TryValues {

  "Notification Serialization" - {
    val validJsonString = Source.fromURL(getClass.getResource("/notification.json")).mkString
    val validJson = parse(validJsonString).value

    val expectedResult = Notification(
      "GNU Terry Pratchett",
      "Words are important. And when there is a critical mass of them, they change the nature of the universe.",
      List(Action("keep that name moving in the Overhead", "http://www.gnuterrypratchett.com/")),
      List(Stack("postal-service"), App("clacks-overhead")),
      Email,
      "Terry Pratchett"
    )

    "parseNotification" - {
      val testSNS = new SNSEvent.SNS()
      val testRecord = new SNSRecord()
      val testEvent = new SNSEvent()

      testSNS.setSubject("GNU Terry Pratchett")
      testSNS.setMessage(validJsonString)
      testRecord.setSns(testSNS)

      "will parse a SNSEvent into a complete notification" in {
        val input = testEvent
          input.setRecords(List(testRecord).asJava)
        Serialization.parseNotification(testEvent).success shouldEqual expectedResult
      }

      "will fail if the SNSEvent is missing" in {
        val input = testEvent
        input.setRecords(List.empty.asJava)
        Serialization.parseNotification(input).isFailure shouldEqual true
      }

      "will fail if there is more than one SNSEvent" in {
        val input = testEvent
        input.setRecords(List(testRecord, testRecord).asJava)
        Serialization.parseNotification(input).isFailure shouldEqual true
      }
    }

    "generateNotification" - {
      "will parse a string and valid json into a complete notification" in {
        Serialization.generateNotification("GNU Terry Pratchett", validJson).success shouldEqual expectedResult
      }

      "will return a failure if the json is missing any required information" in {
        val testJson = parse(
          """{"sender": "Terry Pratchett","target": {"Stack": "postal-service"}}"""
        ).value
        Serialization.generateNotification("GNU Terry Pratchett", testJson).isFailure shouldEqual true
      }
    }
  }

  "parseRequestedChannel" - {
    "will correct determine the channel" in {
      Serialization.parseRequestedChannel("email").success shouldEqual Email
      Serialization.parseRequestedChannel("hangouts").success  shouldEqual HangoutsChat
      Serialization.parseRequestedChannel("all").success  shouldEqual All
      Serialization.parseRequestedChannel("prefer email").success  shouldEqual Preferred(Email)
      Serialization.parseRequestedChannel("prefer hangouts").success  shouldEqual Preferred(HangoutsChat)
    }

    "will return a failure if no match is found" in {
      Serialization.parseRequestedChannel("unknown").isFailure shouldEqual true
    }
  }

  "parseAction" - {
    "will correct parse an action from valid json" in {
      val testJson = parse(
        """{"cta": "keep that name moving in the Overhead","url": "http://www.gnuterrypratchett.com/"}"""
      ).value

      Serialization.parseAction(testJson).success shouldEqual Action("keep that name moving in the Overhead", "http://www.gnuterrypratchett.com/")
    }

    "will return a failure if either the cta or the url is unavailable" in {
      val testJson = parse(
        """{"cta": "keep that name moving in the Overhead"}"""
      ).value

      Serialization.parseAction(testJson).isFailure shouldEqual true
    }
  }

  "parseAllMappings" - {
    "will parse all mappings when given a valid string" in {
      val validJsonString = Source.fromURL(getClass.getResource("/config.json")).mkString

      val expectedResult = List(
        Mapping(List(Stack("stack1")), List(EmailAddress("stack1.email"), HangoutsRoom("stack1.room"))),
        Mapping(List(Stack("stack1"), App("app1")), List(EmailAddress("app1.email"), HangoutsRoom("app1.room"))),
        Mapping(List(AwsAccount("123456789")), List(EmailAddress("awsAccount.email"))),
        Mapping(List(Stack("postal-service"), App("clacks-overhead")), List(EmailAddress("discworld.email")))
      )

      Serialization.parseAllMappings(validJsonString).success shouldEqual expectedResult
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
      ).value

      Serialization.parseMapping(testJson).success shouldEqual Mapping(List(AwsAccount("123456789")), List(EmailAddress("awsAccount.email")))
    }
  }

  "parseContact" - {
    "will correct determine the channel" in {
      Serialization.parseContact("email", "example.email").success shouldEqual EmailAddress("example.email")
      Serialization.parseContact("hangouts", "example.room").success  shouldEqual HangoutsRoom("example.room")
    }

    "will return a failure if no match is found" in {
      Serialization.parseContact("unknown", "unknown").isFailure shouldEqual true
    }
  }

  "parseAllContacts" - {
    "will correctly return the contacts" in {
      val testJson = parse(
        """{"email":"stack.email","hangouts":"stack.room"}"""
      ).value

      Serialization.parseAllContacts(testJson).success shouldEqual List(EmailAddress("stack.email"), HangoutsRoom("stack.room"))
    }

    "will return a failure if it cannot parse the keys" in {
      val testJson = parse(
        """{"email":"stack.email","xhangouts":"stack.room"}"""
      ).value
      Serialization.parseAllContacts(testJson).isFailure shouldEqual true
    }

    "will return a failure if there are no contacts listed" in {
      val testJson = parse(
        """{}"""
      ).value
      Serialization.parseAllContacts(testJson).isFailure shouldEqual true
    }
  }

  "parseTarget" - {
    "will correct determine the target" in {
      Serialization.parseTarget("Stack", "example-stack").success shouldEqual Stack("example-stack")
      Serialization.parseTarget("Stage", "example-stage").success  shouldEqual Stage("example-stage")
      Serialization.parseTarget("App", "example-app").success  shouldEqual App("example-app")
      Serialization.parseTarget("AwsAccount", "example-account").success  shouldEqual AwsAccount("example-account")
    }

    "will return a failure if no match is found" in {
      Serialization.parseTarget("unknown", "unknown").isFailure shouldEqual true
    }
  }

  "parseAllTargets" - {
    "will correctly return the targets" in {
      val testJson: Json = parse(
        """{"Stack":"stack-1","App":"app-1"}"""
      ).value
      Serialization.parseAllTargets(testJson).success shouldEqual List(Stack("stack-1"), App("app-1"))
    }

    "will return a failure if it cannot parse the keys" in {
      val testJson = parse(
        """{"Stack":"stack-1","xApp":"app-1"}"""
      ).value
      Serialization.parseAllTargets(testJson).isFailure shouldEqual true
    }

    "will return a failure if there are no targets listed" in {
      val testJson = parse(
        """{}"""
      ).value
      Serialization.parseAllTargets(testJson).isFailure shouldEqual true
    }
  }
}
