package com.gu.anghammarad.messages

import com.gu.anghammarad.models._
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet

import scala.collection.JavaConverters._


object Messages {
  private[anghammarad] val mdOptions = new MutableDataSet()
    .set(Parser.EXTENSIONS, List(TablesExtension.create(), StrikethroughExtension.create()).asJava)
  private[anghammarad] val mdParser = Parser.builder(mdOptions).build
  private[anghammarad] val mdRenderer = HtmlRenderer.builder(mdOptions).build()

  def channelMessages(notification: Notification): List[(Channel, Message)] = {
    notification.channel match {
      case Email =>
        List(
          Email -> emailMessage(notification)
        )
      case HangoutsChat =>
        List(
          HangoutsChat -> hangoutMessage(notification)
        )
      case All =>
        List(
          Email -> emailMessage(notification),
          HangoutsChat -> hangoutMessage(notification)
        )
    }
  }

  def emailMessage(notification: Notification): EmailMessage = {
    val actionPrefix =
      s"""
         |---------------------
         |
       """.stripMargin

    val htmlActions = notification.actions.map { action =>
      s"[${action.cta}](${action.url})"
    }.mkString("\n\n")
    val plainTextActions = notification.actions.map { action =>
      s"${action.cta} - ${action.url}"
    }.mkString("\n\n")

    val finalMarkdown = notification.message + actionPrefix + htmlActions
    val finalPlanText = notification.message + actionPrefix + plainTextActions

    val md = mdParser.parse(finalMarkdown)
    val html = emailContents(md)

    EmailMessage(
      notification.subject,
      finalPlanText,
      html
    )
  }

  def emailContents(markdown: Node): String = {
    val html = mdRenderer.render(markdown)
    html
  }

  def hangoutMessage(notification: Notification): HangoutMessage = {
    def textButtonJson(action: Action) =
      s"""
         |{
         |  "textButton": {
         |    "text": "${action.cta}",
         |    "onClick": {
         |      "openLink": {
         |        "url": "${action.url}"
         |      }
         |    }
         |  }
         |}
       """.stripMargin
    val json =
      s"""
         |{
         |  "cards": [
         |    {
         |      "sections": [
         |        {
         |          "header": "${notification.subject}",
         |          "widgets": [
         |            {
         |              "textParagraph": {
         |                "text": "${notification.message}"
         |              }
         |            }
         |          ]
         |        },
         |        {
         |          "widgets": [
         |            ${notification.actions.map(textButtonJson).mkString(",")}
         |          ]
         |        }
         |      ]
         |    }
         |  ]
         |}
       """.stripMargin
    HangoutMessage(json)
  }
}
