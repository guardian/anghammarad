package com.gu.anghammarad.common

import com.gu.anghammarad.models._


object Targets {
  private val collectAwsAccount: PartialFunction[Target, AwsAccount] =
    { case a @ AwsAccount(_) => a }
  private val collectStack: PartialFunction[Target, Stack] =
    { case s @ Stack(_) => s }
  private val collectApp: PartialFunction[Target, App] =
    { case a @ App(_) => a }
  private val collectStage: PartialFunction[Target, Stage] =
    { case s @ Stage(_) => s }
  private val collectGithubTeamSlug: PartialFunction[Target, GithubTeamSlug] = {
    case s@GithubTeamSlug(_) => s
  }

  def includesAwsAccount(targets: List[Target]): Boolean = {
    targets.collect(collectAwsAccount).nonEmpty
  }

  def includesStack(targets: List[Target]): Boolean = {
    targets.collect(collectStack).nonEmpty
  }

  def includesApp(targets: List[Target]): Boolean = {
    targets.collect(collectApp).nonEmpty
  }

  def includesStage(targets: List[Target]): Boolean = {
    targets.collect(collectStage).nonEmpty
  }

  def includesGithubTeamSlug(targets: List[Target]): Boolean = {
    targets.collect(collectGithubTeamSlug).nonEmpty
  }


  def stageMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val stages1 = targets1.collect(collectStage).toSet
    val stages2 = targets2.collect(collectStage).toSet
    (stages1 intersect stages2).nonEmpty
  }

  def awsAccountMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val awsAccounts1 = targets1.collect(collectAwsAccount).toSet
    val awsAccounts2 = targets2.collect(collectAwsAccount).toSet
    (awsAccounts1 intersect awsAccounts2).nonEmpty
  }

  def stackMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val stacks1 = targets1.collect(collectStack).toSet
    val stacks2 = targets2.collect(collectStack).toSet
    (stacks1 intersect stacks2).nonEmpty
  }

  def appMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val apps1 = targets1.collect(collectApp).toSet
    val apps2 = targets2.collect(collectApp).toSet
    (apps1 intersect apps2).nonEmpty
  }

  def githubTeamSlugMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val githubTeamSlug1 = targets1.collect(collectGithubTeamSlug).toSet
    val githubTeamSlug2 = targets2.collect(collectGithubTeamSlug).toSet
    (githubTeamSlug1 intersect githubTeamSlug2).nonEmpty
  }


  def shouldDefaultBasedOnStage(targets1: List[Target], targets2: List[Target]): Boolean = {
    val prodOrStagelessMapping = targets1.collect(collectStage).toSet.contains(Stage("PROD")) || !includesStage(targets1)
    prodOrStagelessMapping && !includesStage(targets2)
  }

  def sortMappingsByTargets(targets: List[Target], mappings: List[Mapping]): List[Mapping] = {
    mappings.sortBy { mapping =>
      ( appMatches(mapping.targets, targets) && stageMatches(mapping.targets, targets)
      , stackMatches(mapping.targets, targets) && stageMatches(mapping.targets, targets)
      , awsAccountMatches(mapping.targets, targets) && stageMatches(mapping.targets, targets)
      , appMatches(mapping.targets, targets) && shouldDefaultBasedOnStage(mapping.targets, targets)
      , stackMatches(mapping.targets, targets) && shouldDefaultBasedOnStage(mapping.targets, targets)
      , awsAccountMatches(mapping.targets, targets) && shouldDefaultBasedOnStage(mapping.targets, targets)
      , appMatches(mapping.targets, targets)
      , stackMatches(mapping.targets, targets)
      , awsAccountMatches(mapping.targets, targets)
      )
    }.reverse
  }
}
