package com.gu.anghammarad.messages

import com.gu.anghammarad.models.HangoutMessage

import scala.util.Try


object HangoutsService {
  def sendHangoutsMessage(webhook: String, message: HangoutMessage): Try[Unit] = {
    Try(???)
  }
}
