import AssemblyKeys._

assemblySettings

jarName in assembly := "puma.jar"

mainClass in assembly := Some("org.puma.main.Main")
