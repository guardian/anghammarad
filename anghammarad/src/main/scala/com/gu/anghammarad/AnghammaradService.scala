package com.gu.anghammarad

import com.gu.anghammarad.common.Contacts
import com.gu.anghammarad.models.{Configuration, Contact, Message, Mapping, Notification}
import com.gu.anghammarad.messages.{Messages, SendMessages}

import scala.util.Try

object AnghammaradService {

  def lookupContactsAndCreateMessages(notification: Notification, mappings: List[Mapping]): Try[List[(Message, Contact)]] = {
    for {
      // resolve targets
      contacts <- Contacts.resolveTargetContacts(notification.target, mappings)
      // get contacts for desired channels (if possible)
      channelContacts <- Contacts.resolveContactsForChannels(contacts, notification.channel)
      // find contacts for each message
      contacts <- Contacts.contactsForMessage(notification.channel, channelContacts)
      messages = Messages.createMessages(notification, contacts)
    } yield messages
  }

  def run(
      notification: Notification,
      config: Configuration
  ): Try[List[(Message, Contact)]] = {
    for {
      toSend <- lookupContactsAndCreateMessages(notification, config.mappings)
        .recoverWith { case err => lookupContactsAndCreateMessages(Messages.fallbackNotification(notification, err), config.mappings) }
      result <- SendMessages.sendAll(config, toSend)
    } yield toSend
  }
}
