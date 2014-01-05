package org.puma.configuration

import scala.collection.mutable.ListBuffer
import java.util.Properties

/**
 * Project: puma
 * Package: org.puma.configuration
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
object ConfigurationUtil {

  private[this] val FilesPropertyKey = "files"

  def getFilesToAnalyze: List[String] = {
    val files = new ListBuffer[String]

    val properties = new Properties()
    properties.load(ConfigurationUtil.getClass.getResourceAsStream("/configuration.properties"))

    properties.getProperty(FilesPropertyKey).split(";").foreach( file => files += file )

    files.toList
  }
}
