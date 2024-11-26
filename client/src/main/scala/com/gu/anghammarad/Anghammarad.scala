package com.gu.anghammarad

import com.gu.anghammarad.AWS._
import com.gu.anghammarad.Json._
import com.gu.anghammarad.models._
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest

import scala.concurrent.{ExecutionContext, Future}


object Anghammarad {
  lazy private val defaultClient = AWS.snsClient(AWS.credentialsProvider())
  /**
    * Sends a notification using Anghammarad.
    *
    * @param subject      Used for the subject in emails and the heading of hangouts chat notifications
    * @param message      The message body. Supports markdown, but support differs between notification channels
    * @param actions      Specify Call To Action buttons that will be put at the end of an email / hangout message
    * @param target       Specify who should receive the message
    * @param channel      The notification channel you'd like to use
    * @param sourceSystem The system sending the notification (your system)
    * @param topicArn     ARN of Anghammarad's SNS topic (you will need to obtain this and put it in your app's config)
    * @param client       The SNS client used to add your notification to the topic (if omitted, a default is used)
    * @return             Future containing the resulting SNS Message ID
    */
  def notify(subject: String, message: String, actions: List[Action], target: List[Target], channel: RequestedChannel,
             sourceSystem: String, topicArn: String, client: SnsAsyncClient = defaultClient)
            (implicit executionContext: ExecutionContext): Future[String] = {
    val request = PublishRequest.builder()
      .topicArn(topicArn)
      .subject(subject)
      .message(messageJson(message, sourceSystem, channel, target, actions))
      .build()
    asScala(client.publish(request)).map(_.messageId)
  }

  /**
    * Sends a notification using Anghammarad.
    * This uses a default SNS client with a default implementation of the credentials provider.
    *
    * @param notification The notification to send
    * @param topicArn     ARN of Anghammarad's SNS topic (you will need to obtain this and put it in your app's config)
    * @return             Future containing the resulting SNS Message ID
    */
  def notify(notification: Notification, topicArn: String)
            (implicit executionContext: ExecutionContext): Future[String] = {
    val request = PublishRequest.builder()
      .topicArn(topicArn)
      .subject(notification.subject)
      .message(messageJson(notification.message, notification.sourceSystem, notification.channel, notification.target, notification.actions))
      .build()
     asScala(defaultClient.publish(request)).map(_.messageId)
  }

  /**
    * Sends a notification using Anghammarad.
    *
    * @param notification The notification to send
    * @param topicArn     ARN of Anghammarad's SNS topic (you will need to obtain this and put it in your app's config)
    * @param client       The SNS client used to add your notification to the topic
    * @return             Future containing the resulting SNS Message ID
    */
  def notify(notification: Notification, topicArn: String, client: SnsAsyncClient)
            (implicit executionContext: ExecutionContext): Future[String] = {
    val request = PublishRequest.builder()
      .topicArn(topicArn)
      .subject(notification.subject)
      .message(messageJson(notification.message, notification.sourceSystem, notification.channel, notification.target, notification.actions))
      .build()
    asScala(client.publish(request)).map(_.messageId)
  }
}
