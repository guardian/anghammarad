package com.gu.anghammarad.messages

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.models.HangoutMessage

import scala.util.{Success, Try}
import sttp.client3._

object HangoutsService {

  def postMessage(webhook: String, message: String): Response[Either[String, String]] = {
    val backend = HttpURLConnectionBackend()
    basicRequest
      .body(message)
      .post(uri"$webhook")
      .send(backend)
  }

  def checkResponse(response: Response[Either[String,String]]): Try[Unit] = {
    if (response.isSuccess)
      Success(())
    else
      Fail(s"Unable to send message. Received response code of: ${response.statusText}.")
  }

  def sendHangoutsMessage(webhook: String, message: HangoutMessage): Try[Unit] = {
    for {
      response <- Try {postMessage(webhook, message.cardJson)}
      successOrFailure <- checkResponse(response)
    } yield successOrFailure
  }
}