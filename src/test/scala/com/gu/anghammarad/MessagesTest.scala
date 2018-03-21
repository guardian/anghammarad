package com.gu.anghammarad

import org.scalatest.{FreeSpec, Matchers}
import Messages._


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
