name := """bitcoin-miner"""

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.13",
  "com.typesafe.akka" %% "akka-remote" %  "2.3.13"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")


fork in run := true