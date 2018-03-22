package com.gu.anghammarad

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.anghammarad.models._


class Lambda extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {

    // get config from s3... and fail fast if this is not successful
    val config: String = Config.loadConfig().getOrElse(throw new RuntimeException("Unable to load config from S3"))

    // parse the config and extract the mappings...
    val mappings: List[Mapping] = Serialization.parseAllMappings(config).getOrElse(throw new RuntimeException("Failed to parse config"))

    // parse the notification
    val notification: Notification = ???

    val result = Anghammarad.run(notification, mappings)
  }
}
