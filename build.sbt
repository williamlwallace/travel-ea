name := "SENG302 TEAM 13"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

fork in run := false
resolvers += "SQLite-JDBC Repository" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies += guice
libraryDependencies += javaJdbc
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += javaJdbc % Test
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.6"

libraryDependencies ++= Seq(evolutions, javaJdbc % Test)

libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test

testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")

