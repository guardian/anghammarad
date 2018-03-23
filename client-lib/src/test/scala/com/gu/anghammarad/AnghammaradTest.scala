package com.gu.anghammarad

import com.gu.anghammarad.models._
import com.gu.anghammarad.Anghammarad._
import org.scalatest.{FreeSpec, Matchers}


class AnghammaradTest extends FreeSpec with Matchers {
  "targetJson" - {
    "produces valid JSON for a Stack" in {
      targetJson(Stack("stack")) shouldEqual """{"Stack":"stack"}"""
    }

    "produces valid JSON for a Stage" in {
      targetJson(Stage("stage")) shouldEqual """{"Stage":"stage"}"""
    }

    "produces valid JSON for an App" in {
      targetJson(App("app")) shouldEqual """{"App":"app"}"""
    }

    "produces valid JSON for an AwsAccount" in {
      targetJson(AwsAccount("123456789")) shouldEqual """{"AwsAccount":"123456789"}"""
    }
  }

  "actionsJson" - {
    "produces correct JSON for an action" in {
      actionsJson(Action("text", "https://example.com")) shouldEqual """{"cta":"text","url":"https://example.com"}"""
    }
  }

  "messageJson" - {
    "TODO" in {
      fail("TODO: implement tests for message JSON")
    }
  }
}
