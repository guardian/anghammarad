package com.gu.anghammarad.messages

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.model.{Body, Content, Destination, SendEmailRequest, Message => AwsMessage}
import software.amazon.awssdk.services.ses.SesClient
import com.gu.anghammarad.models.EmailMessage
import com.gu.anghammarad.Config

import scala.util.Try


object EmailService {
  val client = SesClient.builder().region(Region.EU_WEST_1)
    .credentialsProvider(Config.credentialsProvider)
    .build()

  def emailRequest(senderAddress: String, recipientAddress: String, message: EmailMessage): SendEmailRequest = {
    def buildContent(data: String) = Content.builder().charset("UTF-8").data(data).build()

    val awsMessage = AwsMessage.builder()
      .subject(buildContent(message.subject))
      .body(Body.builder()
        .html(buildContent(message.html))
        .text(buildContent(message.plainText))
        .build()
      )
      .build()

    SendEmailRequest.builder()
      .destination(Destination.builder().toAddresses(recipientAddress).build())
      .source(senderAddress)
      .message(awsMessage)
      .build()
  }
  def sendEmail(senderAddress: String, recipientAddress: String, message: EmailMessage): Try[Unit] = {
    Try(client.sendEmail(emailRequest(senderAddress, recipientAddress, message)))
  }
}
