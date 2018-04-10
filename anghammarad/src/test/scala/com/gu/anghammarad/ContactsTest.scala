package com.gu.anghammarad

import com.gu.anghammarad.Contacts._
import com.gu.anghammarad.models._
import com.gu.anghammarad.testutils.TryValues
import org.scalatest.{FreeSpec, Matchers}

class ContactsTest extends FreeSpec with Matchers with TryValues {
  val email = EmailMessage("subject", "text", "html")
  val emailAddress = EmailAddress("test@example.com")
  val hangoutMessage = HangoutMessage("json")
  val hangoutsRoom = HangoutsRoom("webhook")

  "resolveTargetContacts" - {
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
        val targets = List(Stack("stack"), Stage("stage"), App("app"))
        val mappings = List(
          Mapping(List(Stack("stack"), Stage("stage"), App("app")), List(emailAddress))
        )
        resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
      }

      "chooses a complex exact match over a simple partial match" in {
        val targets = List(Stack("stack"), Stage("stage"), App("app"))
        val mappings = List(
          Mapping(List(Stack("stack"), Stage("stage"), App("app")), List(emailAddress)),
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
          Mapping(List(Stack("stack"), App("app2")), List(emailAddress))
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
        "chooses app over stack" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app1"))
          val mappings = List(
            Mapping(List(App("app1")), List(emailAddress)),
            Mapping(List(Stack("stack")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses app over AWS account" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app1"))
          val mappings = List(
            Mapping(List(App("app1")), List(emailAddress)),
            Mapping(List(AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses just app over a mapping with both stack and AWS account" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app1"))
          val mappings = List(
            Mapping(List(App("app1")), List(emailAddress)),
            Mapping(List(Stack("stack"), AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "chooses partial match with app over partial match with same number of less specific targets" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app1"))
          val mappings = List(
            Mapping(List(App("app1"), Stack("stack")), List(emailAddress)),
            Mapping(List(Stack("stack"), AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "does not resolve a partial app match if another target conflicts" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app1"))
          val mappings = List(
            Mapping(List(Stack("stack2"), App("app1")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
        }

        "falls back to less specific partial match where app matches but other targets conflict" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app1"))
          val mappings = List(
            Mapping(List(Stack("stack1"), AwsAccount("123456789")), List(emailAddress)),
            Mapping(List(Stack("stack2"), App("app1")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "stack is more specific than account, less than app" - {
        "chooses stack over AWS account" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app1"))
          val mappings = List(
            Mapping(List(Stack("stack")), List(emailAddress)),
            Mapping(List(AwsAccount("123456789")), List(hangoutsRoom))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }

        "does not resolve a partial stack match if another target conflicts" in {
          val targets = List(AwsAccount("123456789"), Stack("stack"), App("app1"))
          val mappings = List(
            Mapping(List(AwsAccount("111111111"), Stack("stack")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
        }

        "falls back to less specific partial match where stack matches but other targets conflict" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app1"))
          val mappings = List(
            Mapping(List(AwsAccount("123456789")), List(hangoutsRoom)),
            Mapping(List(Stack("stack1"), AwsAccount("111111111")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
        }
      }

      "AWS Account is less specific than app and stack" - {
        "does not resolve a partial AWS Account match if another target conflicts" in {
          val targets = List(AwsAccount("123456789"), Stack("stack1"), App("app1"))
          val mappings = List(
            Mapping(List(AwsAccount("123456789"), Stack("stack2")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
        }
      }
    }

    "handles stage correctly" - {
      "empty stage is assumed to be PROD" - {
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

      "if a non-empty non-PROD stage is targeted, mappings without that stage will not be matched" - {
        "fails to match if there is no matching non-PROD stage" in {
          val targets = List(Stack("stack"), Stage("stage"))
          val mappings = List(
            Mapping(List(Stack("stack")), List(emailAddress))
          )
          resolveTargetContacts(targets, mappings).isFailure shouldBe true
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

    "fails to resolve a complex partially missing target" in {
      val targets = List(Stack("stack"), Stage("PROD"), App("app1"))
      val mappings = List(
        Mapping(List(Stack("stack"), App("app1")), List(emailAddress))
      )
      resolveTargetContacts(targets, mappings).isFailure shouldEqual true
    }

    "fails to resolve partially missing target (doubly so when the partial matches are ambiguous)" in {
      val targets = List(Stack("stack"), Stage("PROD"), App("app1"))
      val mappings = List(
        Mapping(List(Stack("stack"), App("app1")), List(emailAddress)),
        Mapping(List(Stack("stack"), Stage("PROD")), List(emailAddress))
      )
      resolveTargetContacts(targets, mappings).isFailure shouldEqual true
    }
  }

  "resolveContactsForChannels" - {
    "if no contacts are provided, finds no channels" in {
      resolveContactsForChannels(Nil, All) shouldEqual Nil
    }

    "does not find a contact from a different channel" in {
      resolveContactsForChannels(List(emailAddress), HangoutsChat) shouldEqual Nil
    }

    "finds a contact from a relevant channel" in {
      resolveContactsForChannels(List(emailAddress), Email) shouldEqual List(Email -> emailAddress)
    }

    "if multiple channels are requested, returns as many as it can" in {
      resolveContactsForChannels(List(emailAddress), All) shouldEqual List(Email -> emailAddress)
    }

    "returns all matching contacts when All is requested" in {
      resolveContactsForChannels(List(emailAddress, hangoutsRoom), All) shouldEqual List(Email -> emailAddress, HangoutsChat -> hangoutsRoom)
    }
  }

  "contactsForMessages" - {
    "returns an empty list if there were no contacts and no messages" in {
      contactsForMessages(Nil, Nil).success shouldEqual Nil
    }

    "returns a failure if we could not find a contact for a message" in {
      contactsForMessages(List(Email -> email), Nil).isFailure shouldEqual true
    }

    "returns a failure if we could only find a contact for one channel" in {
      contactsForMessages(
        List(Email -> email, HangoutsChat -> hangoutMessage),
        List(Email -> emailAddress)
      ).isFailure shouldEqual true
    }

    "returns the contact for a message, if present" in {
      val result = contactsForMessages(List(Email -> email), List(Email -> emailAddress)).success
      result shouldEqual List(email -> emailAddress)
    }

    "returns the contact for the correct channel, if multiple are present" in {
      val result = contactsForMessages(List(Email -> email), List(HangoutsChat -> hangoutsRoom, Email -> emailAddress)).success
      result shouldEqual List(email -> emailAddress)
    }

    "matches multiple messages with their contacts" in {
      val result = contactsForMessages(
        List(Email -> email, HangoutsChat -> hangoutMessage),
        List(HangoutsChat -> hangoutsRoom, Email -> emailAddress)
      ).success
      result shouldEqual List(email -> emailAddress, hangoutMessage -> hangoutsRoom)
    }
  }
}
