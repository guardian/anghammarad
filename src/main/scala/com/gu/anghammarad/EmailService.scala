package com.gu.anghammarad

import com.amazonaws.auth.{AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.model.{Body, Content, Destination, SendEmailRequest, SendEmailResult, Message => AwsMessage}
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailService, AmazonSimpleEmailServiceClientBuilder}
import scala.util.Try

object EmailService {

  private val provider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("deployTools"),
    new EnvironmentVariableCredentialsProvider()
  )

  val client: AmazonSimpleEmailService = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_WEST_1)
    .withCredentials(provider)
    .build()

  def emailRequest(recipientAddress: String, message: models.EmailMessage): SendEmailRequest = {

    def buildContent(data: String) = new Content().withCharset("UTF-8").withData(data)

    val awsMessage = new AwsMessage()
      .withSubject(buildContent(message.subject))
      .withBody(new Body()
        .withHtml(buildContent(message.html))
        .withText(buildContent(message.plainText))
      )

    new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(recipientAddress))
      .withSource("foo123@theguardian.com") //TODO read from environment variable or conf
      .withMessage(awsMessage)

  }

  def sendEmail(recipientAddress: String, message: models.EmailMessage): Try[SendEmailResult] = {
    Try(client.sendEmail(emailRequest(recipientAddress, message)))
  }

}



