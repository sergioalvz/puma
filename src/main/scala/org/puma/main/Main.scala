package org.puma.main

import org.puma.generator.GeneratorFactory
import org.puma.configuration.{Configuration, ConfigurationUtil, CLIParser}

/**
 * Project: puma
 * Package: org.puma.main
 *
 * User: Sergio Álvarez
 * Date: 09/2013
 */
object Main {
  def main(args: Array[String]): Unit = CLIParser.parse(args, Configuration()) map { config => run(config) }

  private[this] def run(config: Configuration) {
    println("==============================================")
    println("                    puma                      ")
    println("==============================================")
    println()

    ConfigurationUtil.load(config)
    val generator = GeneratorFactory.get
    generator.generate()
  }
}
