package org.puma.main

import scala.io.Source

/**
 * Project: puma
 * Package: org.puma.main
 *
 * User: Sergio Álvarez
 * Date: 09/2013
 */
object Main {
  def main (args: Array[String]) : Unit = {
    if(!(args.length > 0)){
      Console.err.println("ERROR: The file to analyze must be passed as first argument.")
    }else{
      Console.println("==============================================")
      Console.println("                    puma                      ")
      Console.println("==============================================")

      Console.println()
      Console.println("File to analyze: " + args(0))
      Console.println()
      Console.println("Number of tweets to analyze: " + numberOfTweets(args(0)))
    }
  }

  def numberOfTweets (path: String) : Int = Source.fromFile(path).getLines().length
}
