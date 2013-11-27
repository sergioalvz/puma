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

  private[this] def run(path: String) {
    println("==============================================")
    println("                    puma                      ")
    println("==============================================")
    println()
    try{
      val analyzer = new Analyzer(path)
      println("File to analyze: " + path)
      println()
      println("########################")
      println("  Mentions detected: ")
      println("########################")
      val mentions = analyzer.mentions
      for(mention <- mentions.keySet){
        println("@" + mention + ": " + mentions(mention))
      }

      println()
      println("########################")
      println("  Hashtags detected: ")
      println("########################")
      val hashtags = analyzer.hashtags
      for(hashtag <- hashtags.keySet){
        println("#" + hashtag + ": " + hashtags(hashtag))
      }

      println()
      println("########################")
      println("  Bigrams detected: ")
      println("########################")
      analyzer.bigrams
    }catch {
      case ex: FileNotFoundException => Console.err.println("ERROR: The file " + path + " does not exist. Please, check if " +
        "the path provided is valid.")
    }
  }
}
