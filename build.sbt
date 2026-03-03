import sbtrelease.ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion

val assemblySettings = Seq(
  assembly / assemblyMergeStrategy := {
    case path if path.endsWith("module-info.class") => MergeStrategy.last
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first //see https://github.com/sbt/sbt-assembly/issues/362
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

inThisBuild(Seq(
  scalaVersion := "3.3.7",
  crossScalaVersions := Seq("2.13.18", scalaVersion.value),
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-encoding", "UTF-8",
    "-release:11",
  ),
  // sonatype metadata
  organization := "com.gu",
  licenses := Seq(License.Apache2),
))

val awsSdkVersion = "2.41.21"
val circeVersion = "0.14.15"
val flexmarkVersion = "0.64.8"
val scalaTestVersion = "3.2.19"
val scalaLoggingVersion = "3.9.6"

//Projects

lazy val root = project
  .in(file("."))
  .settings(
    name := "anghammarad-root",
    publish / skip := true,
    // publish settings, for common and client
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion,
    ),
    releaseVersion := ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease().value,
  )
  .aggregate(anghammarad, client, common, dev)

lazy val common = project
  .settings(
    name := "anghammarad-common",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )

lazy val client = project
  .dependsOn(common)
  .settings(
    name := "anghammarad-client",
    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "sns" % awsSdkVersion,

      "org.json" % "json" % "20251224",
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    assemblySettings,
  )

lazy val anghammarad = project
  .enablePlugins(JavaAppPackaging, ScalafixPlugin)
  .dependsOn(common)
  .settings(
    name := "anghammarad",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.14.0",
      "com.amazonaws" % "aws-lambda-java-events" % "3.16.1",
      "com.amazonaws" % "aws-lambda-java-core" % "1.4.0",
      "software.amazon.awssdk" % "lambda" % awsSdkVersion,
      "software.amazon.awssdk" % "ses" % awsSdkVersion,
      "software.amazon.awssdk" % "s3" % awsSdkVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.softwaremill.sttp.client3" %% "core" % "3.11.0",
      "com.vladsch.flexmark" % "flexmark" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-gfm-strikethrough" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-tables" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-util" % flexmarkVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    assembly / assemblyOutputPath := file("anghammarad/anghammarad.jar"),
    assemblySettings,
    publish / skip := true,
  )

lazy val dev = project
  .settings(
    name := "dev",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "4.1.0"
    ),
    assemblySettings,
    publish / skip := true,
  )
  .dependsOn(common, anghammarad, client)
