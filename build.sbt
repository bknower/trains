import sbt.Keys.libraryDependencies

name := "custer"

version := "0.1"

scalaVersion := "3.0.2"

// testing library
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

// json library
libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.3"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "4.0.3"

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"

libraryDependencies += "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % "test"