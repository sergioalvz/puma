package org.puma.analyzer

import scala.io.Source

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
class Analyzer(path: String) {
  def numberOfTweets = Source.fromFile(path).getLines().length
}
