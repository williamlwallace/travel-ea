// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0-RC9")

// Ebean Play plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "5.0.0")

// Twirl api
//addSbtPlugin("com.typesafe.play" % "twirl-api" % "1.4.0-RC4")

//SBT Checkstyle plugin
addSbtPlugin("com.etsy" % "sbt-checkstyle-plugin" % "3.1.1")
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "8.18"