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
    * Stage must match - it is considered to be PROD if omitted.
    */
  def resolveTargetContacts(rawTargets: List[Target], rawMappings: List[Mapping]): Try[List[Contact]] = {
    val targets = normaliseStages(rawTargets)
    val mappings = rawMappings.map(mapping => mapping.copy(targets = normaliseStages(mapping.targets)))

    for {
      exactMatches <- findExactMatches(targets, mappings)
      underSpecifiedMatches = findUnderSpecifiedMatches(targets, mappings)
      overSpecifiedMatches = findOverSpecifiedMatches(targets, mappings)
      contacts <- exactMatches.orElse(underSpecifiedMatches).orElse(overSpecifiedMatches).fold[Try[List[Contact]]] {
        Fail(s"Could not find matching contacts for $targets")
      }(Success(_))
    } yield contacts
  }

  /**
    * Check for a mapping that exactly matches the target.
    *
    * Multiple exact matches is an error, so we fail straight away.
    */
  private def findExactMatches(targets: List[Target], mappings: List[Mapping]): Try[Option[List[Contact]]] = {
    mappings.filter(_.targets.toSet == targets.toSet) match {
      case Nil =>
        Success(None)
      case exactMatch :: Nil =>
        Success(Some(exactMatch.contacts))
      case multipleMatches =>
        Fail(s"Found multiple exact matches while resolving contacts for $targets")
    }
  }

  /**
    * Searches among mappings that have more detail than requested.
    *
    * e.g.
    * Ask for:
    *   App("app")
    * Mapping has:
    *   Stack("stack"), App("app")
    *
    * This is tricky because we wouldn't to match on the following
    *
    * Ask for:
    *   Stack("stack")
    * Mapping has:
    *   Stack("stack"), App("app")
    *
    * Accordingly, if the mapping is defined for a target that we don't ask for
    * that is more important (according to the target hierarchy), we will not
    * consider that a match.
    */
  private def findUnderSpecifiedMatches(targets: List[Target], mappings: List[Mapping]): Option[List[Contact]] = {
    mappings.filter { case Mapping(mappingTargets, _) =>
      targets.toSet subsetOf mappingTargets.toSet
    } match {
      case Nil =>
        None
      case matches =>
        val validMatches = matches.filter { case Mapping(mappingTargets, _) =>
          if (includesApp(mappingTargets)) appMatches(targets, mappingTargets)
          else if (includesStack(mappingTargets)) stackMatches(targets, mappingTargets)
          else if (includesAwsAccount(mappingTargets)) awsAccountMatches(targets, mappingTargets)
          else true
        }
        sortMappingsByTargets(targets, validMatches)
          .headOption.map(_.contacts)
    }
  }

  /**
    * Searches among mappings that have less detail than requested.
    * This is likely to be the normal way people specify targets.
    *
    * e.g.
    * Ask for:
    *   AwsAccount("xxx") Stack("stack") App("app")
    * Mapping has:
    *   App("App")
    *
    * we prioritise mappings that include higher priority targets
    * (according to the target hierarchy).
    */
  private def findOverSpecifiedMatches(targets: List[Target], mappings: List[Mapping]): Option[List[Contact]] = {
    mappings.filter { case Mapping(mappingTargets, _) =>
      mappingTargets.toSet subsetOf targets.toSet
    } match {
      case Nil =>
        None
      case matches =>
        sortMappingsByTargets(targets, matches)
          .headOption.map(_.contacts)
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
