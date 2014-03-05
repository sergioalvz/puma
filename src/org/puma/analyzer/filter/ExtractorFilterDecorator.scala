package org.puma.analyzer.filter

/**
 * Project: puma
 * Package: org.puma.analyzer.filter
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
abstract class ExtractorFilterDecorator(filter: ExtractorFilter) extends ExtractorFilter{
  def extract(tweet: String): List[List[String]]
}
