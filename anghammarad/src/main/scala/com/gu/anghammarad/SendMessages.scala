package com.gu.anghammarad

import com.gu.anghammarad.models._

import scala.util.{Failure, Try}


object SendMessages {

  def sendAll(toSend: List[(Message, Contact)]): Try[Unit] = {
    toSend.map {
      case (message: EmailMessage, EmailAddress(address)) =>
        sendEmail(message, address)
      case (message: HangoutMessage, HangoutsRoom(webhook)) =>
        sendHangoutsMessage(message, webhook)
      case _ =>
        Failure(???)
    }
    ???
  }

  def sendEmail(emailMessage: EmailMessage, address: String): Try[Unit] = {
    ???
  }

  def sendHangoutsMessage(hangoutMessage: HangoutMessage, webhook: String): Try[Unit] = {
    ???
  }
}
