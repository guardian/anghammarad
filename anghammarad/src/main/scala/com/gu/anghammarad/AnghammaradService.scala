package com.gu.anghammarad

import com.gu.anghammarad.common.Contacts
import com.gu.anghammarad.models.{Configuration, Contact, Message, Notification}
import com.gu.anghammarad.messages.{Messages, SendMessages}

import scala.util.Try

object AnghammaradService {
  def run(
      notification: Notification,
      config: Configuration
  ): Try[List[(Message, Contact)]] = {
    for {
      contactsOrFallback <- Contacts.lookupContactsWithFallback(
        notification.target,
        notification.channel,
        config.mappings
      )
      // address messages
      toSend = Messages.createMessagesWithFallback(
        notification,
        contactsOrFallback
      )
      // send resolved notifications
      result <- SendMessages.sendAll(config, toSend)
    } yield toSend
  }
}
