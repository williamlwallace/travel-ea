// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0")

// Ebean Play plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "5.0.0")

//SBT Checkstyle plugin
addSbtPlugin("com.etsy" % "sbt-checkstyle-plugin" % "3.1.1")
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "8.18"