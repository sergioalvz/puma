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
        val analyzer = new Analyzer(files(0), files(1))
        val mostValuedTerms = analyzer.analyze
        mostValuedTerms.foreach( pair => { if(pair._2 != 0.0) println(pair._1 + " -> " + pair._2) } )
      }catch {
        case ex: FileNotFoundException => Console.err.println("ERROR: The file does not exist. " + ex.getMessage)
      }
     }
  }
}
