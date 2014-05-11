name := "Puma"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= List(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")
