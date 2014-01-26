package org.puma.analyzer.filter

import org.puma.model.Term

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class TermExtractorFilter extends ExtractorFilter{
  def extract(tweet: String): List[Term] = {
    List.empty[Term]
  }
}
