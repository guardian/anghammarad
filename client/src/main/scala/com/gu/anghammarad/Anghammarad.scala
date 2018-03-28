package com.gu.anghammarad

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.anghammarad.AWS._
import com.gu.anghammarad.Json._
import com.gu.anghammarad.models._

import scala.concurrent.{ExecutionContext, Future}


object Anghammarad {
  /**
    * Sends a notification using Anghammarad.
    *
    * @param subject      Used for the subject in emails and the heading of hangouts chat notifications
    * @param message      The message body. Supports markdown, but support differs between notification channels
    * @param sourceSystem The system sending the notification (your system)
    * @param channel      The notification channel you'd like to use
    * @param target       Specify who should receive the message
    * @param actions      Specify Call To Action buttons that will be put at the end of an email / hangout message
    * @param topicArn     ARN of Anghammarad's SNS topic (you will need to obtain this and put it in your app's config)
    * @param client       The SNS client that should be used to add your notification to the topic
    * @return             Future of the resulting SNS Message ID
    */
  def notify(subject: String, message: String, sourceSystem: String,
             channel: RequestedChannel, target: List[Target], actions: List[Action],
             topicArn: String, client: AmazonSNSAsync)
            (implicit executionContext: ExecutionContext): Future[String] = {
    val request = new PublishRequest()
      .withTopicArn(topicArn)
      .withSubject(subject)
      .withMessage(messageJson(message, sourceSystem, channel, target, actions))
    awsToScala(client.publishAsync)(request).map(_.getMessageId)
  }

  /**
    * Use a default SNS client to create a notification.
    *
    * @param subject      Used for the subject in emails and the heading of hangouts chat notifications
    * @param message      The message body. Supports markdown, but support differs between notification channels
    * @param sourceSystem The system sending the notification (your system)
    * @param channel      The notification channel you'd like to use
    * @param target       Specify who should receive the message
    * @param actions      Specify Call To Action buttons that will be put at the end of an email / hangout message
    * @param topicArn     ARN of Anghammarad's SNS topic (you will need to obtain this and put it in your app's config)
    * @return             Future of the resulting SNS Message ID
    */
  def notify(subject: String, message: String, sourceSystem: String,
             channel: RequestedChannel, target: List[Target], actions: List[Action],
             topicArn: String)
            (implicit executionContext: ExecutionContext): Future[String] = {
    val credentialsProvider = AWS.credentialsProvider()
    val client = AWS.snsClient(credentialsProvider)
    notify(subject, message, sourceSystem, channel, target, actions, topicArn, client)
  }
}
