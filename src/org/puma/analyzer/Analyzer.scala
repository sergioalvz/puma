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
  def analyze(local: String, global: String): Map[Term, Double] = {
    val results = mutable.Map.empty[Term, Double]
    val stats = new GTest()

    val localHashtags = Extractor.hashtags(local)
    val globalHashtags = Extractor.hashtags(global)

    val notDefinedAtGlobal = mutable.Map.empty[Term, Int]

    localHashtags.keys.foreach(term => {
      val globalCountOption = globalHashtags.get(term)
      if(globalCountOption.isDefined) {
        val observed = Int.int2long(localHashtags.get(term).get)
        val expected = Int.int2long(globalCountOption.get)
        val llr = stats.gDataSetsComparison(Array(observed, observed), Array(expected, expected))
        results.put(term, llr)
      }else {
        notDefinedAtGlobal.put(term, localHashtags.get(term).get)
      }
    })

    if(!notDefinedAtGlobal.isEmpty) {
      notDefinedAtGlobal.keys.foreach(term => {
        val termsWithSameFrequency = localHashtags.keys.filter((localTerm) => {
          globalHashtags.get(localTerm).isDefined && localHashtags.get(localTerm).get == notDefinedAtGlobal.get(term).get
        })

        if(termsWithSameFrequency.size > 0){
          var avg = 0
          termsWithSameFrequency.foreach(term => {
            avg += globalHashtags.get(term).get
          })
          avg /= termsWithSameFrequency.size

          val observed = Int.int2long(notDefinedAtGlobal.get(term).get)
          val expected = Int.int2long(avg)
          val llr = stats.gDataSetsComparison(Array(observed, observed), Array(expected, expected))
          results.put(term, llr)
        }
      })
    }

    results.toMap
  }
}
