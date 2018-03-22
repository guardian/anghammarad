package com.gu.anghammarad

import com.gu.anghammarad.messages.Messages._
import org.scalatest.{FreeSpec, Matchers}


class MessagesTest extends FreeSpec with Matchers {
  "emailContents" - {
    "parses a string" in {
      val md = mdParser.parse("Just a string")
      emailContents(md) shouldEqual "<p>Just a string</p>\n"
    }

    "parses simple HTML" in {
      val md = mdParser.parse("Just a **string**")
      emailContents(md) shouldEqual "<p>Just a <strong>string</strong></p>\n"
    }
  }
}
