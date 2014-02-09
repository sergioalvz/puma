package org.puma.configuration

import scala.collection.mutable.ListBuffer
import java.util.Properties
import scala.io.Source
import org.puma.analyzer.filter.ExtractorFilter

/**
 * Project: puma
 * Package: org.puma.configuration
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
object ConfigurationUtil {

  private[this] val LocalFilePropertyKey        = "local"
  private[this] val GlobalFilePropertyKey       = "global"
  private[this] val FilterPropertyKey           = "filter"
  private[this] val MinFrequencyForLLRKey       = "min_frequency_llr"
  private[this] val MaximumExtractedTermsKey    = "maximum_extracted_terms"
  private[this] val FactorToRemoveKey           = "factor_to_remove"

  private[this] val PropertiesFileName          = "configuration.properties"
  private[this] val FilesToAnalyzeDirectoryName = "files_to_analyze"
  private[this] val OutputFilesDirectoryName    = "results"

  def getFilesToAnalyze: List[String] = {
    val files = new ListBuffer[String]
    val properties = new Properties()

    properties.load(Source.fromFile(getExecutableAbsolutePath + PropertiesFileName).bufferedReader())
    files += getFilesToAnalyzeAbsolutePath + properties.getProperty(LocalFilePropertyKey)
    files += getFilesToAnalyzeAbsolutePath + properties.getProperty(GlobalFilePropertyKey)
    files.toList
  }

  def getExecutableAbsolutePath: String = {
    val absolutePath = ConfigurationUtil.getClass.getProtectionDomain.getCodeSource.getLocation.getPath
    absolutePath.substring(0, absolutePath.lastIndexOf('/') + 1)
  }

  def getFilesToAnalyzeAbsolutePath: String =  getExecutableAbsolutePath + FilesToAnalyzeDirectoryName + "/"

  def getFilterToApply: ExtractorFilter = {
    val properties = new Properties()
    properties.load(Source.fromFile(getExecutableAbsolutePath + PropertiesFileName).bufferedReader())
    Class.forName(properties.getProperty(FilterPropertyKey)).newInstance.asInstanceOf[ExtractorFilter]
  }

  def getMinFrequencyForLLR: Int = {
    val properties = new Properties()
    properties.load(Source.fromFile(getExecutableAbsolutePath + PropertiesFileName).bufferedReader())
    properties.getProperty(MinFrequencyForLLRKey).toInt
  }

  def getMaximumExtractedTerms: Int = {
    val properties = new Properties()
    properties.load(Source.fromFile(getExecutableAbsolutePath + PropertiesFileName).bufferedReader())
    properties.getProperty(MaximumExtractedTermsKey).toInt
  }

  def getFactorToRemove: Float = {
    val properties = new Properties()
    properties.load(Source.fromFile(getExecutableAbsolutePath + PropertiesFileName).bufferedReader())
    properties.getProperty(FactorToRemoveKey).toFloat
  }

  def getOutputFilesDirAbsolutePath: String = getExecutableAbsolutePath + OutputFilesDirectoryName + "/"
}
