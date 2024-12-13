package com.gu.anghammarad

import com.gu.anghammarad.common.Contacts
import com.gu.anghammarad.models.{Configuration, Contact, Message, Notification}
import com.gu.anghammarad.messages.{Messages, SendMessages}

import scala.util.Try


object AnghammaradService {
  def run(notification: Notification, config: Configuration, anghammaradStage: String): Try[List[(Message, Contact)]] = {
    // resolve targets
    for {
      contacts <- Contacts.resolveTargetContacts(notification.target, config.mappings)
      // get contacts for desired channels (if possible)
      channelContacts <- Contacts.resolveContactsForChannels(contacts, notification.channel)
      // find contacts for each message
      contacts <- Contacts.contactsForMessage(notification.channel, channelContacts)
      // address messages
      toSend = Messages.createMessages(notification, contacts, anghammaradStage)
      // send resolved notifications
      result <- SendMessages.sendAll(config, toSend)
    } yield toSend
  }
}
