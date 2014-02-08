package org.puma.main

import org.puma.analyzer.Analyzer
import java.io.FileNotFoundException
import org.puma.configuration.ConfigurationUtil

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
    try{
      val files = ConfigurationUtil.getFilesToAnalyze
      if(files.size == 2) {
        val local  = files(0)
        val global = files(1)

        val analyzer = new Analyzer(local, global)
        val mostValuedTerms = analyzer.analyze
        println(mostValuedTerms.mkString("\n"))
      }else {
        Console.err.println("ERROR: Must there exactly two files for analyzing. Please, " +
          "review the \"configuration.properties\" file.")
      }
    }catch {
      case ex: FileNotFoundException => Console.err.println("ERROR: The file does not exist. " + ex.getMessage)
    }

  }
}
