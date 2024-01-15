val compilerOptions = Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8"
)

val assemblySettings = Seq(
  assembly / assemblyMergeStrategy := {
    case path if path.endsWith("module-info.class") => MergeStrategy.last
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

inThisBuild(Seq(
  scalaVersion := "3.3.1",
  crossScalaVersions := Seq("2.13.12", scalaVersion.value),
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-encoding", "UTF-8"
  ),
  // sonatype metadata
  organization := "com.gu",
  licenses := Seq("Apache V2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/guardian/anghammarad"),
      "scm:git@github.com:guardian/anghammarad"
    )
  ),
  homepage := scmInfo.value.map(_.browseUrl),
  developers := List(
    Developer(id = "guardian", name = "Guardian", email = null, url = url("https://github.com/guardian"))
  )
))

val awsSdkVersion = "1.12.637"
val circeVersion = "0.14.6"
val flexmarkVersion = "0.64.8"
val scalaTestVersion = "3.2.17"
val scalaLoggingVersion = "3.9.5"

//Projects

lazy val root = project
  .in(file("."))
  .settings(
    name := "anghammarad-root",
    // publish settings
    releaseCrossBuild := true,
    publish / skip := true,
    publishTo := sonatypePublishTo.value,
    releaseProcess += releaseStepCommandAndRemaining("sonatypeRelease"),
  )
  .aggregate(anghammarad, client, common, dev)

lazy val common = project
  .settings(
    name := "anghammarad-common",
    // publish settings
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishTo := sonatypePublishTo.value,
  )

lazy val client = project
  .dependsOn(common)
  .settings(
    name := "anghammarad-client",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion,
      "org.json" % "json" % "20231013",
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    // publish settings
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishTo := sonatypePublishTo.value,
    assemblySettings,
  )

lazy val anghammarad = project
  .enablePlugins(JavaAppPackaging, ScalafixPlugin)
  .dependsOn(common)
  .settings(
    name := "anghammarad",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.4",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.3",
      "com.amazonaws" % "aws-java-sdk-lambda" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-ses" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.softwaremill.sttp.client3" %% "core" % "3.9.1",
      "com.vladsch.flexmark" % "flexmark" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-gfm-strikethrough" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-ext-tables" % flexmarkVersion,
      "com.vladsch.flexmark" % "flexmark-util" % flexmarkVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    publish / skip := true,
    assembly / assemblyOutputPath := file("anghammarad/anghammarad.jar"),
    assemblySettings,
  )

lazy val dev = project
  .settings(
    name := "dev",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "4.1.0"
    ),
    publish / skip := true,
    assemblySettings,
  )
  .dependsOn(common, anghammarad, client)
