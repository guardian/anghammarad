package com.gu.anghammarad

import com.gu.anghammarad.models._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers


class TargetsTest extends AnyFreeSpec with Matchers {
  import com.gu.anghammarad.Targets._

  "includesAwsAccount" - {
    "returns false if AwsAccount is enquired about and not present" in {
      includesAwsAccount(List(App("app"))) shouldBe false
    }

    "returns true if AwsAccount is enquired about and present" in {
      includesAwsAccount(List(AwsAccount("123456789"), App("app"))) shouldBe true
    }
  }

  "includesStack" - {
    "returns false if Stack is enquired about and not present" in {
      includesStack(List(App("app"))) shouldBe false
    }

    "returns true if Stack is enquired about and present" in {
      includesStack(List(Stack("stack"), App("app"))) shouldBe true
    }
  }

  "includesApp" - {
    "returns false if App is enquired about and not present" in {
      includesApp(List(Stack("stack"))) shouldBe false
    }

    "returns true if App is enquired about and present" in {
      includesApp(List(App("app"), Stack("stack"))) shouldBe true
    }
  }

  "includesStage" - {
    "returns false if Stage is enquired about and not present" in {
      includesStage(List(App("app"))) shouldBe false
    }

    "returns true if Stage is enquired about and present" in {
      includesStage(List(Stage("stage"), App("app"))) shouldBe true
    }
  }

  "stageMatches" - {
    "returns true if the stages match" in {
      stageMatches(List(Stage("PROD")), List(Stage("PROD"))) shouldBe true
    }

    "returns true if the stages match among other targets" in {
      stageMatches(List(App("app"), Stage("PROD")), List(Stage("PROD"), Stack("stack"))) shouldBe true
    }

    "returns false if the stages do not match" in {
      stageMatches(List(Stage("stage")), List(Stage("PROD"))) shouldBe false
    }

    "returns false if the stages do not match among other targets" in {
      stageMatches(List(App("app"), Stage("stage")), List(Stage("PROD"), Stack("stack"))) shouldBe false
    }

    "returns true if multiple stages are present as long as there is an overlap" in {
      stageMatches(List(Stage("stage1"), Stage("stage2")), List(Stage("stage1"))) shouldBe true
    }
  }

  "awsAccountMatches" - {
    "returns true if the AWS Accounts match" in {
      awsAccountMatches(List(AwsAccount("123456789")), List(AwsAccount("123456789"))) shouldBe true
    }

    "returns true if the AWS Accounts match among other targets" in {
      awsAccountMatches(List(App("app"), AwsAccount("123456789")), List(AwsAccount("123456789"), Stack("stack"))) shouldBe true
    }

    "returns false if the AWS Accounts do not match" in {
      awsAccountMatches(List(AwsAccount("111111111")), List(AwsAccount("123456789"))) shouldBe false
    }

    "returns false if the AWS Accounts do not match among other targets" in {
      awsAccountMatches(List(App("app"), Stage("stage")), List(AwsAccount("123456789"), Stack("stack"))) shouldBe false
    }

    "returns true if multiple AWS Accounts are present as long as there is an overlap" in {
      awsAccountMatches(List(AwsAccount("123456789"), AwsAccount("111111111")), List(AwsAccount("123456789"))) shouldBe true
    }
  }

  "stackMatches" - {
    "returns true if the Stacks match" in {
      stackMatches(List(Stack("stack")), List(Stack("stack"))) shouldBe true
    }

    "returns true if the Stacks match among other targets" in {
      stackMatches(List(App("app"), Stack("stack")), List(AwsAccount("123456789"), Stack("stack"))) shouldBe true
    }

    "returns false if the Stacks do not match" in {
      stackMatches(List(Stack("stack1")), List(Stack("stack2"))) shouldBe false
    }

    "returns false if the Stacks do not match among other targets" in {
      stackMatches(List(App("app"), Stack("stack1")), List(Stack("stack2"), AwsAccount("123456789"))) shouldBe false
    }

    "returns true if multiple Stacks are present as long as there is an overlap" in {
      stackMatches(List(Stack("stack1"), Stack("stack2")), List(Stack("stack1"))) shouldBe true
    }
  }

  "appMatches" - {
    "returns true if the Apps match" in {
      appMatches(List(App("app")), List(App("app"))) shouldBe true
    }

    "returns true if the Apps match among other targets" in {
      appMatches(List(App("app"), Stack("stack")), List(AwsAccount("123456789"), App("app"))) shouldBe true
    }

    "returns false if the Apps do not match" in {
      appMatches(List(App("app1")), List(App("app2"))) shouldBe false
    }

    "returns false if the Apps do not match among other targets" in {
      appMatches(List(Stack("stack"), App("app1")), List(App("app2"), AwsAccount("123456789"))) shouldBe false
    }

    "returns true if multiple Apps are present as long as there is an overlap" in {
      appMatches(List(App("app1"), App("app2")), List(App("app1"))) shouldBe true
    }
  }

  "githubTeamSlugMatches" - {
    "returns true if the Slugs match" in {
      githubTeamSlugMatches(List(GithubTeamSlug("slugs")), List(GithubTeamSlug("slugs"))) shouldBe true
    }

    "returns true if the Slugs match among other targets" in {
      githubTeamSlugMatches(List(GithubTeamSlug("slugs"), Stack("stack")), List(AwsAccount("123456789"), GithubTeamSlug("slugs"))) shouldBe true
    }

    "returns false if the Slugs do not match" in {
      githubTeamSlugMatches(List(GithubTeamSlug("slugs1")), List(GithubTeamSlug("slugs2"))) shouldBe false
    }

    "returns false if the Slugs do not match among other targets" in {
      githubTeamSlugMatches(List(Stack("stack"), GithubTeamSlug("slugs1")), List(GithubTeamSlug("slugs2"), AwsAccount("123456789"))) shouldBe false
    }

    "returns true if multiple Slugs are present as long as there is an overlap" in {
      githubTeamSlugMatches(List(GithubTeamSlug("slugs1"), GithubTeamSlug("slugs2")), List(GithubTeamSlug("slugs1"))) shouldBe true
    }
  }

  "sortMappingsByTargets" - {
    val expected = List(EmailAddress("expected"))
    val unexpected = List(EmailAddress("unexpected"))

    "matching app goes before matching stage and aws account" in {
      val targets = List(App("app"), Stack("stack"), AwsAccount("123456789"))
      val mappings = List(
        Mapping(List(App("app")), expected),
        Mapping(List(Stack("stack"), AwsAccount("123456789")), unexpected)
      )
      sortMappingsByTargets(targets, mappings).head.contacts shouldEqual expected
    }

    "non-matching app goes after matching stage and aws account" in {
      val targets = List(App("app1"), Stack("stack"), AwsAccount("123456789"))
      val mappings = List(
        Mapping(List(App("app2")), unexpected),
        Mapping(List(Stack("stack"), AwsAccount("123456789")), expected)
      )
      sortMappingsByTargets(targets, mappings).head.contacts shouldEqual expected
    }

    "matching stack goes before matching aws account" in {
      val targets = List(Stack("stack"), AwsAccount("123456789"))
      val mappings = List(
        Mapping(List(Stack("stack")), expected),
        Mapping(List(AwsAccount("123456789")), unexpected)
      )
      sortMappingsByTargets(targets, mappings).head.contacts shouldEqual expected
    }

    "non-matching stack goes after matching aws account" in {
      val targets = List(Stack("stack1"), AwsAccount("123456789"))
      val mappings = List(
        Mapping(List(Stack("stack2")), unexpected),
        Mapping(List(AwsAccount("123456789")), expected)
      )
      sortMappingsByTargets(targets, mappings).head.contacts shouldEqual expected
    }

    "PROD stage in mappings determines priority if two otherwise equivalent matches are found" in {
      val targets = List(App("app"))
      val mappings = List(
        Mapping(List(App("app"), Stage("PROD")), expected),
        Mapping(List(App("app"), Stage("CODE")), unexpected),
      )
      sortMappingsByTargets(targets, mappings).head.contacts shouldEqual expected
    }

    "Lack of stage in mappings gets priority over a specific CODE mapping if two otherwise equivalent matches are found" in {
      val targets = List(App("app"))
      val mappings = List(
        Mapping(List(App("app")), expected),
        Mapping(List(App("app"), Stage("CODE")), unexpected),
      )
      sortMappingsByTargets(targets, mappings).head.contacts shouldEqual expected
    }

  }
}
