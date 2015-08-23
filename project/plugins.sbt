resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// The Play plugin

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.2")

// web plugins

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.4")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.2.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
