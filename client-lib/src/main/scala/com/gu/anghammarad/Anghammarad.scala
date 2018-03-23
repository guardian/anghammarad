package com.gu.anghammarad

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.auth.{AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.model.{PublishRequest, PublishResult}
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.gu.anghammarad.models._

import scala.concurrent.{ExecutionContext, Future, Promise}

object Anghammarad {
  /**
    * Use this to make an SNS client, or provide your own.
    */
  def client(credentialsProvider: AWSCredentialsProviderChain): AmazonSNSAsync = {
    AmazonSNSAsyncClientBuilder.standard()
      .withRegion(Regions.EU_WEST_1)
      .withCredentials(credentialsProvider)
      .build()
  }

  /**
    * @param subject      Used for the subject in emails and the heading of hangouts chat notifications
    * @param message      The message body. Supports markdown, but support differs between notification channels
    * @param sourceSystem The system sending the notification (your system)
    * @param channel      The notification channel you'd like to use
    * @param target       Specify who should receive the message
    * @param actions      Specify Call To Action buttons that will be put at the end of an email / hangout message
    * @param client       The SNS client that should be used to add your notification to the topic
    * @return             Future of the resulting SNS Message ID
    */
  def notify(subject: String, message: String, sourceSystem: String,
             channel: RequestedChannel, target: List[Target], actions: List[Action],
             client: AmazonSNSAsync)
            (implicit executionContext: ExecutionContext): Future[String] = {
    val request = new PublishRequest()
      .withSubject(subject)
      .withMessage(messageJson(message, sourceSystem, channel, target, actions))
    awsToScala(client.publishAsync)(request).map(_.getMessageId)
  }

  private[anghammarad] def messageJson(message: String, sourceSystem: String, channel: RequestedChannel, target: List[Target], actions: List[Action]): String = {
    val channelStr = channel match {
      case Email => "email"
      case HangoutsChat => "hangouts"
      case All => "all"
    }
    s"""{
       |  "message": $message,
       |  "sender": $sourceSystem,
       |  "channel": $channelStr,
       |  "target": ${target.map(targetJson).mkString(",")}
       |  "actions": ${actions.map(actionsJson).mkString(",")}
       |}""".stripMargin
  }

  private[anghammarad] def targetJson(target: Target): String = {
    val (key, value) = target match {
      case Stack(stack) => "Stack" -> stack
      case Stage(stage) => "Stage" -> stage
      case App(app) => "App" -> app
      case AwsAccount(awsAccount) => "AwsAccount" -> awsAccount
    }
    s"""{"$key":"$value"}"""
  }

  private[anghammarad] def actionsJson(action: Action): String = {
    s"""{"cta":"${action.cta}","url":"${action.url}"}"""
  }

  private class AwsAsyncPromiseHandler[R <: AmazonWebServiceRequest, T](promise: Promise[T]) extends AsyncHandler[R, T] {
    def onError(e: Exception): Unit = {
      promise failure e
    }
    def onSuccess(r: R, t: T): Unit = {
      promise success t
    }
  }

  private def awsToScala[R <: AmazonWebServiceRequest, T](sdkMethod: ( (R, AsyncHandler[R, T]) => java.util.concurrent.Future[T])): (R => Future[T]) = { req =>
    val p = Promise[T]
    sdkMethod(req, new AwsAsyncPromiseHandler(p))
    p.future
  }
}
