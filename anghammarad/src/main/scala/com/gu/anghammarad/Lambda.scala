package com.gu.anghammarad

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.anghammarad.serialization.Serialization

class Lambda extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {
    val stage = Config.getStage()

    val result = for {
      config <- Config.loadConfig(stage)
      mappings <- Serialization.parseAllMappings(config)
      notification <- Serialization.parseNotification(input)
      _ <- Anghammarad.run(notification, mappings)
    } yield ()

    // send notification if result is a failure
  }
}
