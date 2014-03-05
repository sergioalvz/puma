package org.puma.analyzer

import scala.collection.mutable
import org.puma.configuration.ConfigurationUtil
import org.puma.stat.Dunning

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class Analyzer(local: String, global: String) {

  private[this] val localTerms  = new Extractor().filter(ConfigurationUtil.getFilterToApply).path(local).extract
  private[this] val globalTerms = new Extractor().filter(ConfigurationUtil.getFilterToApply).path(global).extract

  private[this] val totalLocalFrequencies  = localTerms.foldLeft(0)(_+_._2)
  private[this] val totalGlobalFrequencies = globalTerms.foldLeft(0)(_+_._2)

  private[this] val results             = mutable.Map.empty[List[String], Double]
  private[this] val commonFreq          = mutable.Map.empty[Int, Int]
  private[this] val minFrequencyLLR     = ConfigurationUtil.getMinFrequencyForLLR

  def analyze: List[(List[String], Double)] = {
    localTerms.keys.foreach(terms => {
      val localFreq = localTerms.get(terms).get.toLong
      if(localFreq > minFrequencyLLR) {        
        val globalOption = globalTerms.get(terms)
        val globalFreq:Long = if (globalOption.isDefined) globalOption.get else calculateAverageGlobalFrequency(terms)

        if(globalFreq > 0) {
          val k11 = localFreq
          val k12 = globalFreq
          val k21 = totalLocalFrequencies
          val k22 = totalGlobalFrequencies
          val llr = Dunning.normalizedRootLogLikelihoodRatio(k11, k12, k21, k22)
          results.put(terms, llr)
        }
      }
    })
    results.toList.sortBy({ _._2 })
  }

  private[this] def calculateAverageGlobalFrequency(terms: List[String]): Int = {
    if(commonFreq.get(localTerms.get(terms).get).isDefined) return commonFreq.get(localTerms.get(terms).get).get
    val sameFreq = localTerms.filterKeys((localTerm) =>
      globalTerms.get(localTerm).isDefined && localTerms.get(localTerm).get == localTerms.get(terms).get
    )
    val avg = if(!sameFreq.isEmpty) sameFreq.foldLeft(0)(_+_._2) / sameFreq.size else 0
    commonFreq.put(localTerms.get(terms).get, avg)
    avg
  }
}