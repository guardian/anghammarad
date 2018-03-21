package com.gu.anghammarad.models

import com.vladsch.flexmark.ast.Node


sealed trait Target
case class Stack(stack: String) extends Target
case class Stage(stage: String) extends Target
case class App(app: String) extends Target
case class AwsAccount(awsAccount: String) extends Target

sealed trait Channel
case object Email extends Channel
case object HangoutsChat extends Channel

sealed trait Contact
case class EmailAddress(address: String) extends Contact
case class HangoutsRoom(webhook: String) extends Contact

case class Mapping(
  targets: List[Target],
  contacts: List[Contact]
)

sealed trait Message
case class EmailMessage(
  subject: String,
  plainText: String,
  html: String
) extends Message
case class HangoutMessage(
  cardJson: String
) extends Message

case class Notification(
  sourceSystem: String,
  channel: Channel,
  target: List[Target],
  subject: String,
  message: String,
  actions: List[Action]
)

case class Action(
  cta: String,
  url: String
)
