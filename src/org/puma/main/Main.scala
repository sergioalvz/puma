package org.puma.main

import org.puma.generator.GeneratorFactory

/**
 * Project: puma
 * Package: org.puma.main
 *
 * User: Sergio Álvarez
 * Date: 09/2013
 */
object Main {
  def main(args: Array[String]): Unit = {
    run()
  }

  private[this] def run() {
    println("==============================================")
    println("                    puma                      ")
    println("==============================================")
    println()

    val generator = GeneratorFactory.get
    generator.generate()
  }
}
