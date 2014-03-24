package org.puma.generator

import org.puma.configuration.ConfigurationUtil
import org.puma.analyzer.filter._
import scala.collection.mutable
import scala.io.Source
import org.puma.analyzer.NgramExtractor
import java.io.{FileOutputStream, OutputStreamWriter, BufferedWriter, File}
import scala.xml.pull.{EvElemEnd, EvText, EvElemStart, XMLEventReader}


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
  private[this] val scoreByTerm   = getScores.withDefaultValue(0.0)

  def generate(): Unit = {
    val location  = new mutable.StringBuilder()
    val text      = new mutable.StringBuilder()
    val latitude  = new mutable.StringBuilder()
    val longitude = new mutable.StringBuilder()

    var insideLocation  = false
    var insideText      = false
    var insideLatitude  = false
    var insideLongitude = false
    
    val reader = new XMLEventReader(Source.fromFile(ConfigurationUtil.getFilesToAnalyzeAbsolutePath + fileToAnalyze))
    reader.foreach(event => {
      event match {
        case EvElemStart(_, "location", _, _)  => insideLocation = true
        case EvElemEnd(_, "location")          => insideLocation = false
        case EvElemStart(_, "latitude", _, _)  => insideLatitude = true
        case EvElemEnd(_, "latitude")          => insideLatitude = false
        case EvElemStart(_, "longitude", _, _) => insideLongitude = true
        case EvElemEnd(_, "longitude")         => insideLongitude = false
        case EvElemStart(_, "text", _, _)      => insideText = true
        case EvElemEnd(_, "text")              => insideText = false
        case EvText(nodeText) =>
        {
          if(insideLocation)
            location ++= nodeText
          else if(insideLatitude)
            latitude ++= nodeText
          else if(insideLongitude)
            longitude ++= nodeText
          else if(insideText)
            text ++= nodeText
        }
        case EvElemEnd(_, "tweet") =>
        {
          processXmlData(location.toString(), latitude.toString(), longitude.toString(), text.toString())
          location.clear()
          latitude.clear()
          longitude.clear()
          text.clear()
        }
        case _ => ;
      }
    })
  }

  private[this] def processXmlData(location: String, latitude: String, longitude: String, text: String): Unit = {
    val locationKeywords = new LocationFilter().extract(location)
    val mentionsHashtags = new MentionFilter(new HashtagFilter()).extract(text)
    val bigrams  = new BigramsFilter().extract(text)
    val keywords = new KeywordFilter().extract(text).filter(keyword => {
      bigrams.find(bigram => bigram.contains(keyword)) == None
    })

    val terms = locationKeywords ++ mentionsHashtags ++ bigrams ++ keywords

    val score = terms.foldLeft(0.0)((acc, current) => acc + scoreByTerm(current.mkString(" ")))
    saveTweetScore(score, longitude, latitude, text)
  }

  private[this] def saveTweetScore(score: Double, latitude: String, longitude: String, text: String) = {
    val dir  = new File(ConfigurationUtil.getOutputFilesDirAbsolutePath)
    dir.mkdirs()

    val file = new File(dir.getAbsolutePath + "/" + s"${fileToAnalyze}_scores.tsv")
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
       val fields = line.split("\t")
       scores(fields(1)) = fields(0).toDouble
      })
    })
    scores.toMap
  }
}
