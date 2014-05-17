package org.puma.configuration

import scala.collection.mutable.ListBuffer
import scala.io.Source
import org.puma.analyzer.filter.ExtractorFilter
import scala.xml.{Elem, XML}
import java.io.{BufferedReader, File}

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

  private[this] val ConfigurationFileName        = "configuration.xml"
  private[this] val FilesToAnalyzeDirectoryName  = "files_to_analyze"
  private[this] val OutputFilesDirectoryName     = "results"
  /* =========================================================== */


  def getExecutableAbsolutePath: String = {
    val absolutePath = ConfigurationUtil.getClass.getProtectionDomain.getCodeSource.getLocation.getPath
    absolutePath.substring(0, absolutePath.lastIndexOf('/') + 1)
  }

  def getFilesToAnalyzeDirAbsolutePath:String = s"$getExecutableAbsolutePath$FilesToAnalyzeDirectoryName/"
  def getOutputFilesDirAbsolutePath:String    = s"$getExecutableAbsolutePath$OutputFilesDirectoryName/"

  /* ===========================================================
   *                 LOAD STOP WORDS
   * =========================================================== */
  private[this] val stopWordsList = Source.fromInputStream(getStopWordsFileStream).getLines().toArray
  private[this] def getStopWordsFileStream = {
    ConfigurationUtil.getClass.getResourceAsStream(s"/$StopWordsFileName")
  }
  /* =========================================================== */


  /* ===========================================================
   *                 LOAD CONFIGURATION XML
   * =========================================================== */
  private[this] val configuration: Elem = loadConfiguration

  private[this] def loadConfiguration = XML.load(getConfigurationFile)

  private[this] def getConfigurationFile: BufferedReader = {
    val custom = new File(s"$getExecutableAbsolutePath$ConfigurationFileName")
    Source.fromFile(custom).bufferedReader()
  }
  /* =========================================================== */


  def getMode: String = (configuration \\ "mode").text

  def getFilesToAnalyze: List[String] = {
    val local  = getFilesToAnalyzeDirAbsolutePath + (configuration \\ LLRGeneratorKey \\ LocalFilePropertyKey).text
    val global = getFilesToAnalyzeDirAbsolutePath + (configuration \\ LLRGeneratorKey \\ GlobalFilePropertyKey).text
    List(local, global)
  }

  def getFiltersToApply: Seq[ExtractorFilter] =
    (configuration \\ LLRGeneratorKey \\ FiltersPropertyKey \\ FilterPropertyKey).map(n =>
      Class.forName(n.text).newInstance.asInstanceOf[ExtractorFilter])

  def getMinFrequencyForLLR: Int = (configuration \\ LLRGeneratorKey \\ MinFrequencyForLLRKey).text.toInt

  def getMaximumExtractedTerms: Int = (configuration \\ LLRGeneratorKey \\ MaximumExtractedTermsKey).text.toInt

  def getFactorToRemove: Float = (configuration \\ LLRGeneratorKey \\ FactorToRemoveKey).text.toFloat

  def stopWords: Array[String] = stopWordsList

  def getLLRResultFiles: Array[String] = {
    var files = new ListBuffer[String]
    (configuration \\ ScoreGeneratorKey \\ LLRFilesPropertyKey \\ FilePropertyKey).foreach(node => files += node.text)
    files.toArray[String]
  }

  def getFileToAnalyze:String = (configuration \\ ScoreGeneratorKey \\ FileToAnalyzePropertyKey).text

  def getBoundingBoxesFile:String = (configuration \\ ScoreGeneratorKey \\ BoundingBoxesFilePropertyKey).text
}
