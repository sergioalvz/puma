package org.puma.generator

import org.puma.configuration.ConfigurationUtil
import org.puma.analyzer.Analyzer
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.Console

/**
 * Project: puma
 * Package: org.puma.generator
 *
 * Author: Sergio Álvarez
 * Date: 03/2014
 */
class LLRGenerator extends Generator {
  def generate(): Unit = {
    try{
      val files = ConfigurationUtil.getFilesToAnalyze
      if(files.size == 2) {
        val local  = files(0)
        val global = files(1)

        val analyzer = new Analyzer(local, global)
        val mostValuedTerms = analyzer.analyze
        saveToFile(mostValuedTerms)
      }else {
        println("ERROR: Must there exactly two files for analyzing. Please, review the \"configuration.properties\" file.")
      }
    }catch {
      case ex: FileNotFoundException => Console.err.println("ERROR: The file does not exist. " + ex.getMessage)
    }
  }

  private[this] def saveToFile(terms:List[(List[String], Double)]): Unit = {
    val dir  = new File(ConfigurationUtil.getOutputFilesDirAbsolutePath)
    dir.mkdirs()

    val title = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance.getTime)
    val filterName = ConfigurationUtil.getFilterToApply.getClass.getSimpleName
    val file = new File(s"${dir.getAbsolutePath}/${title}_$filterName.tsv")
    if(!file.exists) file.createNewFile

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
    try{
      terms.foreach(t => writer.write(s"${t._2}\t${t._1.mkString(" ")}\n"))
    }finally{
      writer.flush()
      writer.close()
    }
  }
}
