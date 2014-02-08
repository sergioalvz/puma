package org.puma.analyzer.filter

import org.puma.model.Term
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class MentionFilter(filter: ExtractorFilter) extends ExtractorFilterDecorator(filter) {
  def this() = this(new SimpleTermExtractorFilter())

  private[this] val extractor = new com.twitter.Extractor

  def extract(tweet: String): List[Term] = {
    val mentions = filter.extract(tweet).to[ListBuffer] // initializing with previous extraction
    extractor.extractMentionedScreennames(tweet).asScala.foreach(term => {
      mentions += new Term(List(term.toLowerCase))
    })
    mentions.toList
  }

}
