package org.puma.analyzer

import scala.collection.mutable
import org.apache.commons.math3.stat.inference.GTest
import org.puma.model.Term

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
object Analyzer {
  def analyze(source: String, target: String): Map[Term, Double] = {
    val results = mutable.Map.empty[Term, Double]
    val stats = new GTest()

    val sourceHashtags = Extractor.hashtags(source)
    val targetHashtags = Extractor.hashtags(target)

    sourceHashtags.keySet.foreach(term => {
      val targetCountOption = targetHashtags.get(term)
      if(targetCountOption != None) {
        val observed = Int.int2long(sourceHashtags.get(term).get)
        val expected = Int.int2long(targetCountOption.get)
        val llr = stats.gDataSetsComparison(Array(observed, observed), Array(expected, expected))

        results.put(term, llr)
      }
    })

    results.toMap
  }
}
