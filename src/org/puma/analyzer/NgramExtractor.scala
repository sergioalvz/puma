package org.puma.analyzer

import org.puma.configuration.ConfigurationUtil
import scala.collection.mutable.ListBuffer

/**
 * Project: puma
 * Package: org.puma.analyzer.filter
 *
 * Author: Sergio Álvarez
 * Date: 03/2014
 */
object NgramExtractor {
  private[this] val SymbolsToClean = Array('\\', ',', '(', '\'', ')', '{', '}', '?', '¿', '¡', '!', '.', '&', '%',
    '$', ';', ':', '+', '-', '*', '^', '/', '_', '\n', '\t', '=', '|')

  def extract(tweet: String, count: Int): List[List[String]] = {
    var result = new ListBuffer[List[String]]
    val ngrams = getNgrams(clear(tweet), count)
    ngrams.foreach(ngram => {
      if (isValidNgram(ngram, count)) {
        val termToAdd = ngram.map(ngram => ngram.trim).toList
        if(!result.contains(termToAdd)) result += termToAdd
      }
    })
    result.toList
  }

  private[this] def isValidNgram(ngram: Array[String], count: Int): Boolean = {
    ngram.size == count &&
    ngram.find(term => term.trim.isEmpty) == None &&
    ngram.find(term => term.trim.length == 1) == None &&
    ngram.find(term => !term.trim.matches("[a-záéíóú]*")) == None &&
    !ngram.forall(term => if(count > 1) term.trim == ngram(0).trim else false) &&
    ngram.find(term => term.startsWith("@") || term.startsWith("#")) == None &&
    ngram.find(term => ConfigurationUtil.stopWords.contains(term.trim)) == None
  }

  private[this] def clear(raw: String): String = {
    raw.toLowerCase.trim.map[Char, String](char => if(SymbolsToClean.contains(char)) '\0' else char)
  }

  private[this] def getNgrams(tweet: String, count: Int): List[Array[String]] = {
    val words = tweet.split(" ")
    words.combinations(count).toList
  }
}
