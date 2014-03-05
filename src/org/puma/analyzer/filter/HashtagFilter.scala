package org.puma.analyzer.filter

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class HashtagFilter(filter: ExtractorFilter) extends ExtractorFilterDecorator(filter){
  def this() = this(new SimpleTermExtractorFilter())

  private[this] val extractor = new com.twitter.Extractor

  def extract(tweet: String): List[List[String]] = {
    val hashtags = filter.extract(tweet).to[ListBuffer] // initializing with previous extraction
    extractor.extractHashtags(tweet).asScala.foreach(term => {
      hashtags += List(term.toLowerCase)
    })
    hashtags.toList
  }
}
