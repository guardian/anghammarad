package com.gu.anghammarad

import com.gu.anghammarad.common.models._
import com.gu.anghammarad.messages.Messages._
import com.gu.anghammarad.messages.{HangoutsService, Messages}
import io.circe.parser._
import org.scalatest.{FreeSpec, Matchers}
import scala.util.{Failure, Success}


class MessagesTest extends FreeSpec with Matchers {
  "emailMessage" - {
    "plain text" - {
      "sets the subject" in {
        val notification = testNotification("subject", "message")
        emailMessage(notification).subject shouldEqual "subject"
      }

      "if actions are present" - {
        val notification = testNotification("subject", "message", Action("cta1", "url1"), Action("cta2", "url2"))

        "adds divider" in {
          emailMessage(notification).plainText should include("_____________")
        }

        "adds actions after divider" in {
          val chunks = emailMessage(notification).plainText.split("_____________")
          chunks(1) should (include("cta1") and include("url1") and include("cta2") and include("url2"))
        }

        "includes the message" in {
          val notification = testNotification("subject", "message")
          emailMessage(notification).plainText should include("message")
        }
      }

      "if actions are not present" - {
        val notification = testNotification("subject", "message")

        "does not add divider" in {
          emailMessage(notification).plainText should not include "_____________"
        }

        "uses the given message" in {
          val notification = testNotification("subject", "message")
          emailMessage(notification).plainText shouldEqual "message"
        }
      }
    }

    "html" - {
      "if actions are present" - {
        val notification = testNotification("subject", "message", Action("cta1", "url1"), Action("cta2", "url2"))

        "adds divider" in {
          emailMessage(notification).html should include("<hr")
        }

        "adds actions after divider" in {
          val chunks = emailMessage(notification).html.split("<hr")
          chunks(1) should (include("""<a href="url1">cta1</a>""") and include("""<a href="url2">cta2</a>"""))
        }

        "includes the given message if it is simple" in {
          val notification = testNotification("subject", "message")
          emailMessage(notification).html should include("message")
        }
      }

      "if actions are not present" - {
        val notification = testNotification("subject", "message")

        "does not add divider" in {
          emailMessage(notification).html should not include "<hr"
        }

        "includes the given message if it is simple" in {
          val notification = testNotification("subject", "message")
          emailMessage(notification).html should include("message")
        }

        "converts markdown to HTML" in {
          val notification = testNotification("subject", "*em* **strong**")
          emailMessage(notification).html should (include("<em>em</em>") and include("<strong>strong</strong>"))
        }
      }
    }
  }

  "hangoutMessage" - {
    "if actions are present" - {
      val notification = testNotification("subject", "message", Action("cta1", "url1"), Action("cta2", "url2"))

      "is valid JSON" in {
        val message = hangoutMessage(notification)
        val result = parse(message.cardJson)
        result match {
          case Right(_) =>
          case Left(err) => throw err
        }
      }
    }

    "if no actions are present" - {
      val notification = testNotification("subject", "message")

      "is valid JSON" in {
        val message = hangoutMessage(notification)
        val result = parse(message.cardJson)
        result match {
          case Right(_) =>
          case Left(err) => throw err
        }
      }

    }
  }

  def testNotification(subject: String, message: String, actions: Action*): Notification = {
    Notification("test", All, Nil, subject, message, actions.toList)
  }

  /**
    * This is useful for iterating on the card format.
    *
    * Switch ignore to in and run this test to send test message
    *
    * TODO:
    * you will need to provide a webhook and we should make this test read
    * that out of config outside this repo for safety
    */
  "test end-to-end hangouts message" ignore {
    val notification = Notification(
      "Testing", HangoutsChat, Nil, "Subject",
      "Mentions don't work in card messages yet.\nMessage *with* some **styles**!",
      List(Action("CTA", "https://example.com/"), Action("Another CTA", "https://example.com/"))
    )
    val message = Messages.hangoutMessage(notification)
    // println(message.cardJson)
    val result = HangoutsService.sendHangoutsMessage("???", message)
    result match {
      case Success(_) => ()
      case Failure(err) => throw err
    }
  }
}
