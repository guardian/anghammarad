package com.gu.anghammarad.messages

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.models.HangoutMessage
import scalaj.http.{Http, HttpResponse}
import scala.util.{Success, Try}

object HangoutsService {
  def sendHangoutsMessage(webhook: String, message: HangoutMessage): Try[Unit] = {
    val response: HttpResponse[String] = Http(webhook.stripPrefix("https://chat.googleapis.com"))
      .postData(message.cardJson)
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
      .proxy("chat.googleapis.com", 443)
      .asString
    val status = response.code
    if(status == 200)
      Success(())
    else
      Fail(s"Got $status from chat.googleapis.com, could not send message: $response")
  }
}