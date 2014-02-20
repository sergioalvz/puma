package org.puma.analyzer.filter

import org.puma.model.Term
import scala.collection.mutable.ListBuffer
import org.puma.configuration.ConfigurationUtil

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class BigramsFilter(filter: ExtractorFilter) extends ExtractorFilterDecorator(filter) {
  def this() = this(new SimpleTermExtractorFilter())

  private[this] val SymbolsToClean = Array('\\', ',', '(', '\'', ')', '{', '}', '?', '¿', '¡', '!', '.', '&', '%',
    '$', ';', ':', '+', '-', '*', '^', '/', '_', '\n', '\t', '=')

  def extract(tweet: String): List[Term] = {
    val bigrams = filter.extract(tweet).to[ListBuffer] // initializing with previous extraction
    val extractedBigrams = getBigrams(clear(tweet.toLowerCase.trim))
    extractedBigrams.foreach(bigram => {
      if (isValidBigram(bigram)) {
        val termToAdd = new Term(List(bigram(0), bigram(1)))
        if(!bigrams.contains(termToAdd)) bigrams += termToAdd
      }
    })
    bigrams.toList
  }

  private[this] def isValidBigram(bigram: Array[String]): Boolean = {
    bigram.size == 2 &&
    !bigram(0).trim.isEmpty &&
    !bigram(1).trim.isEmpty &&
    !(bigram(0).startsWith("@") || bigram(0).startsWith("#")) &&
    !(bigram(1).startsWith("@") || bigram(1).startsWith("#"))
  }

  private[this] def clear(raw: String): String = {
    raw.map[Char, String](char => if(SymbolsToClean.contains(char)) '\0' else char)
  }

  private[this] def getBigrams(tweet: String): List[Array[String]] = {
    val words = tweet.split(" ")
    val combinations = words.combinations(2).toList
    combinations.filter(terms => {
      !ConfigurationUtil.stopWords.contains(terms(0)) && !ConfigurationUtil.stopWords.contains(terms(1))
    }) // Removing those bigrams only composed by stop words
  }
}
