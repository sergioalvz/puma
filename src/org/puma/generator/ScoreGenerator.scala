package org.puma.generator

import org.puma.configuration.ConfigurationUtil
import scala.xml.XML
import org.puma.analyzer.filter._
import scala.collection.mutable
import scala.io.Source
import org.puma.analyzer.NgramExtractor
import java.io.{FileOutputStream, OutputStreamWriter, BufferedWriter, File}


/**
 * Project: puma
 * Package: org.puma.generator
 *
 * Author: Sergio Álvarez
 * Date: 03/2014
 */
class ScoreGenerator extends Generator{

  private[this] val resultFiles   = ConfigurationUtil.getLLRResultFiles
  private[this] val fileToAnalyze = ConfigurationUtil.getFileToAnalyze

  def generate(): Unit = {
    val scoreByTerm = getScores.withDefaultValue(0.0)

    val root = XML.load(s"${ConfigurationUtil.getFilesToAnalyzeAbsolutePath}$fileToAnalyze")
    (root \\ "tweet").foreach(node => {
      val text      = (node \\ "text")(0).text
      val location  = (node \\ "location")(0).text
      val longitude = (node \\ "latitude")(0).text
      val latitude  = (node \\ "longitude")(0).text

      val locationKeywords = new LocationFilter().extract(location)
      val mentionsHashtags = new MentionFilter(new HashtagFilter()).extract(text)
      val bigrams  = new BigramsFilter().extract(text)
      val keywords = new KeywordFilter().extract(text).filter(keyword => {
        bigrams.find(bigram => bigram.contains(keyword)) == None
      })

      val terms = locationKeywords ++ mentionsHashtags ++ bigrams ++ keywords

      var score: Double = 0.0
      terms.foreach(term => {
        val format = term.mkString(" ")
        score += scoreByTerm(format)
      })
      saveTweetScore(score, latitude, longitude, text)
    })
  }

  private[this] def saveTweetScore(score: Double, latitude: String, longitude: String, text: String) = {
    val dir  = new File(ConfigurationUtil.getOutputFilesDirAbsolutePath)
    dir.mkdirs()

    val file = new File(s"${dir.getAbsolutePath}/${fileToAnalyze}_scores.tsv")
    if(!file.exists) file.createNewFile

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))
    try{
      writer.write(s"$score\t${latitude.trim}\t${longitude.trim}\t${clear(text.trim)}\n")
    }finally{
      writer.flush()
      writer.close()
    }
  }

  private[this] def clear(text: String): String = {
    val clean = NgramExtractor.extract(text, 1)
    clean.flatten.mkString(" ")
  }

  private[this] def getScores: Map[String, Double] = {
    val scores = mutable.Map.empty[String, Double]
    resultFiles.foreach(file => {
      val lines = Source.fromFile(ConfigurationUtil.getOutputFilesDirAbsolutePath + file).getLines().toArray
      lines.foreach(line => {
       val pieces = line.split("\t")
       scores(pieces(1)) = pieces(0).toDouble
      })
    })
    scores.toMap
  }
}
