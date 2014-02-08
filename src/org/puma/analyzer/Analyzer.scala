package org.puma.analyzer

import scala.collection.mutable
import org.apache.commons.math3.stat.inference.GTest
import org.puma.model.Term
import org.puma.analyzer.filter.{MentionFilter, BigramsFilter, SimpleTermExtractorFilter, HashtagFilter}

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class Analyzer(local: String, global: String) {

  private[this] var localTerms:  Map[Term, Int] = null
  private[this] var globalTerms: Map[Term, Int] = null

  private[this] val stats               = new GTest()
  private[this] val results             = mutable.Map.empty[Term, Double]
  private[this] val commonFrequencies   = mutable.Map.empty[Int, Int]

  def analyze: List[(Term, Double)] = {

    localTerms = new Extractor()
      .filter(new HashtagFilter(new SimpleTermExtractorFilter()))
      .path(local)
      .extract

    globalTerms = new Extractor()
      .filter(new HashtagFilter(new SimpleTermExtractorFilter()))
      .path(global)
      .extract

    val totalLocalFrequencies  = localTerms.foldLeft(0)(_+_._2)
    val globalLocalFrequencies = globalTerms.foldLeft(0)(_+_._2)

    localTerms.keys.foreach(term => {
      val localFrequency = localTerms.get(term).get.toLong
      var globalFrequency:Long = 0

      if(localFrequency > 500) {
        val globalCountOption = globalTerms.get(term)
        if(globalCountOption.isDefined)
          globalFrequency = globalCountOption.get.toLong
        else
          globalFrequency = calculateAverageGlobalFrequency(term).toLong

        val k11 = globalFrequency + localFrequency
        val k22 = (totalLocalFrequencies + globalLocalFrequencies) - k11
        val llr = stats.rootLogLikelihoodRatio(k11, globalFrequency, localFrequency, k22)
        results.put(term, llr)
      }
    })

    results.toList.sortBy({ _._2 })
  }

  private[this] def calculateAverageGlobalFrequency(term: Term): Int = {
    if(commonFrequencies.get(localTerms.get(term).get).isDefined){
      return commonFrequencies.get(localTerms.get(term).get).get
    }
    val termsWithSameFrequency = localTerms.filterKeys((localTerm) => {
      globalTerms.get(localTerm).isDefined &&
        localTerms.get(localTerm).get == localTerms.get(term).get
    })
    var avg = 0
    if(!termsWithSameFrequency.isEmpty){
      avg = termsWithSameFrequency.foldLeft(0)(_+_._2) / termsWithSameFrequency.size
    }
    commonFrequencies.put(localTerms.get(term).get, avg)
    avg
  }
}
