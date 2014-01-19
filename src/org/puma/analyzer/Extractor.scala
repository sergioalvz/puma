package org.puma.analyzer

import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.puma.model.Term

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
object Extractor {
  private[this] val extractor = new com.twitter.Extractor()
  private[this] val SymbolsToClean = Array("\\", ",", "(", "\'", ")", "{", "}", "?", "¿", "¡", "!", ".", "&", "%",
    "$", ";", ":", "+", "-", "*", "^", "/", "_", "\n", "\t")

  /* ========================================================
                      PUBLIC METHODS
     ======================================================== */
  def mentions(path: String): Map[Term, Int] = {
    def get(tweet: String): List[Term] =
      toListOfExtractedResult(extractor.extractMentionedScreennames(tweet))
    extract(path, get)
  }

  def hashtags(path: String): Map[Term, Int] = {
    def get(tweet: String): List[Term] =
      toListOfExtractedResult(extractor.extractHashtags(tweet))
    extract(path, get)
  }

  def bigrams(path: String): Map[Term, Int] = {
    def get(tweet: String): List[Term] = {
      val bigrams = new ListBuffer[Term]

      val extractedBigrams = ngram(clear(tweet.trim), 2)
      extractedBigrams.foreach(bigram => {
        if (isValidBigram(bigram)) {
          bigrams += new Term(List(bigram(0), bigram(1)))
        }
      })
      bigrams.toList
    }

    extract(path, get)
  }

  /* ========================================================
                      PRIVATE METHODS
     ======================================================== */

  private[this] def toListOfExtractedResult(terms: java.util.List[String]): List[Term] = {
    val listOfStrings = new ListBuffer[Term]
    terms.asScala.foreach(term => {
      listOfStrings += new Term(List(term))
    })
    listOfStrings.toList
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

  private[this] def extract(path: String, get: (String) => List[Term]): Map[Term, Int] = {
    val results = mutable.Map.empty[Term, Int]

    val reader = new XMLEventReader(Source.fromFile(path))
    var isTweetTextNode = false
    for (event <- reader) {
      event match {
        case EvElemStart(_, "text", _, _) => isTweetTextNode = true
        case EvText(text) if isTweetTextNode => {
          get(text).foreach(term => {
            if(results.contains(term))
              results(term) += 1
            else
              results(term) = 1
          })
        }
        case EvElemEnd(_, "text") => isTweetTextNode = false
        case _ => ;
      }
    }

    results.toMap
  }
}
