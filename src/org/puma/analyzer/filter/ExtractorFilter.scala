package org.puma.analyzer.filter


/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
trait ExtractorFilter {
  def extract(tweet: String): List[List[String]]
}
