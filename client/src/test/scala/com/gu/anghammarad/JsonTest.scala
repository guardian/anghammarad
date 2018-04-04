package com.gu.anghammarad

import com.gu.anghammarad.Json._
import com.gu.anghammarad.models._
import org.scalatest.{FreeSpec, Matchers}


class JsonTest extends FreeSpec with Matchers {
  "targetJson" - {
    "produces valid JSON for a Stack" in {
      targetJson(List(Stack("stack"))) shouldEqual """{"Stack":"stack"}"""
    }

    "produces valid JSON for a Stage" in {
      targetJson(List(Stage("stage"))) shouldEqual """{"Stage":"stage"}"""
    }

    "produces valid JSON for an App" in {
      targetJson(List(App("app"))) shouldEqual """{"App":"app"}"""
    }

    "produces valid JSON for an AwsAccount" in {
      targetJson(List(AwsAccount("123456789"))) shouldEqual """{"AwsAccount":"123456789"}"""
    }
    "produces valid JSON for all" in {
      targetJson(List(Stack("stack"), Stage("stage"), App("app"), AwsAccount("123456789"))) shouldEqual
        """{"Stack":"stack","Stage":"stage","App":"app","AwsAccount":"123456789"}"""
    }
  }

  "actionsJson" - {
    "produces correct JSON for an action" in {
      actionJson(List(Action("text", "https://example.com"))) shouldEqual """[{"cta":"text","url":"https://example.com"}]"""
    }
  }

  "messageJson" - {
    "sets message as provided" in {
      messageJson("message", "source", All, Nil, Nil) should include(""""message":"message"""")
    }

    "sets sender as provided" in {
      messageJson("message", "source", All, Nil, Nil) should include(""""sender":"source"""")
    }

    "represents channel correctly" - {
      "for 'All'" in {
        messageJson("message", "source", All, Nil, Nil) should include(""""channel":"all"""")
      }

      "for 'Email'" in {
        messageJson("message", "source", Email, Nil, Nil) should include(""""channel":"email"""")
      }

      "for 'HangoutsChat'" in {
        messageJson("message", "source", HangoutsChat, Nil, Nil) should include(""""channel":"hangouts"""")
      }
    }

    "includes target" in {
      val result = messageJson("message", "source", HangoutsChat, List(Stack("stack-name"), App("app-name")), Nil)
      result should (include("stack-name") and include("app-name"))
    }

    "includes actions" in {
      val result = messageJson("message", "source", HangoutsChat, Nil, List(Action("cta1", "url1"), Action("cta2", "url2")))
      result should (include ("cta1") and include ("url1"))
      result should (include ("cta2") and include ("url2"))
    }

    "properly escapes input" - {
      "quotes are escaped" in {
        messageJson("""Message with " <-quotes marks""", "source system", All, Nil, Nil) should include("""\"""")
      }
    }
  }
}
