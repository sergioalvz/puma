package org.puma.analyzer.filter

import org.puma.model.Term
import scala.collection.mutable.ListBuffer

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class BigramsFilter(filter: ExtractorFilter) extends ExtractorFilterDecorator(filter) {
  private[this] val SymbolsToClean = Array("\\", ",", "(", "\'", ")", "{", "}", "?", "¿", "¡", "!", ".", "&", "%",
    "$", ";", ":", "+", "-", "*", "^", "/", "_", "\n", "\t")

  def extract(tweet: String): List[Term] = {
    val bigrams = filter.extract(tweet).to[ListBuffer] // initializing with previous extraction

    val extractedBigrams = ngram(clear(tweet.trim), 2)
    extractedBigrams.foreach(bigram => {
      if (isValidBigram(bigram)) {
        bigrams += new Term(List(bigram(0).toLowerCase, bigram(1).toLowerCase))
      }
    })
    bigrams.toList
  }

  private[this] def isValidBigram(bigram: List[String]): Boolean = {
    bigram.size == 2 && !bigram(0).trim.isEmpty && !bigram(1).trim.isEmpty &&
      !(bigram(0).startsWith("@") || bigram(0).startsWith("#")) &&
      !(bigram(1).startsWith("@") || bigram(1).startsWith("#"))
  }

  private[this] def clear(raw: String): String = {
    var replaced = raw
    for (symbol <- SymbolsToClean) {
      replaced = replaced.replace(symbol, "")
    }
    replaced
  }

  /*
  * =========================================================
  *   Extracted from: https://gist.github.com/beala/3254234
  * =========================================================
  */
  private[this] def ngram(sent: String, n: Int): List[List[String]] = {
    def helper(sent: List[String]): List[List[String]] = {
      sent match {
        case (h :: t) if sent.length >= n => sent.take(n) :: helper(t)
        case (_) => Nil
      }
    }
    helper(sent.split(' ').toList)
  }


}
