name := "Puma"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= List(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0-rc1",
  "com.twitter" % "twitter-text" % "1.6.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.0-rc1",
  "org.apache.logging.log4j" % "log4j-core" % "2.0-rc1",
  "com.github.scopt" %% "scopt" % "3.2.0"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")
