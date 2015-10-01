name := """botrpg-online"""

version := "0.0.1-SNAPSHOT"

scalaVersion in Global := "2.11.7"

lazy val root = Project("root", file("."))
  .aggregate(client, server)

lazy val common = (crossProject.crossType(CrossType.Pure) in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.3.5"
    )
  )
  .jvmConfigure(_.settings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "0.6.4"
  ))
  .jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val commonJvm = common.jvm

lazy val commonJs = common.js

lazy val server = Project("server", file("server"))
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.webjars" % "angularjs" % "1.3.11",
      "org.webjars" % "angular-ui-bootstrap" % "0.12.0",
      "org.webjars" % "bootstrap" % "3.3.2",
      "org.webjars" % "bootswatch-paper" % "3.3.1+2",
      cache,
      ws,
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.3.14" % "test"
    ),
    scalaJSProjects := Seq(client),
    pipelineStages := Seq(scalaJSProd, digest, gzip),
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    dockerRepository := Some("technius"),
    packageName in Docker := "botrpg-server",
    maintainer in Docker := "Bryan Tan <techniux@gmail.com>",
    dockerUpdateLatest := true,
    dockerBaseImage := "java:8-jre",
    dockerEntrypoint in Docker := Seq("sh", "-c", "bin/server"),
    dockerExposedPorts := Seq(9000)
  )
  .aggregate(client)
  .dependsOn(commonJvm)


lazy val client = Project("client", file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(
    libraryDependencies ++= Seq(
      "biz.enef" %%% "scalajs-angulate" % "0.2"
    ),
    scalacOptions += "-Xlint:-infer-any" // workaround for possible scalac bug
  )
  .dependsOn(commonJs)

scalacOptions in Global ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings"
)
