package org.puma.configuration

import scala.collection.mutable.ListBuffer
import scala.io.Source
import org.puma.analyzer.filter.ExtractorFilter
import scala.xml.{Elem, XML}
import java.io.{BufferedReader, File, InputStream}

/**
 * Project: puma
 * Package: org.puma.configuration
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
object ConfigurationUtil {

  /* ===========================================================
   *                     CONSTANTS
   * =========================================================== */
  private[this] val ModePropertyKey              = "mode"
  private[this] val LocalFilePropertyKey         = "local"
  private[this] val GlobalFilePropertyKey        = "global"
  private[this] val FiltersPropertyKey           = "filters"
  private[this] val FilterPropertyKey            = "filter"
  private[this] val MinFrequencyForLLRKey        = "minimumFrequency"
  private[this] val MaximumExtractedTermsKey     = "maximumExtractedTerms"
  private[this] val FactorToRemoveKey            = "factorToRemove"
  private[this] val FilePropertyKey              = "file"
  private[this] val FileToAnalyzePropertyKey     = "fileToAnalyze"
  private[this] val BoundingBoxesFilePropertyKey = "localBoundingBoxes"

  private[this] val LLRGeneratorKey              = "llrGenerator"
  private[this] val ScoreGeneratorKey            = "scoreGenerator"
  private[this] val LLRFilesPropertyKey          = "llrFiles"

  private[this] val StopWordsFileName            = "common-stop-words.txt"

  /* =========================================================== */

  private[this] var config: Configuration     = null
  private[this] var _XmlConfiguration: Elem   = null
  private[this] var _stopWords: Array[String] = null

  def load(config: Configuration): Unit = this.config = config

  def getMode: String = (XmlConfiguration \\ ModePropertyKey).text

  def getFilesToAnalyze: List[String] = {
    val local  = (XmlConfiguration \\ LLRGeneratorKey \\ LocalFilePropertyKey).text
    val global = (XmlConfiguration \\ LLRGeneratorKey \\ GlobalFilePropertyKey).text
    List(local, global)
  }

  def getFiltersToApply: Seq[ExtractorFilter] = {
    (XmlConfiguration \\ LLRGeneratorKey \\ FiltersPropertyKey \\ FilterPropertyKey)
      .map(n => Class.forName(n.text).newInstance.asInstanceOf[ExtractorFilter])
  }

  def getMinFrequencyForLLR: Int = (XmlConfiguration \\ LLRGeneratorKey \\ MinFrequencyForLLRKey).text.toInt

  def getMaximumExtractedTerms: Int = (XmlConfiguration \\ LLRGeneratorKey \\ MaximumExtractedTermsKey).text.toInt

  def getFactorToRemove: Float = (XmlConfiguration \\ LLRGeneratorKey \\ FactorToRemoveKey).text.toFloat

  def stopWords: Array[String] = {
    if(_stopWords == null) { _stopWords = loadStopWords }
    _stopWords
  }

  def getLLRResultFiles: Seq[String] = (XmlConfiguration \\ ScoreGeneratorKey \\ LLRFilesPropertyKey \\ FilePropertyKey).map(n => n.text)

  def getFileToAnalyze:String = (XmlConfiguration \\ ScoreGeneratorKey \\ FileToAnalyzePropertyKey).text

  def getBoundingBoxesFile: String = (XmlConfiguration \\ ScoreGeneratorKey \\ BoundingBoxesFilePropertyKey).text

  private[this] def XmlConfiguration: Elem = {
    if(_XmlConfiguration == null) { _XmlConfiguration = XML.loadFile(config.config) }
    _XmlConfiguration
  }

  private[this] def loadStopWords: Array[String] =
    Source.fromInputStream(ConfigurationUtil.getClass.getResourceAsStream(s"/$StopWordsFileName")).getLines().toArray
}
