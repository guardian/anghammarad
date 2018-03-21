package com.gu.anghammarad

import com.gu.anghammarad.models._

import scala.util.{Failure, Try}


object SendMessages {

  def sendAll(toSend: List[(Contact, Message)]): Try[Unit] = {
    toSend.map {
      case (EmailAddress(address), message: EmailMessage) =>
        sendEmail(address, message)
      case (HangoutsRoom(webhook), message: HangoutMessage) =>
        sendHangoutsMessage(webhook, message)
      case _ =>
        Failure(???)
    }
    ???
  }

  def sendEmail(address: String, emailMessage: EmailMessage): Try[Unit] = {
    ???
  }

  def sendHangoutsMessage(webhook: String, hangoutMessage: HangoutMessage): Try[Unit] = {
    ???
  }
}
