package com.gu.anghammarad

import com.gu.anghammarad.models._
import scala.util.Try


object Anghammarad {
  def run(notification: Notification, config: List[Mapping]): Try[Unit] = {
    // resolve targets
    for {
      contacts <- Contacts.resolveTargetContacts(notification.target, config)
      // get contacts for desired channels (if possible)
      channelContacts = Contacts.resolveContactsForChannels(contacts, notification.channel)
      // make messages
      channelMessages = Messages.channelMessages(notification)
      // find contacts for each message
      toSend <- Contacts.contactsForMessages(channelMessages, channelContacts)
      // send resolved notifications
      result <- SendMessages.sendAll(toSend)
    } yield result
  }
}
