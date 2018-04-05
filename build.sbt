import com.gu.riffraff.artifact.RiffRaffArtifact.autoImport.{riffRaffArtifactResources, riffRaffUploadManifestBucket}

val compilerOptions = Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-target:jvm-1.8"
)

organization in ThisBuild := "com.gu"
scalaVersion in ThisBuild := "2.12.4"
licenses in ThisBuild := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

val awsSdkVersion = "1.11.258"
val circeVersion = "0.9.1"

//Projects

lazy val root = project
  .in(file("."))
  .settings(
    name := "anghammarad-root",
    scalacOptions ++= compilerOptions
  )
  .aggregate(anghammarad, client, common)

lazy val common = project
  .settings(
    name := "common",
    scalacOptions ++= compilerOptions
  )

lazy val client = project
  .dependsOn(common)
  .settings(
    name := "anghammarad-client",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion,
      "org.json" % "json" % "20180130",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    ),
    scalacOptions ++= compilerOptions,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/guardian/anghammarad"),
        "scm:git@github.com:guardian/anghammarad"
      )
    ),
    publishTo := sonatypePublishTo.value
  )

lazy val anghammarad = project
  .enablePlugins(JavaAppPackaging, RiffRaffArtifact)
  .dependsOn(common)
  .settings(
    name := "anghammarad",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-java-sdk-lambda" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-ses" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.criteo.lolhttp" %% "lolhttp" % "0.9.2",
      "com.vladsch.flexmark" % "flexmark-all" % "0.32.18",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    ),
    assemblyJarName := s"${name.value}.jar",
    riffRaffPackageType := assembly.value,
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffManifestProjectName := "tools::anghammarad",
    riffRaffArtifactResources += (file("cloudformation/anghammarad.template.yaml"), "cfn/cfn.yaml"),
    scalacOptions ++= compilerOptions
  )

lazy val dev = project
  .settings(
    name := "dev",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.7.0"
    ),
    scalacOptions ++= compilerOptions
  )
  .dependsOn(common, anghammarad)
