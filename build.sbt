name := "SENG302 TEAM 400 - Proffat"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

fork in run := false

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies += guice
libraryDependencies += javaJdbc % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "com.auth0" % "java-jwt" % "3.3.0"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8"
libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8"
libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.9.8"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.12"
libraryDependencies += ws

libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test

testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")

