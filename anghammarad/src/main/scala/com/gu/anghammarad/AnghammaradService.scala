package com.gu.anghammarad

import com.gu.anghammarad.common.Contacts
import com.gu.anghammarad.models.{
  Channel,
  Configuration,
  Contact,
  Message,
  Notification
}
import com.gu.anghammarad.messages.{Messages, SendMessages}

import scala.util.{Failure, Success, Try}

object AnghammaradService {

  def lookupContacts(notification: Notification, config: Configuration) = for {
    contacts <- Contacts.resolveTargetContacts(
      notification.target,
      config.mappings
    )
    // get contacts for desired channels (if possible)
    channelContacts <- Contacts.resolveContactsForChannels(
      contacts,
      notification.channel
    )
    // find contacts for each message
    contacts <- Contacts.contactsForMessage(
      notification.channel,
      channelContacts
    )
  } yield contacts

  def run(
      notification: Notification,
      config: Configuration
  ): Try[List[(Message, Contact)]] = {
    val fallbackContacts: List[(Channel, Contact)] = ???
    val messageWithContact = lookupContacts(notification, config) match {
      case Success(contacts) =>
        Messages.createMessages(
          notification,
          contacts
        )
      case Failure(_) =>
        Messages.createMessages(
          notification.copy(subject =
            "Failed to deliver the following message due to a problem with Anghammarad's config"
          ),
          fallbackContacts
        )
    }

    for {
      result <- SendMessages.sendAll(config, messageWithContact)
    } yield messageWithContact
  }
}
