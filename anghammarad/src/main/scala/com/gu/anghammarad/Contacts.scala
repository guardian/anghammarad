package com.gu.anghammarad

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.models.{Contact, Email, EmailAddress, HangoutsChat, HangoutsRoom, Mapping, Message, _}
import com.gu.anghammarad.Targets._

import scala.util.{Success, Try}


object Contacts {
  /**
    * Gets all available contacts for this target, from configuration.
    *
    * The logic is complex, the tests are a good reference for the expected behaviour.
    *
    * Exact matches are prioritised, then we apply logic to route other messages correctly.
    * App, Stack and AWS Account use a hierarchy, App > Stack > AwsAccount.
    * Stage treats PROD as the default and is required for a non-PROD match.
    */
  def resolveTargetContacts(rawTargets: List[Target], rawMappings: List[Mapping]): Try[List[Contact]] = {
    val targets = normaliseStages(rawTargets)
    val mappings = rawMappings.map(mapping => mapping.copy(targets = normaliseStages(mapping.targets)))

    mappings.filter(_.targets.toSet == targets.toSet) match {
      case Nil =>
        val underSpecified = mappings.filter { case Mapping(mappingTargets, _) =>
          targets.toSet subsetOf mappingTargets.toSet
        } match {
          case Nil =>
            Fail(s"No contacts found for $targets")
          case exactMatch :: Nil =>
            Success(exactMatch.contacts)
          case matches =>
            Fail(s"Cannot resolve contacts from ambiguous partial matches for $targets")
        }
        val overSpecified = mappings.filter { case Mapping(mappingTargets, _) =>
          mappingTargets.toSet subsetOf targets.toSet
        } match {
          case Nil =>
            Fail(s"No contacts found for $targets")
          case exactMatch :: Nil =>
            Success(exactMatch.contacts)
          case matches =>
            val bestMatch = matches.sortBy { mapping =>
              ( appMatches(mapping.targets, targets)
              , stackMatches(mapping.targets, targets)
              , awsAccountMatches(mapping.targets, targets)
              )
            }.lastOption
            bestMatch.fold[Try[List[Contact]]](Fail(s"Cannot resolve contacts from ambiguous partial matches for $targets"))(mapping => Success(mapping.contacts))
        }
        underSpecified.recoverWith { case _ => overSpecified }
      case exactMatch :: Nil =>
        Success(exactMatch.contacts)
      case _ =>
        Fail(s"Found multiple exact matches while resolving contacts for $targets")
    }
  }

  /**
    * Attempts to find contacts for each requested channel.
    */
  def resolveContactsForChannels(contacts: List[Contact], requestedChannel: RequestedChannel): List[(Channel, Contact)] = {
    requestedChannel match {
      case Email =>
        contacts.collect {
          case ea: EmailAddress => Email -> ea
        }
      case HangoutsChat =>
        contacts.collect {
          case hr: HangoutsRoom => HangoutsChat -> hr
        }
      case All =>
        contacts.collect {
          case ea: EmailAddress => Email -> ea
          case hr: HangoutsRoom => HangoutsChat -> hr
        }
    }
  }

  /**
    * Finds a contact (from provided available targets) for each message.
    */
  def contactsForMessages(channelMessages: List[(Channel, Message)], channelContacts: List[(Channel, Contact)]): Try[List[(Message, Contact)]] = {
    val resolved = channelMessages.flatMap { case (messageChannel, message) =>
      for {
        (_, contact) <- channelContacts.find { case (contactChannel, _) =>
          contactChannel == messageChannel
        }
      } yield message -> contact
    }
    if (resolved.size < channelMessages.size) {
      Fail(s"Could not find contacts for messages on the requested channels messages($channelMessages), contacts($channelContacts)")
    } else {
      Success(resolved)
    }
  }
}
