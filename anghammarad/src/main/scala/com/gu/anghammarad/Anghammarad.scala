package com.gu.anghammarad

import com.gu.anghammarad.models.{Configuration, Notification}
import com.gu.anghammarad.messages.{Messages, SendMessages}
import scala.util.Try


object Anghammarad {
  def run(notification: Notification, config: Configuration): Try[Unit] = {
    // resolve targets
    for {
      contacts <- Contacts.resolveTargetContacts(notification.target, config.mappings)
      // get contacts for desired channels (if possible)
      channelContacts = Contacts.resolveContactsForChannels(contacts, notification.channel)
      // make messages
      channelMessages = Messages.channelMessages(notification)
      // find contacts for each message
      toSend <- Contacts.contactsForMessages(channelMessages, channelContacts)
      // send resolved notifications
      result <- SendMessages.sendAll(config, toSend)
    } yield result
  }
}