import com.gu.riffraff.artifact.RiffRaffArtifact.autoImport.{riffRaffArtifactResources, riffRaffUploadManifestBucket}

organization in ThisBuild := "com.gu"
scalaVersion in ThisBuild := "2.12.4"

//Projects

lazy val root = project
  .in(file("."))
  .settings(
    name := "anghammarad-root"
  )
  .aggregate(anghammarad, `client-lib`, common)

lazy val common = project
  .settings(
    name := "common",
    scalacOptions ++= compilerOptions
  )

lazy val `client-lib` = project
  .dependsOn(common)
  .settings(
    name := "client-lib",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    ),
    scalacOptions ++= compilerOptions
  )

lazy val anghammarad = project
  .enablePlugins(JavaAppPackaging, RiffRaffArtifact)
  .dependsOn(common)
  .settings(
    name := "anghammarad",
    assemblyJarName := s"${name.value}.jar",
    riffRaffPackageType := assembly.value,
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffManifestProjectName := "tools::anghammarad",
    libraryDependencies ++= anghammaradDependencies,
    riffRaffArtifactResources += (file("cloudformation/anghammarad.template.yaml"), "cfn/cfn.yaml"),
    scalacOptions ++= compilerOptions
  )

lazy val dev = project
  .in(file("dev"))
  .settings(
    name := "dev",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.7.0"
    ),
    scalacOptions ++= compilerOptions
  )
  .dependsOn(common, anghammarad)

// Dependencies

val awsSdkVersion = "1.11.258"
val circeVersion = "0.9.1"

lazy val anghammaradDependencies = Seq(
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
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

// Settings

val compilerOptions = Seq(
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-target:jvm-1.8"
)


