package com.gu.anghammarad

import com.gu.anghammarad.Contacts._
import com.gu.anghammarad.models._
import com.gu.anghammarad.serialization.Serialization
import com.gu.anghammarad.testutils.TryValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source
import scala.util.Try


class ContactsTest extends AnyFreeSpec with Matchers with TryValues {
  val email = EmailMessage("subject", "text", "html")
  val emailAddress = EmailAddress("test@example.com")
  val hangoutMessage = HangoutMessage("json", None)
  val hangoutsRoom = HangoutsRoom("webhook")

  "resolveTargetContacts" - {
    // checks that contacts resolution still works in realistic scenarios
    // implementation details are covered in the following tests
    "integration tests with larger mappings" - {
      val integrationConfigStr = Source.fromURL(getClass.getResource("/contacts-integration-fixture.json")).mkString
      val mappings = Serialization.parseAllMappings(integrationConfigStr).success

      "finds exact match among mappings" in {
        val targets = List(AwsAccount("123456789"), App("app2"), Stack("stack2"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("app2.email"))
      }

      "chooses correct match for sparsely-requested targets" in {
        val targets = List(App("app2"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("app2.email"))
      }

      "uses most specific match among multiple choices (matches mapping with stack and app, not the one with just app)" in {
        val targets = List(AwsAccount("123456789"), Stack("stack2"), App("app1"), Stage("PROD"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("stack2.app1.email"))
      }

      "chooses correct app match for specific target where app matches (even if stack and account are separately configured)" in {
        val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app1"), Stage("PROD"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("app1.email"), HangoutsRoom("app1.channel"))
      }

      "matches stack from more specific target" in {
        val targets = List(AwsAccount("foo"), Stack("stack1"), App("foo"), Stage("PROD"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("stack1.email"), HangoutsRoom("stack1.channel"))
      }

      "chooses correct stack match for specific target if stack is configured but app is not" in {
        val targets = List(AwsAccount("123456789"), Stack("stack1"), App("different-app"), Stage("PROD"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("stack1.email"), HangoutsRoom("stack1.channel"))
      }

      "chooses correct AWS Account match for specific target if AWS Account is configured but stack and app are not" in {
        val targets = List(AwsAccount("123456789"), Stack("different-stack"), App("different-app"), Stage("PROD"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("123456789.email"))
      }

      "chooses correct GithubTeamSlug match for specific target if GithubTeamSlug is configured" in {
        val targets = List(GithubTeamSlug("slug1"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("app2.CODE.email"))
      }

      "chooses correct GithubTeamSlug match for specific target if GithubTeamSlug is configured but stack and app are not" in {
        val targets = List(GithubTeamSlug("slug1"), Stack("different-stack"), App("different-app"), Stage("PROD"))
        resolveTargetContacts(targets, mappings).success shouldEqual List(EmailAddress("app2.CODE.email"))
      }

      "does not choose GithubTeamSlug when another exact mapping is provided" in {
        val stackStageAppTargets = List(GithubTeamSlug("slug4"), Stack("stack4"), App("app4"), Stage("PROD4"))
        val accountIdTarget = List(GithubTeamSlug("slug4"), AwsAccount("111111111"))
        val exactStackTarget = List(GithubTeamSlug("slug4"), Stack("stack1"))
        resolveTargetContacts(stackStageAppTargets, mappings).success shouldEqual List(EmailAddress("stack4.email"))
        resolveTargetContacts(accountIdTarget, mappings).success shouldEqual List(EmailAddress("111111111.email"))
        resolveTargetContacts(exactStackTarget, mappings).success shouldEqual List(EmailAddress("stack1.email"), HangoutsRoom("stack1.channel"))
      }
      "chooses GithubTeamSlug when an alternative, underspecified match is provided" in {
        val stackTargets = List(GithubTeamSlug("slug4"), Stack("stack4"))
        resolveTargetContacts(stackTargets, mappings).success shouldEqual List(EmailAddress("slug4.email"))
      }
    }

    "cannot resolve from empty mappings" in {
      val targets = List(Stack("stack"))
      val mappings = Nil
      resolveTargetContacts(targets, mappings).isFailure shouldEqual true
    }

    "fails to resolve a missing target" in {
      val targets = List(AwsAccount("111111111"))
      val mappings = List(
        Mapping(List(Stack("stack"), App("app1")), List(emailAddress)),
        Mapping(List(Stack("stack"), App("app2")), List(emailAddress)),
        Mapping(List(AwsAccount("123456789")), List(emailAddress))
      )
      resolveTargetContacts(targets, mappings).isFailure shouldEqual true
    }

    "chooses an exact match, if it exists" - {
      "chooses exact app match" in {
        val targets = List(App("app"))
        val mappings = List(
          Mapping(List(App("app")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "finds multiple contacts for an exact match" in {
        val targets = List(App("app"))
        val mappings = List(
          Mapping(List(App("app")), List(emailAddress, hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress, hangoutsRoom)
      }

      "finds exact match among other mappings" in {
        val targets = List(App("app1"))
        val mappings = List(
          Mapping(List(App("app1")), List(emailAddress)),
          Mapping(List(Stack("stack1"), App("app2")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "chooses exact app match over a partial match" in {
        val targets = List(App("app1"))
        val mappings = List(
          Mapping(List(App("app1")), List(emailAddress)),
          Mapping(List(Stack("stack1"), App("app1")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "chooses exact stack match over a partial match" in {
        val targets = List(Stack("stack1"))
        val mappings = List(
          Mapping(List(Stack("stack1")), List(emailAddress)),
          Mapping(List(Stack("stack1"), App("app1")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "chooses exact AWS account match over a partial match" in {
        val targets = List(AwsAccount("123456789"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789")), List(emailAddress)),
          Mapping(List(AwsAccount("123456789"), App("app1")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "resolves a more complex exact match" in {
        val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "chooses a complex exact match over a simple partial match" in {
        val targets = List(Stack("stack"), App("app"))
        val mappings = List(
          Mapping(List(Stack("stack"), App("app")), List(emailAddress)),
          Mapping(List(App("app")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "fails to resolve ambiguous exact matches" in {
        val targets = List(App("app"))
        val mappings = List(
          Mapping(List(App("app")), List(emailAddress)),
          Mapping(List(App("app")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).isFailure shouldEqual true
      }
    }

    "when targets are under-specified, searches for a match among more specific mappings" - {
      "resolves a partial match" in {
        val targets = List(App("app"))
        val mappings = List(
          Mapping(List(Stack("stack"), App("app")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "resolves a more complex partial match" in {
        val targets = List(Stack("stack"), App("app"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "will not choose a mapping with a more specific requirement than has been targeted (app)" in {
        val targets = List(AwsAccount("123456789"), Stack("stack"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).isFailure shouldBe true
      }

      "will not choose a mapping with a more specific requirement than has been targeted (stack)" in {
        val targets = List(AwsAccount("123456789"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).isFailure shouldBe true
      }

      "fails to resolve from ambiguous partial matches" in {
        val targets = List(Stack("stack"))
        val mappings = List(
          Mapping(List(Stack("stack"), App("app1")), List(emailAddress)),
          Mapping(List(Stack("stack"), App("app2")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).isFailure shouldEqual true
      }

      "resolves ambiguous incomplete partial matches where target hierarchy can disambiguate mappings" in {
        val targets = List(Stack("stack"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack")), List(emailAddress)),
          Mapping(List(Stack("stack"), App("app")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "fails to resolve from ambiguous partial matches with equal specificity" in {
        val targets = List(AwsAccount("123456789"), Stack("stack"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app1")), List(emailAddress)),
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app2")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).isFailure shouldEqual true
      }

      "fails to resolve from ambiguous partial matches of different complexity (does not pick the closest match)" in {
        val targets = List(Stack("stack"))
        val mappings = List(
          Mapping(List(Stack("stack"), App("app1")), List(emailAddress)),
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app2")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).isFailure shouldEqual true
      }
    }

    "when target is over-specified, uses hierarchy of targets to identify a match from less specific mappings" - {
      "app is most specific then both stack and Aws Account" - {
        "selects a targeted app" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(App("app")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses app over stack" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(App("app")), List(emailAddress)),
            Mapping(List(Stack("stack")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses app over AWS account" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(App("app")), List(emailAddress)),
            Mapping(List(AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses just app over a mapping with both stack and AWS account" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(App("app")), List(emailAddress)),
            Mapping(List(Stack("stack"), AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses partial match with app over partial match with same number of less specific targets" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(App("app"), Stack("stack")), List(emailAddress)),
            Mapping(List(Stack("stack"), AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "does not resolve a partial app match if another target conflicts" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app"))
          val mappings = List(
            Mapping(List(Stack("stack2"), App("app")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
        }

        "falls back to less specific partial match where app matches but other targets conflict" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app"))
          val mappings = List(
            Mapping(List(Stack("stack1"), AwsAccount("123456789")), List(emailAddress)),
            Mapping(List(Stack("stack2"), App("app")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "stack is more specific than account, less than app" - {
        "selects a targeted stack" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(Stack("stack")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses stack over AWS account" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(Stack("stack")), List(emailAddress)),
            Mapping(List(AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "does not resolve a partial stack match if another target conflicts" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(AwsAccount("111111111"), Stack("stack")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
        }

        "falls back to less specific partial match where stack matches but other targets conflict" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(AwsAccount("123456789")), List(emailAddress)),
            Mapping(List(Stack("stack"), AwsAccount("111111111")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "AWS Account is less specific than app and stack" - {
        "does not resolve a partial AWS Account match if another target conflicts" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app"))
          val mappings = List(
            Mapping(List(AwsAccount("123456789"), Stack("stack2")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
        }
      }
    }

    "if targets are both over and under specified against the mappings" - {
      "will not choose a mapping with a more specific requirement than has been targeted" in {
        val targets = List(AwsAccount("123456789"), Stack("stack"))
        val mappings = List(
          Mapping(List(Stack("stack")), List(emailAddress)),
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "chooses the better match where that mapping only lacks less specific targets" in {
        val targets = List(Stack("stack"), App("app"))
        val mappings = List(
          Mapping(List(AwsAccount("123456789"), Stack("stack"), App("app")), List(emailAddress)),
          Mapping(List(App("app")), List(hangoutsRoom))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }
    }

    "handles stage correctly" - {
      "empty stage is assumed to be PROD" - {
        "resolves exact match with explicit stage" in {
          val targets = List(Stack("stack"), App("app"), Stage("stage"))
          val mappings = List(
            Mapping(List(Stack("stack"), App("app"), Stage("stage")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "resolves exact match with implicit PROD stage" in {
          val targets = List(Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(Stack("stack"), App("app")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "over-specified partial match with empty stage in target matches empty stage in mapping" in {
          val targets = List(Stack("stack"), App("app"))
          val mappings = List(
            Mapping(List(App("app")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "under-specified partial match with empty stage in target matches empty stage in mapping" in {
          val targets = List(App("app"))
          val mappings = List(
            Mapping(List(Stack("stack"), App("app")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "explicit PROD in target matches empty stage in mapping" in {
          val targets = List(Stack("stack"), Stage("PROD"))
          val mappings = List(
            Mapping(List(Stack("stack")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "empty stage in target matches explicit PROD in mapping" in {
          val targets = List(Stack("stack"))
          val mappings = List(
            Mapping(List(Stack("stack"), Stage("PROD")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "if a stage is targeted, matches exact mappings for that stage" - {
        "fails to match if there is no matching non-PROD stage" in {
          val targets = List(Stack("stack"), Stage("stage"))
          val mappings = List(
            Mapping(List(Stack("stack"), Stage("stage")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "if a stage is targeted, matches over-specified mappings for that stage" - {
        "fails to match if there is no matching non-PROD stage" in {
          val targets = List(Stack("stack"), App("app"), Stage("stage"))
          val mappings = List(
            Mapping(List(App("app"), Stage("stage")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "if a stage is targeted, matches under-specified mappings for that stage" - {
        "fails to match if there is no matching non-PROD stage" in {
          val targets = List(App("app"), Stage("stage"))
          val mappings = List(
            Mapping(List(Stack("stack"), App("app"), Stage("stage")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }
    }
  }

  "resolveContactsForChannels" - {
    "if no contacts are provided, fails because it cannot find any channels" in {
      resolveContactsForChannels(Nil, All).isFailure shouldBe true
    }

    "fails if a requested channel is not available" in {
      resolveContactsForChannels(List(emailAddress), HangoutsChat).isFailure shouldBe true
    }

    "finds a contact from a relevant channel" in {
      resolveContactsForChannels(List(emailAddress), Email).success shouldEqual List(Email -> emailAddress)
    }

    "if multiple channels are requested, returns as many as it can" in {
      resolveContactsForChannels(List(emailAddress), All).success shouldEqual List(Email -> emailAddress)
    }

    "returns all matching contacts when All is requested" in {
      resolveContactsForChannels(List(emailAddress, hangoutsRoom), All).success shouldEqual List(Email -> emailAddress, HangoutsChat -> hangoutsRoom)
    }

    "if email is preferred and present, returns email" in {
      resolveContactsForChannels(List(emailAddress, hangoutsRoom), Preferred(Email)).success shouldEqual List(Email -> emailAddress)
    }

    "if email is preferred and absent, returns webhook" in {
      resolveContactsForChannels(List(hangoutsRoom), Preferred(Email)).success shouldEqual List(HangoutsChat -> hangoutsRoom)
    }

    "if hangouts is preferred and present, returns webhook" in {
      resolveContactsForChannels(List(emailAddress, hangoutsRoom), Preferred(HangoutsChat)).success shouldEqual List(HangoutsChat -> hangoutsRoom)
    }

    "if webhook is preferred and absent, returns email" in {
      resolveContactsForChannels(List(emailAddress), Preferred(HangoutsChat)).success shouldEqual List(Email -> emailAddress)
    }
  }

  "contactsForMessage" - {
    "if there were no contacts and no messages it fails to find contacts and fails" in {
      contactsForMessage(All, Nil).isFailure shouldEqual true
    }

    "returns a failure if we could not find a contact for a message" in {
      contactsForMessage(Email, Nil).isFailure shouldEqual true
    }

    "returns a failure if the requested channel is not available" in {
      val result = contactsForMessage(HangoutsChat, List(Email -> emailAddress))
      result.isFailure shouldEqual true
    }

    "returns the contact for a message, if present" in {
      contactsForMessage(Email, List(Email -> emailAddress)).success shouldEqual List(Email -> emailAddress)
    }

    "returns the contact for the correct channel, if multiple are present" in {
      val result = contactsForMessage(Email, List(HangoutsChat -> hangoutsRoom, Email -> emailAddress)).success
      result shouldEqual List(Email -> emailAddress)
    }

    "matches multiple messages with their contacts" in {
      val result = contactsForMessage(All, List(HangoutsChat -> hangoutsRoom, Email -> emailAddress)).success
      result shouldEqual List(Email -> emailAddress, HangoutsChat -> hangoutsRoom)
    }
  }
}
