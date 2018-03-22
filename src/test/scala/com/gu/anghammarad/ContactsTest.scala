package com.gu.anghammarad

import org.scalatest.{EitherValues, FreeSpec, Matchers}
import Contacts._
import com.gu.anghammarad.testutils.TryValues
import models._


class ContactsTest extends FreeSpec with Matchers with TryValues {
  val email = EmailMessage("subject", "text", "html")
  val emailAddress = EmailAddress("test@example.com")
  val hangoutMessage = HangoutMessage("json")
  val hangoutsRoom = HangoutsRoom("webhook")

  "resolveTargetContacts" - {
    "resolves an exact match" in {
      val targets = List(App("app"))
      val mappings = List(Mapping(
        List(App("app")),
        List(emailAddress)
      ))
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

    "finds multiple contacts for an exact match" in {
      val targets = List(App("app"))
      val mappings = List(Mapping(
        List(App("app")),
        List(emailAddress, hangoutsRoom)
      ))
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

    "chooses exact match over a partial match" in {
      val targets = List(App("app1"))
      val mappings = List(
        Mapping(List(App("app1")), List(emailAddress)),
        Mapping(List(Stack("stack1"), App("app1")), List(hangoutsRoom))
      )
      resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
    }

    "resolves a more complex exact match" in {
      val targets = List(Stack("stack"), Stage("stage"), App("app"))
      val mappings = List(Mapping(
        List(Stack("stack"), Stage("stage"), App("app")),
        List(emailAddress)
      ))
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

    "resolves a partial match" in {
      val targets = List(App("app"))
      val mappings = List(
        Mapping(List(Stack("stack"), App("app")), List(emailAddress))
      )
      resolveTargetContacts(targets, mappings).success shouldEqual List(emailAddress)
    }

    "resolves a complex partial match" in {
      val targets = List(Stack("stack"), App("app"))
      val mappings = List(
        Mapping(List(Stack("stack"), App("app"), AwsAccount("123456789")), List(emailAddress))
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

    "fails to resolve from ambiguous complex partial matches" in {
      val targets = List(Stack("stack"), Stage("PROD"))
      val mappings = List(
        Mapping(List(Stack("stack"), Stage("PROD"), App("app1")), List(emailAddress)),
        Mapping(List(Stack("stack"), Stage("PROD"), App("app2")), List(emailAddress))
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

    "fails to resolve a partially missing target" in {
      val targets = List(Stack("stack"), App("app1"))
      val mappings = List(
        Mapping(List(App("app1")), List(emailAddress))
      )
      resolveTargetContacts(targets, mappings).isFailure shouldEqual true
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
