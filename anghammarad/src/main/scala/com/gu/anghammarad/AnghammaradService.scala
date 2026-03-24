package com.gu.anghammarad

import com.gu.anghammarad.common.Contacts
import com.gu.anghammarad.models._
import com.gu.anghammarad.messages.{Messages, SendMessages}

import scala.util.{Failure, Success, Try}

object AnghammaradService {
  def run(
      notification: Notification,
      config: Configuration
  ): Try[List[(Message, Contact)]] = {
    val toSend = Contacts.lookupContacts(
      notification.target, notification.channel, config.mappings
    ) match {
      case Success(contacts) =>
        Success(Messages.createMessages(notification, contacts))
      case Failure(_) =>
        val fallbackNotification = notification.copy(
          subject = "Anghammarad failed to deliver a notification",
          message = Messages.failureMessage(notification)
        )
        Contacts.lookupContacts(
          List(App("anghammarad")), HangoutsChat, config.mappings
        ).map(contacts => Messages.createMessages(fallbackNotification, contacts))
    }

    for {
      messages <- toSend
      _ <- SendMessages.sendAll(config, messages)
    } yield messages
  }
}
