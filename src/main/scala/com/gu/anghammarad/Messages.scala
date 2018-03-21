package com.gu.anghammarad

import com.gu.anghammarad.models._
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet

import collection.JavaConverters._


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
    val md = mdParser.parse(notification.message)
    val html = emailContents(md)

    EmailMessage(
      notification.subject,
      notification.message,
      html
    )
  }

  def emailContents(markdown: Node): String = {
    val html = mdRenderer.render(markdown)
    html
  }

  def hangoutMessage(notification: Notification): HangoutMessage = {
    ???
  }
}
