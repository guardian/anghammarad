package com.gu.anghammarad

class DevMain {
  def main(args: Array[String]): Unit = {
    // parse raw notification
    val rawNotification: RawNotification = ???
    val result = Main.run(rawNotification)
    // log error
  }
}
