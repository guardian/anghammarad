package com.gu.anghammarad.messages

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.model.{Body, Content, Destination, SendEmailRequest, Message => AwsMessage}
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailService, AmazonSimpleEmailServiceClientBuilder}
import com.gu.anghammarad.models.EmailMessage
import com.gu.anghammarad.Config

import scala.util.Try


object EmailService {
  val client: AmazonSimpleEmailService = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_WEST_1)
    .withCredentials(Config.credentialsProvider)
    .build()

  def emailRequest(senderAddress: String, recipientAddress: String, message: EmailMessage): SendEmailRequest = {
    def buildContent(data: String) = new Content().withCharset("UTF-8").withData(data)

    val awsMessage = new AwsMessage()
      .withSubject(buildContent(message.subject))
      .withBody(new Body()
        .withHtml(buildContent(message.html))
        .withText(buildContent(message.plainText))
      )

    new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(recipientAddress))
      .withSource(senderAddress)
      .withMessage(awsMessage)
  }
  def sendEmail(senderAddress: String, recipientAddress: String, message: EmailMessage): Try[Unit] = {
    Try(client.sendEmail(emailRequest(senderAddress, recipientAddress, message)))
  }
}
