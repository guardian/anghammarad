package com.gu.anghammarad

import com.gu.anghammarad.models.Notification


class DevMain {
  def main(args: Array[String]): Unit = {
    // parse raw notification
    val rawNotification: Notification = ???
    val result = Anghammarad.run(rawNotification)
    // log error
  }
}
