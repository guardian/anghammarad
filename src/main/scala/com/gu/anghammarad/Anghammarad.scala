package com.gu.anghammarad

import com.gu.anghammarad.models._
import scala.util.Try


object Anghammarad {
  def run(notification: Notification, config: List[Mapping]): Try[Unit] = {
    // resolve targets
    val contacts: List[Contact] = Contacts.resolveTargetContacts(notification.target, config)
    // get contacts for desired channels (if possible)
    val channelContacts: List[(Channel, Contact)] = Contacts.resolveContactsForChannels(contacts, notification.channel)
    // make messages
    val channelMessages: List[(Channel, Message)] = Messages.channelMessages(notification)
    // find contacts for each message
    val toSend: List[(Contact, Message)] = Contacts.contactsForMessages(channelContacts, channelMessages)

    SendMessages.sendAll(toSend)

    ???
  }
}
