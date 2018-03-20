package com.gu.anghammarad

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
      val message = channelMessage(contact, notification)
      send(contact, message)
    }
    ???
  }

  def resolveContact(target: List[Target], config: Mapping): List[Contact] = {
    ???
  }

  def channelMessage(contact: Contact, notification: Notification): Message = {
    contact match {
      case Email(_) =>
        ???
      case HangoutsChat(_) =>
        ???
    }
  }

  def send(contact: Contact, message: Message): Try[Unit] = {
    contact match {
      case Email(address) =>
        ???
      case HangoutsChat(webhook) =>
        ???
    }
  }
}
