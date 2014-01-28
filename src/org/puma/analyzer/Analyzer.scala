package org.puma.analyzer

import scala.collection.mutable
import org.apache.commons.math3.stat.inference.GTest
import org.puma.model.Term
import org.puma.analyzer.filter.{MentionFilter, SimpleTermExtractorFilter, HashtagFilter}

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
    val extractor = new Extractor()
    val commonFrequencies = mutable.Map.empty[Int, Int]
    val notDefinedAtGlobal = mutable.Map.empty[Term, Int]
    
    extractor.filter = new HashtagFilter(new MentionFilter(new SimpleTermExtractorFilter()))
    extractor.path = local
    val localTerms = extractor.extract
    
    extractor.path = global
    val globalTerms = extractor.extract

    localTerms.keys.foreach(term => {
      val globalCountOption = globalTerms.get(term)
      if(globalCountOption.isDefined) {
        val observed = Int.int2long(localTerms.get(term).get)
        val expected = Int.int2long(globalCountOption.get)
        val llr = stats.gDataSetsComparison(Array(observed, observed), Array(expected, expected))
        results.put(term, llr)
      }else {
        notDefinedAtGlobal.put(term, localTerms.get(term).get)
      }
    })

    if(!notDefinedAtGlobal.isEmpty) {
      notDefinedAtGlobal.keys.foreach(term => {
        var avgFrequencyAtGlobal: Int = 0
        if(commonFrequencies.get(localTerms.get(term).get).isDefined){
          avgFrequencyAtGlobal = commonFrequencies.get(localTerms.get(term).get).get
        }else{
          val termsWithSameFrequency = localTerms.keys.filter((localTerm) => {
            globalTerms.get(localTerm).isDefined &&
              localTerms.get(localTerm).get == notDefinedAtGlobal.get(term).get
          })
          var avg = 0
          if(!termsWithSameFrequency.isEmpty) {
            termsWithSameFrequency.foreach(term => {
              avg += globalTerms.get(term).get
            })
            avg /= termsWithSameFrequency.size
          }
          commonFrequencies.put(localTerms.get(term).get, avg)
        }
        if(avgFrequencyAtGlobal > 0) {
          val observed = Int.int2long(notDefinedAtGlobal.get(term).get)
          val expected = Int.int2long(avgFrequencyAtGlobal)
          val llr = stats.gDataSetsComparison(Array(observed, observed), Array(expected, expected))
          results.put(term, llr)
        }
      })
    }

    results.toMap
  }
}
