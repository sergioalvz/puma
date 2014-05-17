import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("org.puma.main.Main")

outputPath in assembly := new java.io.File("./out/puma.jar")
