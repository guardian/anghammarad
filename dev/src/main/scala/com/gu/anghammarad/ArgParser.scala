package com.gu.anghammarad

import java.io.File

import com.gu.anghammarad.models._
import scopt.OptionParser

import scala.io.Source


object ArgParser {
  val argParser: OptionParser[Arguments] = new OptionParser[Arguments]("anghammarad") {
    cmd("json")
      .action { (_, _) =>
        Json("", "")
      }
      .text("provide a JSON message to mimic SNS")
      .children(
        arg[String]("subject")
          .action {
            case (subject, j: Json) =>
              j.copy(subject = subject)
            case _ => throw new RuntimeException("Arguments error")
          }
          .text("Subject for the message"),
        opt[File]('f', "file")
          .action {
            case (file, j: Json) =>
              val message = Source.fromFile(file, "UTF-8").mkString
              j.copy(json = message)
            case _ => throw new RuntimeException("Arguments error")
          }
          .text("file containing JSON message"),
        opt[String]('j', "json")
          .action {
            case (message, j: Json) =>
              j.copy(json = message)
            case _ => throw new RuntimeException("Arguments error")
          }
          .text("the raw JSON describing a message")
      )
    cmd("fields")
      .action { (_, _) =>
        Specified("", "", Nil, Nil, None, "")
      }
      .text("specify fields directly")
      .children(
        opt[String]("source")
          .action {
            case (source, fields: Specified) =>
              fields.copy(source = source)
            case _ => throw new RuntimeException("Arguments error")
          }
          .text("Source system (sender)"),
        opt[Unit]("email")
          .action {
            case (_, fields: Specified) =>
              fields.copy(channel = Some(Email))
            case _ => throw new RuntimeException("Arguments error")
          }
          text "Specify email as the channel",
        opt[Unit]("prefer-email")
          .action {
            case (_, fields: Specified) =>
              fields.copy(channel = Some(Preferred(Email)))
            case _ => throw new RuntimeException("Arguments error")
          }
          text "Specify email as the preferred channel",
        opt[Unit]("hangouts")
          .action {
            case (_, fields: Specified) =>
              fields.copy(channel = Some(HangoutsChat))
            case _ => throw new RuntimeException("Arguments error")
          }
          text "Specify hangouts chat as the channel",
        opt[Unit]("prefer-hangouts")
          .action {
            case (_, fields: Specified) =>
              fields.copy(channel = Some(Preferred(HangoutsChat)))
            case _ => throw new RuntimeException("Arguments error")
          }
          text "Specify hangouts chat as the preferred channel",
        opt[Unit]("all")
          .action {
            case (_, fields: Specified) =>
              fields.copy(channel = Some(All))
            case _ => throw new RuntimeException("Arguments error")
          }
          text "Specify all services as channels",
        opt[Map[String, String]]("targets")
          .validate { targets =>
            val valid = targets.forall { case (k, _) =>
              Set("Stack", "Stage", "App", "AwsAccount").contains(k)
            }
            if (valid) Right(())
            else Left("Valid targets are `Stack`, `Stage`, `App`, `AwsAccount`")
          }
          .action {
            case (targets, fields: Specified) =>
              val resolvedTargets = targets.toList.map {
                case ("Stack", stack) =>
                  Stack(stack)
                case ("Stage", stage) =>
                  Stage(stage)
                case ("App", app) =>
                  App(app)
                case ("AwsAccount", accountId) =>
                  AwsAccount(accountId)
                case (k, _) =>
                  throw new RuntimeException(s"Unkown target $k")
              }
              fields.copy(targets = resolvedTargets)
            case _ => throw new RuntimeException("Arguments error")
          }
          text "Specify all services as channels",
        opt[String]("subject")
          .action {
            case (subject, fields: Specified) =>
              fields.copy(subject = subject)
            case _ => throw new RuntimeException("Arguments error")
          }
          .text("Message subject"),
        opt[String]("message")
          .action {
            case (message, fields: Specified) =>
              fields.copy(message = message)
            case _ => throw new RuntimeException("Arguments error")
          }
          .text("Message body"),
        opt[Seq[String]]("ctas")
          .validate { ctas =>
            val valid = ctas.forall {
              case CTA(_, _) => true
              case _ => false
            }
            if (valid) Right(())
            else Left("Actions should be provided in the format text|url")
          }
          .action {
            case (ctas, fields: Specified) =>
              val actions = ctas.map {
                case CTA(text, url) =>
                  Action(text, url)
                case str => throw new RuntimeException(s"Invalid action format $str, should be `text|url`")
              }
              fields.copy(actions = actions.toList)
            case _ => throw new RuntimeException("Arguments error")
          }
          text "CTAs in the form text|url",
      )
  }

  val CTA = "(.+)\\|(.+)".r
}

trait Arguments
case object InitialArgs extends Arguments
case class Json(
  subject: String,
  json: String
) extends Arguments
case class Specified(
  subject: String,
  message: String,
  actions: List[Action],
  targets: List[Target],
  channel: Option[RequestedChannel],
  source: String
) extends Arguments
