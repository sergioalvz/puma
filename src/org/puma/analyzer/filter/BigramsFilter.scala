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
        val termToAdd = new Term(List(bigram(0).trim, bigram(1).trim))
        if(!bigrams.contains(termToAdd)) bigrams += termToAdd
      }
    })
    bigrams.toList
  }

  private[this] def isValidBigram(bigram: Array[String]): Boolean = {
    bigram.size == 2 && // just 2 words for bigram
    !bigram(0).trim.isEmpty && // bigram not emtpy
    !bigram(1).trim.isEmpty && // bigram not empty
    (bigram(0).trim.length > 1 && bigram(1).trim.length > 1) && // rejecting 'a a' 't t' etc...
    (bigram(0).trim.matches("[a-záéíóú]*") && bigram(1).trim.matches("[a-záéíóú]*")) && // just for filtering...
    bigram(0).trim != bigram(1).trim && // rejecting bigrams composed by the same words 'day day' 'to to' etc...
    !(bigram(0).startsWith("@") || bigram(0).startsWith("#")) && // rejecting mentions and hashtags
    !(bigram(1).startsWith("@") || bigram(1).startsWith("#")) && // rejecting mentions and hashtags
    !ConfigurationUtil.stopWords.contains(bigram(0).trim) && // rejecting bigrams with stopwords
    !ConfigurationUtil.stopWords.contains(bigram(1).trim) // rejecting bigrams with stopwords
  }

  private[this] def clear(raw: String): String = {
    raw.map[Char, String](char => if(SymbolsToClean.contains(char)) '\0' else char)
  }

  private[this] def getBigrams(tweet: String): List[Array[String]] = {
    val words = tweet.split(" ")
    words.combinations(2).toList
  }
}
