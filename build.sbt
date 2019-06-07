import sbt.Keys.scalaVersion

enablePlugins(JavaAppPackaging)

val akkaV          = "2.5.16"
val akkaHttpV      = "10.1.4"
val scalaTestV     = "3.0.5"
val circeV         = "0.10.1"
val akkaHttpCirceV = "1.22.0"
val igniteScala    = "1.7.2-SNAPSHOT"
val logbackV       = "1.2.3"
val nsScalaTimeV   = "2.20.0"
val quillV         = "2.6.0"
val scalaAsyncV    = "0.9.7"

lazy val recService = (project in file("."))
  .dependsOn(RootProject(uri("git://github.com/stacktome/ignite-scala.git")))
  .settings(Seq(
    name := """ignite-data-service""",
    version := "1.0",
    scalaVersion := "2.12.8",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-actor"           % akkaV,
      "com.typesafe.akka"      %% "akka-stream"          % akkaV,
      "com.typesafe.akka"      %% "akka-testkit"         % akkaV,
      "com.typesafe.akka"      %% "akka-http"            % akkaHttpV,
      "com.typesafe.akka"      %% "akka-http-spray-json" % akkaHttpV,
      "com.typesafe.akka"      %% "akka-http-testkit"    % akkaHttpV,
      "org.scalatest"          %% "scalatest"            % scalaTestV % "test",
      "de.heikoseeberger"      %% "akka-http-circe"      % akkaHttpCirceV,
      "io.circe"               %% "circe-core"           % circeV,
      "io.circe"               %% "circe-generic"        % circeV,
      "io.circe"               %% "circe-jawn"           % circeV,
      "io.circe"               %% "circe-parser"         % circeV,
      "ch.qos.logback"         % "logback-classic"       % logbackV,
      "com.github.nscala-time" %% "nscala-time"          % nsScalaTimeV,
      "io.getquill"            %% "quill-sql"            % quillV,
      "org.scala-lang.modules" %% "scala-async"          % scalaAsyncV
    ),
    // add 'config' directory first in the classpath of the start script,
    // an alternative is to set the config file locations via CLI parameters
    // when starting the application
    scriptClasspath := Seq("../config/") ++ scriptClasspath.value
  ))
  .enablePlugins(JavaServerAppPackaging)
