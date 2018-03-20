package com.gu.anghammarad

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.util.Try


class Main extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {
    // parse raw notification
    val rawNotification: RawNotification = ???
    val result = Main.run(rawNotification)
    // log error
  }
}
object Main {
  def run(rawNotification: RawNotification): Try[Unit] = {
    // parse input into Notification
    val notification: Notification = ???
    // resolve targets
    val contacts: List[Contact] = ???
    // DECIDE!!!
    val filteredContacts: List[Contact] = ???
    // send
    filteredContacts.map { contact =>
      val message = Logic.channelMessage(contact, notification)
      Logic.send(contact, message)
    }
    ???
  }
}
