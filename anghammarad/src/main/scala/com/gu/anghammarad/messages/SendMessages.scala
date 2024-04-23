package com.gu.anghammarad.messages

import com.gu.anghammarad.Enrichments._
import com.gu.anghammarad.common.AnghammaradException.Fail
import com.gu.anghammarad.models._

import scala.util.Try


object SendMessages {
  def sendAll(config: Configuration, toSend: List[(Message, Contact)]): Try[Unit] = {
    toSend.traverseT {
      case (message: EmailMessage, EmailAddress(address)) =>
        EmailService.sendEmail(config.emailSender, address, message)
      case (message: HangoutMessage, HangoutsRoom(webhook)) =>
        HangoutsService.sendHangoutsMessage(webhook, message)
      case (message, contact) =>
        Fail(s"Invalid recipients, cannot send $message to $contact")
    }.map(_ => ())
  }
}
