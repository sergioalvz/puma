package org.puma.analyzer

import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
class Analyzer(path: String) {
  def analyze() = {
    val reader = new XMLEventReader(Source.fromFile(path))
    var isTweetTextNode = false
    for(event <- reader){
      event match{
        case EvElemStart(_, "text",_,_) => isTweetTextNode = true
        case EvText(text) if isTweetTextNode => analyzeTweet(text)
        case EvElemEnd(_, "text") => isTweetTextNode = false
        case _ => ;
      }
    }
  }

  def analyzeTweet(tweet: String) = {
    val formatted = tweet.replace("@", "__MT__").replace("#", "__HT__")
    val pattern = "\\b(__MT__|__HT__)?[a-zA-Z0-9]+\\b".r
    pattern.findAllIn(formatted).foreach(result => {
      if(result.startsWith("__MT__")){
        println("MENCIÓN: " + result)
      }else if(result.startsWith("__HT__")){
        println("HASHTAG: " + result)
      }
    })
  }
}
