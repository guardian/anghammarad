package com.gu.anghammarad

import com.gu.anghammarad.models._

object Contacts {
  /**
    * Gets all available contacts for this target, from configuration.
    */
  def resolveTargetContacts(target: List[Target], config: List[Mapping]): List[Contact] = {
    ???
  }

  /**
    * Attempts to find contacts for each requested channel.
    */
  def resolveContactsForChannels(contacts: List[Contact], requestedChannel: RequestedChannel): List[(Channel, Contact)] = {
    ???
  }

  /**
    * Finds a contact (from provided available targets) for each message.
    */
  def contactsForMessages(channelContacts: List[(Channel, Contact)], channelMessages: List[(Channel, Message)]): List[(Contact, Message)] = {
    ???
  }
}
