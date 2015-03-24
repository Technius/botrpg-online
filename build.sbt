name := """botrpg-online"""

version := "0.0.1-SNAPSHOT"

scalaVersion in Global := "2.11.6"

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
      "com.lihaoyi" %% "upickle" % "0.2.6",
      "org.scala-js" %% "scalajs-stubs" % "0.6.0",
      cache,
      ws,
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test"
    ),
    scalaJSProjects := Seq(client),
    pipelineStages := Seq(scalaJSProd, digest, gzip),
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false
  )
  .settings(commonSrcDirs: _*)
  .aggregate(client)

lazy val client = Project("client", file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(
    libraryDependencies ++= Seq(
      "biz.enef" %%% "scalajs-angulate" % "0.1",
      "com.lihaoyi" %%% "upickle" % "0.2.6"
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
