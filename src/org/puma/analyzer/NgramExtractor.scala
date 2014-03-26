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

  def extract(tweet:String, count:Int, allowMentionsAndHashtags:Boolean = false): List[List[String]] = {
    var result = new ListBuffer[List[String]]
    val ngrams = getNgrams(clear(tweet), count)
    ngrams.foreach(ngram => {
      if (isValidNgram(ngram, count, allowMentionsAndHashtags)) {
        val termToAdd = ngram.map(ngram => ngram.trim).toList
        if(!result.contains(termToAdd)) result += termToAdd
      }
    })
    result.toList
  }

  private[this] def isValidNgram(ngram:Array[String], count:Int, allowMentionsAndHashtags:Boolean): Boolean = {
    ngram.size == count &&
    ngram.find(term => term.trim.isEmpty) == None &&
    ngram.find(term => term.trim.length == 1) == None &&
    isValidCharSequence(ngram, allowMentionsAndHashtags) &&
    !ngram.forall(term => if(count > 1) term.trim == ngram(0).trim else false) &&
    !hasStopWords(ngram, allowMentionsAndHashtags)
  }

  private[this] def isValidCharSequence(ngram:Array[String], allowMentionsAndHashtags:Boolean): Boolean = {
    if(allowMentionsAndHashtags) {
      ngram.find(term => !term.trim.matches("^(@|#)?[a-záéíóú]*")) == None
    }else {
      ngram.find(term => !term.trim.matches("[a-záéíóú]*")) == None
    }
  }

  private[this] def hasStopWords(ngram:Array[String], allowMentionsAndHashtags:Boolean): Boolean = {
    ngram.exists(term => {
      if(allowMentionsAndHashtags && (term.startsWith("@") || term.startsWith("#"))) {
        false
      }else {
        ConfigurationUtil.stopWords.contains(term.trim)
      }
    })
  }

  private[this] def clear(raw:String): String = {
    raw.toLowerCase.trim.map[Char, String](char => if(SymbolsToClean.contains(char)) '\0' else char)
  }

  private[this] def getNgrams(tweet:String, count: Int): List[Array[String]] = {
    val words = tweet.split(" ")
    words.combinations(count).toList
  }
}
