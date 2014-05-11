package org.puma.analyzer.filter

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class SimpleTermExtractorFilter extends ExtractorFilter{
  def extract(tweet: String): List[List[String]] = {
    List.empty[List[String]]
  }

  def field:String = "text"
}
