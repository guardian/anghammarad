package com.gu.anghammarad.messages

import com.gu.anghammarad.models._
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import io.circe.Json

import scala.jdk.CollectionConverters._
import scala.collection.compat._

object Messages {
  private val mdExtensions: List[Extension] = List(
    TablesExtension.create(),
    StrikethroughExtension.create()
  )

  private[anghammarad] val mdOptions = new MutableDataSet()
    .set(Parser.EXTENSIONS, mdExtensions.asJava)

  private[anghammarad] val mdParser = Parser.builder(mdOptions).build
  private[anghammarad] val mdRenderer = HtmlRenderer.builder(mdOptions).build()

  def emailMessage(notification: Notification): EmailMessage = {
    val (markdown, plaintext) =
      if (notification.actions.isEmpty) {
        (notification.message, notification.message)
      } else {
        val actionPrefix =
          s"""
             |_____________________
             |
             |""".stripMargin

        val htmlActions = notification.actions.map { action =>
          s"[${action.cta}](${action.url})"
        }.mkString("\n\n")
        val plainTextActions = notification.actions.map { action =>
          s"${action.cta} - ${action.url}"
        }.mkString("\n\n")

        (notification.message + actionPrefix + htmlActions, notification.message + actionPrefix + plainTextActions)
      }

    val markdownWithNotice = markdown + anghammaradNotice(notification)
    val plaintextWithNotice = plaintext + anghammaradNotice(notification)

    val html = mdRenderer.render(mdParser.parse(markdownWithNotice))

    EmailMessage(
      notification.subject,
      plaintextWithNotice,
      html
    )
  }

  def hangoutMessage(notification: Notification): HangoutMessage = {
    val messageWithAnghammaradNotice = notification.message ++ anghammaradNotice(notification)

    val html = mdRenderer.render(mdParser.parse(messageWithAnghammaradNotice))
      // hangouts chat supports a subset of tags that differs from the flexmark-generated HTML
      .replace("<strong>", "<b>").replace("</strong>", "</b>")
      .replace("<em>", "<i>").replace("</em>", "</i>")
      .replace("<p>", "").replace("</p>", "<br>")

    val json =
      s"""{
         |  "cards": [
         |    {
         |      "sections": [
         |        {
         |          "header": ${Json.fromString(notification.subject).noSpaces},
         |          "widgets": [
         |            {
         |              "textParagraph": {
         |                "text": ${Json.fromString(html).noSpaces}
         |              }
         |            }
         |          ]
         |        }${buttonJson(notification.actions)}
         |      ]
         |    }
         |  ]
         |}
         |""".stripMargin
    HangoutMessage(json)
  }

  private def buttonJson(actions: List[Action]): String = {
    if (actions.isEmpty) {
      ""
    } else {
      s""",
         |{
         |  "widgets": [
         |    {
         |      "buttons": [
         |        ${actions.map(textButtonJson).mkString(",")}
         |      ]
         |    }
         |  ]
         |}""".stripMargin
    }
  }

  private def textButtonJson(action: Action): String = {
    s"""{
       |  "textButton": {
       |    "text": ${Json.fromString(action.cta).noSpaces},
       |    "onClick": {
       |      "openLink": {
       |        "url": ${Json.fromString(action.url).noSpaces}
       |      }
       |    }
       |  }
       |}
       |""".stripMargin
  }

  private def anghammaradNotice(notification: Notification): String = {
    s"""
       |
       |This message was sent by ${notification.sourceSystem} via [Anghammarad](https://github.com/guardian/anghammarad).
       |""".stripMargin
  }
}
