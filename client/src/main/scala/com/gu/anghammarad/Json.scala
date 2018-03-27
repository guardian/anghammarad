package com.gu.anghammarad

import com.gu.anghammarad.models._
import org.json.JSONObject.{quote => quoteJson}


object Json {
  private[anghammarad] def messageJson(message: String, sourceSystem: String, channel: RequestedChannel, target: List[Target], actions: List[Action]): String = {
    val channelStr = channel match {
      case Email => "email"
      case HangoutsChat => "hangouts"
      case All => "all"
    }
    s"""{
       |  "message":${quoteJson(message)},
       |  "sender":${quoteJson(sourceSystem)},
       |  "channel":${quoteJson(channelStr)},
       |  "target":${quoteJson(target.map(targetJson).mkString(","))}
       |  "actions":${quoteJson(actions.map(actionsJson).mkString(","))}
       |}""".stripMargin
  }

  private[anghammarad] def targetJson(target: Target): String = {
    val (key, value) = target match {
      case Stack(stack) => "Stack" -> stack
      case Stage(stage) => "Stage" -> stage
      case App(app) => "App" -> app
      case AwsAccount(awsAccount) => "AwsAccount" -> awsAccount
    }
    s"""{"$key":${quoteJson(value)}}"""
  }

  private[anghammarad] def actionsJson(action: Action): String = {
    s"""{"cta":${quoteJson(action.cta)},"url":${quoteJson(action.url)}}"""
  }
}
