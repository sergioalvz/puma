package org.puma.analyzer.filter

import scala.collection.mutable.ListBuffer
import org.puma.analyzer.NgramExtractor

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class BigramsFilter(filter: ExtractorFilter) extends ExtractorFilterDecorator(filter) {
  def this() = this(new SimpleTermExtractorFilter())

  def extract(tweet: String): List[List[String]] = {
    val results = filter.extract(tweet).to[ListBuffer] // initializing with previous extraction
    NgramExtractor.extract(tweet, 2).foreach(ngram => results += ngram)
    results.toList
  }

  def field: String = "text"
}
