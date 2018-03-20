package com.gu.anghammarad

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import collection.JavaConverters._


class Main extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {
    val msgs = input.getRecords.asScala.map { snsRecord =>
      val msgId = snsRecord.getSNS.getMessageId

      val msgType = snsRecord.getSNS.getType
      val source = snsRecord.getEventSource
      val timestamp = snsRecord.getSNS.getTimestamp

      val subject = snsRecord.getSNS.getSubject
      val message = snsRecord.getSNS.getMessage
      val msgAttrs = snsRecord.getSNS.getMessageAttributes.asScala.map { case (key, msgAttr) =>
        s"$key [${msgAttr.getType}]: ${msgAttr.getValue}"
      }.mkString("{", ",", "}")
      s"$msgId ($msgType $source $timestamp): [$subject] $message $msgAttrs"
    }
    context.getLogger.log(msgs.mkString("{", ",", "}"))
  }
}
