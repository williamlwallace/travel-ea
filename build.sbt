name := "SENG302 TEAM 400 - Proffat"

version := "0.0.4"

scalaVersion := "2.12.8"

fork in run := false

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies += guice
libraryDependencies += javaJdbc % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.199"
libraryDependencies += "com.auth0" % "java-jwt" % "3.8.0"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.0.1"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9"
libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.9"
libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.9.9"
libraryDependencies += "org.apache.commons" % "commons-text" % "1.6"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.12"
libraryDependencies += "com.googlecode.owasp-java-html-sanitizer" % "owasp-java-html-sanitizer" % "20180219.1"

libraryDependencies += ws

libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test

testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")

libraryDependencies += "io.cucumber" % "cucumber-core" % "4.2.0" % " test "
libraryDependencies += "io.cucumber" % "cucumber-jvm" % "4.2.0" % " test "
libraryDependencies += "io.cucumber" % "cucumber-junit" % "4.2.0" % " test "
libraryDependencies += "io.cucumber" % "cucumber-java" % "4.2.0"


