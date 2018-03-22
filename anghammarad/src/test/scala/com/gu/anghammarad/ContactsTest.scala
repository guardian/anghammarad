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
    // TODO think about how this should work
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
