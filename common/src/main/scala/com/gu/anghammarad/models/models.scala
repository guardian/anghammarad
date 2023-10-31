package com.gu.anghammarad.models

sealed trait Target
case class Stack(stack: String) extends Target
case class Stage(stage: String) extends Target
case class App(app: String) extends Target
case class AwsAccount(awsAccount: String) extends Target
case class GithubTeamSlug(slug: String) extends Target

sealed trait RequestedChannel
case object All extends RequestedChannel
case class Preferred(preferredChannel: Channel) extends RequestedChannel

sealed trait Channel
case object Email extends Channel with RequestedChannel
case object HangoutsChat extends Channel with RequestedChannel

sealed trait Contact
case class EmailAddress(address: String) extends Contact
case class HangoutsRoom(webhook: String) extends Contact

case class Configuration(
  emailDomain: String,
  emailSender: String,
  mappings: List[Mapping]
)

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
  cardJson: String,
  threadKey: Option[String]
) extends Message

case class Notification(
  subject: String,
  message: String,
  actions: List[Action],
  target: List[Target],
  channel: RequestedChannel,
  sourceSystem: String,
  threadKey: Option[String] // only used for Hangouts messages
)

case class Action(
  cta: String,
  url: String
)
