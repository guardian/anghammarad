package com.gu.cloudwatch

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.anghammarad.Anghammarad
import com.gu.anghammarad.models.{Action, HangoutsChat, Notification, Preferred, Stack}
import io.circe.Json
import io.circe.parser._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, SECONDS}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Try

case class CloudWatchAlarm(alarmName: String, alarmDescription: Option[String], newStateValue: String)

def getCloudWatchAlarm(content: Json): Try[CloudWatchAlarm] = {
  val hCursor = content.hcursor
  val parsingResult = for {
    alarmName <- hCursor.downField("AlarmName").as[String]
    alarmDescription <- hCursor.downField("AlarmDescription").as[Option[String]]
    newStateValue <- hCursor.downField("NewStateValue").as[String]
  } yield CloudWatchAlarm(alarmName = alarmName, alarmDescription = alarmDescription, newStateValue = newStateValue)
  parsingResult.toTry
}

class Lambda extends RequestHandler[SNSEvent, Unit] {
  override def handleRequest(input: SNSEvent, context: Context): Unit = {
    context.getLogger.log(s"received alarm event: $input")
    val snsRecords = input.getRecords.asScala.toList
    snsRecords.foreach(f = message => {
      val parseAlarmNotificationAttempt: Try[CloudWatchAlarm] = parse(message.getSNS.getMessage).toTry
        .flatMap(json => getCloudWatchAlarm(json))
      // TODO: what should we do if we can't parse this?
      val alarmNotification = parseAlarmNotificationAttempt.get
      val notification = Notification(
        subject = alarmNotification.alarmName, //TODO: include alarm/resolution information
        message = alarmNotification.alarmDescription.getOrElse("No description provided"),
        // TODO: allow users to pass their own actions via the alarm description?
        actions = List(Action("View alarm in AWS", s"https://eu-west-1.console.aws.amazon.com/cloudwatch/home?region=eu-west-1#s=Alarms&alarm=${alarmNotification.alarmName}")),
        // TODO: how can we get the targets from alarms given the lack of tag support (fallback to AWS account?)
        // https://github.com/aws-cloudformation/cloudformation-coverage-roadmap/issues/64
        target = List(Stack("testing-alerts")),
        channel = Preferred(HangoutsChat),
        sourceSystem = "cloudwatch-to-anghammarad",
      )
      // TODO: improve error handling because this Lambda is on the critical path
      context.getLogger.log(s"Attempting to notify via Anghammarad")
      Await.result(Anghammarad.notify(notification, System.getenv("ANGHAMMARAD_SNS_TOPIC_ARN")), Duration(5, SECONDS))
    })
  }

}