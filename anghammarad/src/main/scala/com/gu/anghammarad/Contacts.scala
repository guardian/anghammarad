package com.gu.anghammarad

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.models._

import scala.util.{Success, Try}


object Contacts {
  /**
    * Gets all available contacts for this target, from configuration.
    */
  def resolveTargetContacts(target: List[Target], config: List[Mapping]): Try[List[Contact]] = {
    ???
  }

  /**
    * Attempts to find contacts for each requested channel.
    */
  def resolveContactsForChannels(contacts: List[Contact], requestedChannel: RequestedChannel): List[(Channel, Contact)] = {
    requestedChannel match {
      case Email =>
        contacts.collect {
          case ea: EmailAddress => Email -> ea
        }
      case HangoutsChat =>
        contacts.collect {
          case hr: HangoutsRoom => HangoutsChat -> hr
        }
      case All =>
        contacts.collect {
          case ea: EmailAddress => Email -> ea
          case hr: HangoutsRoom => HangoutsChat -> hr
        }
    }
  }

  /**
    * Finds a contact (from provided available targets) for each message.
    */
  def contactsForMessages(channelMessages: List[(Channel, Message)], channelContacts: List[(Channel, Contact)]): Try[List[(Message, Contact)]] = {
    val resolved = channelMessages.flatMap { case (messageChannel, message) =>
      for {
        (_, contact) <- channelContacts.find { case (contactChannel, _) =>
          contactChannel == messageChannel
        }
      } yield message -> contact
    }
    if (resolved.size < channelMessages.size)
      Fail(s"Could not find contacts for messages on the requested channels messages($channelMessages), contacts($channelContacts)")
    else
      Success(resolved)
  }
}
