package com.gu.anghammarad

import com.gu.anghammarad.AnghammaradException.Fail
import com.gu.anghammarad.models.{Channel, Contact, Email, EmailAddress, HangoutsChat, HangoutsRoom, Mapping, Message, _}
import com.gu.anghammarad.Targets._
import com.gu.anghammarad.messages.Messages.{emailMessage, hangoutMessage}

import scala.util.{Success, Try}


object Contacts {
  /**
    * Gets all available contacts for this target, from configuration.
    *
    * The logic is complex, the tests are a good reference for the expected behaviour.
    *
    * Exact matches are prioritised, then we apply logic to route other messages correctly.
    * App, Stack and AWS Account use a hierarchy, App > Stack > AwsAccount.
    *
    * If there are multiple matches then stage is used as a tiebreaker.
    * If stage is not sent by the source system then we assume a notification should be sent to the PROD mapping (note
    * that for legacy reasons mappings which omit stage are assumed to be PROD).
    */
  def resolveTargetContacts(targets: List[Target], mappings: List[Mapping]): Try[List[Contact]] = for {
    exactMatches <- findExactMatches(targets, mappings)
    underSpecifiedMatches = findUnderSpecifiedMatches(targets, mappings)
    overSpecifiedMatches = findOverSpecifiedMatches(targets, mappings)
    contacts <- exactMatches.orElse(underSpecifiedMatches).orElse(overSpecifiedMatches).fold[Try[List[Contact]]] {
      Fail(s"Could not find matching contacts for $targets")
    }(Success(_))
  } yield contacts

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
    * This is tricky because we wouldn't want to match on the following
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
          else if (includesGithubTeamSlug(mappingTargets)) githubTeamSlugMatches(targets, mappingTargets)
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
  def resolveContactsForChannels(contacts: List[Contact], requestedChannel: RequestedChannel): Try[List[(Channel, Contact)]] = {
    val emails = contacts.collect {
      case ea: EmailAddress => Email -> ea
    }
    val webhooks = contacts.collect {
      case hr: HangoutsRoom => HangoutsChat -> hr
    }
    val resolved = requestedChannel match {
      case Email =>
        emails
      case HangoutsChat =>
        webhooks
      case All =>
        emails ++ webhooks
      case Preferred(Email) =>
        if (emails.nonEmpty) emails
        else webhooks
      case Preferred(HangoutsChat) =>
        if (webhooks.nonEmpty) webhooks
        else emails
    }
    if (resolved.isEmpty) Fail(s"Could not find any contacts for requested channel, $requestedChannel")
    else Success(resolved)
  }

  /**
    * Finds a contact (from provided available targets) for each message.
    */
  def contactsForMessage(requestedChannel: RequestedChannel, channelContacts: List[(Channel, Contact)]): Try[List[(Channel, Contact)]] = {
    val emailContacts = channelContacts.filter { case (channel, _) =>
      channel == Email
    }
    val hangoutsContacts = channelContacts.filter { case (channel, _) =>
      channel == HangoutsChat
    }
    val resolvedContacts = requestedChannel match {
      case Email =>
        emailContacts
      case HangoutsChat =>
        hangoutsContacts
      case All =>
        emailContacts ++ hangoutsContacts
      case Preferred(Email) =>
        if (emailContacts.nonEmpty) emailContacts
        else hangoutsContacts
      case Preferred(HangoutsChat) =>
        if (hangoutsContacts.nonEmpty) hangoutsContacts
        else emailContacts
    }

    if (resolvedContacts.isEmpty) {
      Fail(s"Could not find contacts for messages on the requested channels $requestedChannel, contacts($channelContacts)")
    } else {
      Success(resolvedContacts)
    }
  }

  def createMessages(notification: Notification, addressees: List[(Channel, Contact)]): List[(Message, Contact)] = {
    addressees.map {
      case (Email, contact) =>
        emailMessage(notification) -> contact
      case (HangoutsChat, contact) =>
        hangoutMessage(notification) -> contact
    }
  }
}
