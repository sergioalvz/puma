package org.puma.main

import org.puma.analyzer.Analyzer
import java.io._
import org.puma.configuration.ConfigurationUtil
import org.puma.model.Term
import scala.Console
import java.util.{Calendar, Date}
import java.text.SimpleDateFormat

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
        saveToFile(mostValuedTerms)
      }else {
        Console.err.println("ERROR: Must there exactly two files for analyzing. Please, " +
          "review the \"configuration.properties\" file.")
      }
    }catch {
      case ex: FileNotFoundException => Console.err.println("ERROR: The file does not exist. " + ex.getMessage)
    }
  }

  private[this] def saveToFile(terms:List[(Term, Double)]): Unit = {
    val dir  = new File(ConfigurationUtil.getOutputFilesDirAbsolutePath)
    dir.mkdirs()

    val title = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance.getTime)
    val file = new File(s"${dir.getAbsolutePath}/$title.tsv")
    if(!file.exists) file.createNewFile

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
    try{
      terms.foreach(t => writer.write(s"${t._1}\t${t._2}\n"))
    }finally{
      writer.flush()
      writer.close()
    }
  }
}
