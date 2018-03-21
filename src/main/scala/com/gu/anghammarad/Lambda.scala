package com.gu.anghammarad

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.anghammarad.models.{Mapping, Notification}


class Lambda extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {
    // parse raw notification
    val notification: Notification = ???
    val config: List[Mapping] = ???

    val result = Anghammarad.run(notification, config)
    // log error
  }
}
