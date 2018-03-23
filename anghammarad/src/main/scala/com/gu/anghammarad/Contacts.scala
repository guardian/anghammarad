package com.gu.anghammarad

import com.gu.anghammarad.common.AnghammaradException.Fail
import com.gu.anghammarad.common.models.{Contact, Email, EmailAddress, HangoutsChat, HangoutsRoom, Mapping, Message, _}

import scala.util.{Success, Try}


object Contacts {
  /**
    * Gets all available contacts for this target, from configuration.
    */
  def resolveTargetContacts(targets: List[Target], mappings: List[Mapping]): Try[List[Contact]] = {
    mappings.filter(_.targets == targets) match {
      case Nil =>
        mappings.filter { case Mapping(mappingTargets, _) =>
          targets.toSet subsetOf mappingTargets.toSet
        } match {
          case Nil =>
            Fail(s"No contacts found for $targets")
          case exactMatch :: Nil =>
            Success(exactMatch.contacts)
          case _ =>
            Fail(s"Cannot resolve contacts from ambiguous partial matches for $targets")
        }
      case exactMatch :: Nil =>
        Success(exactMatch.contacts)
      case _ =>
        Fail(s"Found multiple exact matches while resolving contacts for $targets")
    }
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
    if (resolved.size < channelMessages.size) {
      Fail(s"Could not find contacts for messages on the requested channels messages($channelMessages), contacts($channelContacts)")
    } else {
      Success(resolved)
    }
  }
}
