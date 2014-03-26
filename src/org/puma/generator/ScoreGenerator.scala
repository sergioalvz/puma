package org.puma.generator

import org.puma.configuration.ConfigurationUtil
import org.puma.analyzer.filter._
import scala.collection.mutable
import scala.io.Source
import org.puma.analyzer.NgramExtractor
import java.io.{FileOutputStream, OutputStreamWriter, BufferedWriter, File}
import scala.xml.pull.{EvElemEnd, EvText, EvElemStart, XMLEventReader}
import scala.collection.mutable.ListBuffer
import scala.xml.XML


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
  private[this] val boundingBoxes = getBoundingBoxes

  def generate(): Unit = {
    val location, text, latitude, longitude, username = new mutable.StringBuilder()
    var insideLocation, insideText, insideLatitude, insideLongitude, insideUsername = false
    
    val reader = new XMLEventReader(Source.fromFile(ConfigurationUtil.getFilesToAnalyzeDirAbsolutePath + fileToAnalyze))
    reader.foreach({
        case EvElemStart(_, "username", _, _)  => insideUsername = true
        case EvElemEnd(_, "username")          => insideUsername = false
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
          else if(insideUsername)
            username ++= nodeText
        }
        case EvElemEnd(_, "tweet") =>
        {
          processXmlData(username.toString(), location.toString(), latitude.toString(), longitude.toString(), text.toString())
          location.clear()
          latitude.clear()
          longitude.clear()
          text.clear()
          username.clear()
        }
        case _ => ;
    })
  }

  private[this] def processXmlData(username: String, location: String, latitude: String, longitude: String, text: String): Unit = {
    val locationKeywords = new LocationFilter().extract(location)
    val mentionsHashtags = new MentionFilter(new HashtagFilter()).extract(text)
    val bigrams  = new BigramsFilter().extract(text)
    val keywords = new KeywordFilter().extract(text).filter(keyword => {
      bigrams.find(bigram => bigram.contains(keyword)) == None
    })

    val terms = locationKeywords ++ mentionsHashtags ++ bigrams ++ keywords

    val score = terms.foldLeft(0.0)((acc, current) => acc + scoreByTerm(current.mkString(" ")))
    if(isValidScore(score, longitude, latitude)){
      saveScore(score, username, location, longitude, latitude, text)
    }
  }
  
  private[this] def isValidScore(score:Double, latitude:String, longitude:String): Boolean = {
    if(score == 0.0) return false

    val mustBeLocal = score > 0.0
    mustBeLocal == isInBoundingBoxes(longitude, latitude)
  }

  private[this] def saveScore(score:Double, username:String, location:String, latitude:String, longitude:String, text:String) = {
    val dir  = new File(ConfigurationUtil.getOutputFilesDirAbsolutePath)
    dir.mkdirs()

    val file = new File(dir.getAbsolutePath + "/" + s"${fileToAnalyze}_scores.tsv")
    if(!file.exists) file.createNewFile

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))
    val line = score + "\t" + latitude.trim + "\t" + longitude.trim + "\t" + format(username, location, text) + "\n"
    try{
      writer.write(line)
    }finally{
      writer.flush()
      writer.close()
    }
  }

  private[this] def format(username:String, location:String, text:String): String = {
    val normalized = NgramExtractor.extract(location.trim + " " + text.trim(), 1, allowMentionsAndHashtags = true).flatten.mkString(" ")
    "@" + username.toLowerCase.trim + " " + normalized
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

  private[this] def getBoundingBoxes: List[List[(Double, Double)]] = {
    val root = XML.loadFile(ConfigurationUtil.getResourcesDirAbsolutePath + ConfigurationUtil.getBoundingBoxesFile)
    var boundingBoxes = new ListBuffer[List[(Double, Double)]]
    (root \\ "boundingBox").foreach(b => {
      val sw_long = (b \ "sw" \ "longitude").text
      val sw_lat = (b \ "sw" \ "latitude").text
      val ne_long = (b \ "ne" \ "longitude").text
      val ne_lat = (b \ "ne" \ "latitude").text

      boundingBoxes += List((sw_long.toDouble, sw_lat.toDouble), (ne_long.toDouble, ne_lat.toDouble))
    })
    boundingBoxes.toList
  }

  private[this] def isInBoundingBoxes(lng: String, lat: String): Boolean = {
    boundingBoxes.foreach(boundingBox => {
      val sw = boundingBox(0)
      val ne = boundingBox(1)
      if(sw._1 <= lng.toDouble && ne._1 >= lng.toDouble && sw._2 <= lat.toDouble && ne._2 >= lat.toDouble) return true
    })
    false
  }
}
