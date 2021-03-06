import de.johoop.jacoco4sbt._
import JacocoPlugin._
import de.johoop.findbugs4sbt.FindBugs._
import de.johoop.findbugs4sbt.ReportType

organization := "org.hibernatewrapper"

name := "hibernate-wrapper"

version := "0.0.1"

scalaVersion := "2.11.6"

val jettyVersion = "9.2.4.v20141103"

libraryDependencies ++= Seq(
  "org.hibernate" % "hibernate-core" % "4.3.7.Final",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % "provided",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "optional",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.h2database" % "h2" % "1.4.182" % "test",
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "test",
  "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "test",
  "org.eclipse.jetty" % "jetty-annotations" % jettyVersion % "test",
  "org.eclipse.jetty" % "jetty-client" % jettyVersion % "test",
  "org.mockito" % "mockito-all" % "1.10.8" % "test"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-unchecked", "-deprecation")

jacoco.settings

seq(findbugsSettings : _*)

findbugsReportType := Some(ReportType.Html)
