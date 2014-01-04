package org.puma.main

import org.puma.analyzer.Extractor
import java.io.FileNotFoundException

/**
 * Project: puma
 * Package: org.puma.main
 *
 * User: Sergio Álvarez
 * Date: 09/2013
 */
object Main {

  private[this] val files = Array("prueba.xml")

  def main(args: Array[String]): Unit = {
      run()
  }

  private[this] def run() {
    println("==============================================")
    println("                    puma                      ")
    println("==============================================")
    println()

    val extractor = new Extractor()
    for(path <- files){
      try{
      }catch {
        case ex: FileNotFoundException => Console.err.println("ERROR: The file " + path + " does not exist. Please, check if " +
          "the path provided is valid.")
      }
    }
  }
}
