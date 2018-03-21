package com.gu.anghammarad

import com.amazonaws.services.simpleemail.model.SendEmailResult
import com.gu.anghammarad.models._
import scala.util.Try


object Anghammarad {
  def run(notification: Notification): Try[Unit] = {
    // parse input into Notification
    val notification: Notification = ???
    // resolve targets
    val contacts: List[Contact] = ???
    // DECIDE!!!
    val filteredContacts: List[Contact] = ???
    // send
    filteredContacts.map { contact =>
      val message = channelMessage(notification.channel, notification)
      send(contact, message)
    }
    ???
  }

  def resolveContact(target: List[Target], config: Mapping): List[Contact] = {
    ???
  }

  def channelMessage(channel: Channel, notification: Notification): Message = {
    channel match {
      case Email =>
        val contents = notification.message
        EmailMessage(notification.subject, contents, ???)
      case HangoutsChat =>
        HangoutMessage(???)
        ???
    }
  }

  def send(contact: Contact, message: Message): Try[SendEmailResult] = {
    contact match {
      case e: EmailAddress =>
        ???
      case HangoutsRoom(webhook) =>
        ???
    }
  }
}
