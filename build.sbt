name := "anghammarad"
organization := "com.gu"
scalaVersion := "2.12.4"
version      := "0.1.0-SNAPSHOT"

val awsSdkVersion = "1.11.258"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-java-sdk-lambda" % awsSdkVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

scalacOptions := Seq("-unchecked", "-deprecation")
