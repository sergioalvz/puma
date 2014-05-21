package org.puma.configuration

import java.io.File

case class Configuration(config: File = null) {}

object CLIParser {
  private[this] val parser = new scopt.OptionParser[Configuration]("Puma") {
    head("Puma", "1.0")
    opt[File]('c', "config")
      .required() action { (x, c) => c.copy(config = x) } text("Configuration XML file for executing Puma.")
  }

  def parse(args:Array[String], conf:Configuration):Option[Configuration] = this.parser.parse(args, conf)
}
