name         := "anghammarad"
organization := "com.gu"
scalaVersion := "2.12.4"
version      := "0.1.0-SNAPSHOT"

val awsSdkVersion = "1.11.258"
val circeVersion = "0.9.1"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-java-sdk-lambda" % awsSdkVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.vladsch.flexmark" % "flexmark-all" % "0.32.18",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

scalacOptions ++= Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-target:jvm-1.8"
)

enablePlugins(JavaAppPackaging)
topLevelDirectory in Universal := None
packageName in Universal := normalizedName.value
