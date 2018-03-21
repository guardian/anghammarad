package com.gu.anghammarad

import com.gu.anghammarad.models.{Mapping, Notification}


class DevMain {
  def main(args: Array[String]): Unit = {
    // parse raw notification
    val rawNotification: Notification = ???
    val config: List[Mapping] = ???

    val result = Anghammarad.run(rawNotification, config)
    // log error
  }
}
