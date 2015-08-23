name := """botrpg-online"""

version := "0.0.1-SNAPSHOT"

scalaVersion in Global := "2.11.7"

lazy val root = Project("root", file("."))
  .aggregate(client, server)

lazy val common = Project("common", file("common"))

def commonSrcDirs = Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value.getAbsoluteFile / ".." / "common" / "src" / "main" / "scala",
  unmanagedSourceDirectories in Test += baseDirectory.value.getAbsoluteFile / ".." / "common" / "src" / "test" / "scala"
)

lazy val server = Project("server", file("server"))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      "org.webjars" % "angularjs" % "1.3.11",
      "org.webjars" % "angular-ui-bootstrap" % "0.12.0",
      "org.webjars" % "bootstrap" % "3.3.2",
      "org.webjars" % "bootswatch-paper" % "3.3.1+2",
      "com.lihaoyi" %% "upickle" % "0.3.5",
      "org.scala-js" %% "scalajs-stubs" % "0.6.2",
      cache,
      ws,
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test"
    ),
    scalaJSProjects := Seq(client),
    pipelineStages := Seq(scalaJSProd, digest, gzip),
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    dockerRepository := Some("technius"),
    packageName in Docker := "botrpg-server",
    version in Docker := "latest",
    maintainer in Docker := "Bryan Tan <techniux@gmail.com>",
    dockerBaseImage := "williamyeh/java7:latest",
    dockerExposedPorts := Seq(9000)
  )
  .settings(commonSrcDirs: _*)
  .aggregate(client)
  .enablePlugins(DockerPlugin)

lazy val client = Project("client", file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(
    libraryDependencies ++= Seq(
      "biz.enef" %%% "scalajs-angulate" % "0.2",
      "com.lihaoyi" %%% "upickle" % "0.3.5"
    ),
    scalacOptions += "-Xlint:-infer-any" // workaround for possible scalac bug
  )
  .settings(commonSrcDirs: _*)

scalacOptions in Global ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings"
)
