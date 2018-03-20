package com.gu.anghammarad

import scala.util.Try


object Logic {
  def resolveContact(target: Target, config: List[Mapping]): List[Contact] = {
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
