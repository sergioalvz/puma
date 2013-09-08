package org.puma.main

import org.puma.analyzer.Analyzer
import java.io.FileNotFoundException

/**
 * Project: puma
 * Package: org.puma.main
 *
 * User: Sergio Álvarez
 * Date: 09/2013
 */
object Main {
  def main(args: Array[String]): Unit = {
    if (!(args.length > 0)) {
      Console.err.println("ERROR: The file to analyze must be passed as first argument.")
    } else {
      run(args(0))
    }
  }

  def run(path: String) {
    println("==============================================")
    println("                    puma                      ")
    println("==============================================")
    println()
    try {
      val analyzer = new Analyzer(path)
      println("File to analyze: " + path)
      println()
      println("Number of tweets to analyze: " + analyzer.numberOfTweets)
    } catch {
      case ex: FileNotFoundException => Console.err.println("ERROR: The file " + path +" does not exist. Please, check if " +
        "the path provided is valid.")
    }
  }
}
