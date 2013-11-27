package org.puma.analyzer

import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText
import com.twitter.Extractor
import scala.collection.JavaConverters._
import scala.collection.mutable
import java.io.{Reader, StringReader}
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.es.SpanishAnalyzer
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import java.util.StringTokenizer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import scala.reflect.internal.util.StringOps

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
class Analyzer(path: String) {
  private[this] val mentionsMap = mutable.Map.empty[String, Int]
  private[this] val hashtagsMap = mutable.Map.empty[String, Int]
  private[this] val bigramsMap = mutable.Map.empty[String, Int]
  private[this] val extractor = new Extractor()

  def mentions: mutable.Map[String, Int] = {
    def add(tweet: String) {
      extractor.extractMentionedScreennames(tweet).asScala.foreach(mention => {
        if (mentionsMap.contains(mention))
          mentionsMap(mention) += 1
        else
          mentionsMap(mention) = 1
      })
    }
    mentionsMap.clear()
    analyze(add)
    mentionsMap
  }

  def hashtags: mutable.Map[String, Int] = {
    def add(tweet: String) {
      extractor.extractHashtags(tweet).asScala.foreach(hashtag => {
        if (hashtagsMap.contains(hashtag))
          hashtagsMap(hashtag) += 1
        else
          hashtagsMap(hashtag) = 1
      })
    }
    hashtagsMap.clear()
    analyze(add)
    hashtagsMap
  }

  def bigrams: mutable.Map[String, Int] = {
    def add(tweet: String) {
      val result = ngram(clear(tweet.trim), 2)
      result.foreach(words => {
        if (words.size == 2 && !words(0).trim.isEmpty && !words(1).trim.isEmpty) {
          if (!(words(0).startsWith("@") || words(0).startsWith("#")) && !(words(1).startsWith("@") || words(1)
            .startsWith("#"))) {
            println(words(0) + " " + words(1))
          }
        }
      })
    }
    bigramsMap.clear()
    analyze(add)
    bigramsMap
  }

  private[this] def analyze(add: (String) => Unit) = {
    val reader = new XMLEventReader(Source.fromFile(path))
    var isTweetTextNode = false
    for (event <- reader) {
      event match {
        case EvElemStart(_, "text", _, _) => isTweetTextNode = true
        case EvText(text) if isTweetTextNode => add(text)
        case EvElemEnd(_, "text") => isTweetTextNode = false
        case _ => ;
      }
    }
  }

  private[this] def clear(raw: String): String = {
    val symbols = Array("\\", ",", "(", "\'", ")", "{", "}", "?", "¿", "¡", "!", ".", "&", "%", "$", ";", ":", "+",
      "-", "*", "^", "/", "_")
    var replaced = raw
    for (symbol <- symbols) {
      replaced = replaced.replace(symbol, "")
    }
    replaced
  }

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
