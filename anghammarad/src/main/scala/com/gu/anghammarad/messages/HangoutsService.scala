package com.gu.anghammarad.messages

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.models.HangoutMessage
import lol.http.{Client, Post}

import scala.concurrent.duration._
import scala.util.{Success, Try}


object HangoutsService {
  // using a blocking call, but lolhttp still requires an execution context
  import scala.concurrent.ExecutionContext.Implicits.global

  val client = Client("chat.googleapis.com", 443, "https")

  def sendHangoutsMessage(webhook: String, message: HangoutMessage): Try[Unit] = {
    val request = Post(webhook.stripPrefix("https://chat.googleapis.com"), message.cardJson)
    val (status, response) = client.runSync(request, timeout = 5.seconds) { response =>
      val status = response.status
      for {
        response <- response.readAs[String]
      } yield (status, response)
    }
    if (status == 200) {
      Success(())
    } else {
      Fail(s"Got $status from chat.googleapis.com, could not send message: $response")
    }
  }
}
