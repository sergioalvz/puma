package org.puma.analyzer

import scala.collection.mutable
import org.puma.model.Term
import org.puma.configuration.ConfigurationUtil
import org.puma.stat.Dunning
import com.typesafe.scalalogging.log4j.Logging

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

  private[this] val results             = mutable.Map.empty[Term, Double]
  private[this] val commonFreq          = mutable.Map.empty[Int, Int]
  private[this] val minFrequencyLLR     = ConfigurationUtil.getMinFrequencyForLLR

  def analyze: List[(Term, Double)] = {
    localTerms.keys.foreach(term => {
      val localFreq = localTerms.get(term).get.toLong
      if(localFreq > minFrequencyLLR) {        
        val globalOption = globalTerms.get(term)
        val globalFreq:Long = if (globalOption.isDefined) globalOption.get else calculateAverageGlobalFrequency(term)

        if(globalFreq > 0) {
          val k11 = localFreq
          val k12 = globalFreq
          val k21 = totalLocalFrequencies
          val k22 = totalGlobalFrequencies
          val llr = Dunning.rootLogLikelihoodRatio(k11, k12, k21, k22)
          results.put(term, llr)
        }
      }
    })
    results.toList.sortBy({ _._2 })
  }

  private[this] def calculateAverageGlobalFrequency(term: Term): Int = {
    if(commonFreq.get(localTerms.get(term).get).isDefined) return commonFreq.get(localTerms.get(term).get).get
    val sameFreq = localTerms.filterKeys((localTerm) =>
      globalTerms.get(localTerm).isDefined && localTerms.get(localTerm).get == localTerms.get(term).get
    )
    val avg = if(!sameFreq.isEmpty) sameFreq.foldLeft(0)(_+_._2) / sameFreq.size else 0
    commonFreq.put(localTerms.get(term).get, avg)
    avg
  }
}