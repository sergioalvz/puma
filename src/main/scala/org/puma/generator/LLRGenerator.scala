package org.puma.generator

import org.puma.configuration.ConfigurationUtil
import org.puma.analyzer.Analyzer
import org.puma.analyzer.filter.ExtractorFilter
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
      val files  = ConfigurationUtil.getFilesToAnalyze
      val local  = files(0)
      val global = files(1)

      ConfigurationUtil.getFiltersToApply.foreach(f => {
        val analyzer = new Analyzer(local, global, f)
        val mostValuedTerms = analyzer.analyze
        saveToFile(mostValuedTerms, f)
      })
    }catch {
      case ex: FileNotFoundException => Console.err.println("ERROR: The file does not exist. " + ex.getMessage)
    }
  }

  private[this] def saveToFile(terms:List[(List[String], Double)], filter: ExtractorFilter): Unit = {
    val title = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance.getTime)
    val filterName = filter.getClass.getSimpleName

    val file = new File(s"${title}_$filterName.tsv")
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
