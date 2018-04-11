package com.gu.anghammarad

import com.gu.anghammarad.models._


object Targets {
  private val collectAwsAccount: PartialFunction[Target, AwsAccount] = { case a @ AwsAccount(_) => a }
  private val collectStack: PartialFunction[Target, Stack] = { case s @ Stack(_) => s }
  private val collectApp: PartialFunction[Target, App] = { case a @ App(_) => a }
  private val collectStage: PartialFunction[Target, Stage] = { case s @ Stage(_) => s }

  def normaliseStages(targets: List[Target]): List[Target] = {
    if (includesStage(targets)) targets
    else Stage("PROD") :: targets
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

  def stageMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val stages1 = targets1.collect(collectStage).toSet
    val stages2 = targets2.collect(collectStage).toSet
    (stages1 intersect stages2).nonEmpty
  }

  def awsAccountMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val stages1 = targets1.collect(collectAwsAccount).toSet
    val stages2 = targets2.collect(collectAwsAccount).toSet
    (stages1 intersect stages2).nonEmpty
  }

  def stackMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val stages1 = targets1.collect(collectStack).toSet
    val stages2 = targets2.collect(collectStack).toSet
    (stages1 intersect stages2).nonEmpty
  }

  def appMatches(targets1: List[Target], targets2: List[Target]): Boolean = {
    val stages1 = targets1.collect(collectApp).toSet
    val stages2 = targets2.collect(collectApp).toSet
    (stages1 intersect stages2).nonEmpty
  }

  def sortMappingsByTargets(targets: List[Target], mappings: List[Mapping]): List[Mapping] = {
    mappings.sortBy { mapping =>
      ( appMatches(mapping.targets, targets)
      , stackMatches(mapping.targets, targets)
      , awsAccountMatches(mapping.targets, targets)
      )
    }.reverse
  }
}
