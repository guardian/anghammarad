package com.gu.anghammarad

import com.gu.anghammarad.common.Contacts
import com.gu.anghammarad.models.{App, Channel, Configuration, Contact, HangoutsChat, Message, Notification}
import com.gu.anghammarad.messages.{Messages, SendMessages}

import scala.util.Try

object AnghammaradService {
  private val FALLBACK_TARGET = List(App("anghammarad"))
  private val FALLBACK_CHANNEL = HangoutsChat
  private val FALLBACK_SUBJECT = "Anghammarad failed to deliver a notification"

  def run(
      notification: Notification,
      config: Configuration
  ): Try[List[(Message, Contact)]] = {
    for {
      (notificationToSend, contacts) <- lookupContactsAndNotification(
        notification,
        config
      )
      toSend = Messages.createMessages(notificationToSend, contacts)
      _ <- SendMessages.sendAll(config, toSend)
    } yield toSend
  }

  private def lookupContactsAndNotification(
      originalNotification: Notification,
      config: Configuration
  ): Try[(Notification, List[(Channel, Contact)])] = {
    Contacts
      .lookupContacts(
        targets = originalNotification.target,
        requestedChannel = originalNotification.channel,
        mappings = config.mappings
      )
      .map(contacts => originalNotification -> contacts)
      .recoverWith { case _ =>
        Contacts
          .lookupContacts(
            targets = FALLBACK_TARGET,
            requestedChannel = FALLBACK_CHANNEL,
            mappings = config.mappings
          )
          .map(fallbackContacts => fallbackNotification(originalNotification) -> fallbackContacts)
      }
  }

  private def fallbackNotification(
      originalNotification: Notification
  ): Notification = {
    originalNotification.copy(
      subject = FALLBACK_SUBJECT,
      message = failureMessage(originalNotification)
    )
  }

  private def failureMessage(originalNotification: Notification): String = {
    val missingTargets = originalNotification.target.mkString(", ")

    s"""
       |The [config](https://github.com/guardian/anghammarad-config) is missing information for the following targets: $missingTargets.
       |
       |**Requested channel**:
       |${originalNotification.channel}
       |
       |**Subject**:
       |${originalNotification.subject}
       |
       |**Body**:
       |${originalNotification.message}
       |""".stripMargin
  }
}
