package com.gu.anghammarad

import com.gu.anghammarad.models._
import com.typesafe.scalalogging.StrictLogging
import org.json.JSONObject.{quote => quoteJson}


object Json extends StrictLogging {
  private[anghammarad] def messageJson(message: String, sourceSystem: String, channel: RequestedChannel, targets: List[Target], actions: List[Action]): String = {
    val channelStr = channel match {
      case Email => "email"
      case HangoutsChat => "hangouts"
      case All => "all"
    }
    val x = s"""{
       |  "message":${quoteJson(message)},
       |  "sender":${quoteJson(sourceSystem)},
       |  "channel":${quoteJson(channelStr)},
       |  "target": ${targetJson(targets)},
       |  "actions": [ ${actions.map(actionsJson).mkString(",")} ]
       |}""".stripMargin
    logger.info(x)
    x
  }

  private[anghammarad] def targetJson(targets: List[Target]): String = {
    def targetJsonString(key: String, value: String) = s""""$key":${quoteJson(value)}"""
    val kvpairs = targets.map (target => {
      target match {
        case Stack(stack) => targetJsonString("Stack", stack)
        case Stage(stage) => targetJsonString("Stage", stage)
        case App(app) => targetJsonString("App", app)
        case AwsAccount(awsAccount) => targetJsonString("AwsAccount", awsAccount)
        case _ => ""
      }
    }
    ).mkString(",")
    s"{$kvpairs}"
  }

  private[anghammarad] def actionsJson(action: Action): String = {
    s"""{"cta":${quoteJson(action.cta)},"url":${quoteJson(action.url)}}"""
  }
}
