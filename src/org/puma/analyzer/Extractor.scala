package org.puma.analyzer

import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText
import scala.collection.mutable
import org.puma.model.Term
import org.puma.analyzer.filter.ExtractorFilter

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
object Extractor {

  def extract(path: String, filter: ExtractorFilter): Map[Term, Int] = {
    val results = mutable.Map.empty[Term, Int]

    val reader = new XMLEventReader(Source.fromFile(path))
    var isTweetTextNode = false
    for (event <- reader) {
      event match {
        case EvElemStart(_, "text", _, _) => isTweetTextNode = true

        case EvText(text) if isTweetTextNode => {
          filter.extract(text).foreach(term => {
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
