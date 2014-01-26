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

    val files = ConfigurationUtil.getFilesToAnalyze
    if(files.size == 2) {
      try{
        val mostValuedTerms = Analyzer.analyze(files(0), files(1))
        mostValuedTerms.keys.foreach(term => {
          if(mostValuedTerms.get(term).get < 0.0) println(term + " -> " + mostValuedTerms.get(term).get)
        })
      }catch {
        case ex: FileNotFoundException => Console.err.println("ERROR: The file does not exist. " + ex.getMessage)
      }
     }
  }
}
