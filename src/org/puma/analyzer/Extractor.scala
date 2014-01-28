package org.puma.analyzer

import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText
import scala.collection.mutable
import org.puma.model.Term
import org.puma.analyzer.filter.ExtractorFilter
import com.typesafe.scalalogging.log4j.Logging

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
class Extractor extends Logging{
  private[this] var _path: String = null
  private[this] var _filter: ExtractorFilter = null

  private[this] var results = mutable.Map.empty[Term, Int]
  private[this] var minimumFrequency = 1

  private[this] val MaximumExtractedTerms = 500000 // 500,000


  def path = _path
  def filter = _filter

  def path_= (value: String): Unit = _path = value
  def filter_= (value: ExtractorFilter): Unit = _filter = value

  def extract: Map[Term, Int] = {
    if(_filter == null || _path == null)
      throw new IllegalArgumentException("You must provide a filter and valid path for making the extraction")

    logger.debug(s"Extracting: $path with filter: $filter")

    results.clear()       // clearing previous
    minimumFrequency = 1  // extraction
    logger.debug(s"Clearing results. Results has currently ${results.size} elements. Minimum Frequency is " +
      s"$minimumFrequency")

    val reader = new XMLEventReader(Source.fromFile(_path))
    var isTweetTextNode = false
    for (event <- reader) {
      event match {
        case EvElemStart(_, "text", _, _) => isTweetTextNode = true
        case EvText(text) if isTweetTextNode => applyFilter(text)
        case EvElemEnd(_, "text") => isTweetTextNode = false
        case _ => ;
      }
    }

    results.toMap
  }

  private[this] def applyFilter(tweet: String) = {
    checkMemoryStatus()
    _filter.extract(tweet).foreach(term => {
      if(results.contains(term)){
        results(term) += 1
      }else{
        results(term) = minimumFrequency
      }
    })
  }

  private[this] def checkMemoryStatus() = {
    if(results.keys.size >= MaximumExtractedTerms) {
      logger.debug(s"Memory overload. Maximum limit for extracted terms have been reached. Reducing map...")
      reduceMapLoad()
    }
  }

  private[this] def reduceMapLoad() = {
    val itemsToRemove = (results.keys.size * 0.4).toInt
    logger.debug(s"They are going to be removed $itemsToRemove items")
    
    val orderedList = results.toList.sortBy({_._2})
    minimumFrequency = orderedList(itemsToRemove - 1)._2
    logger.debug(s"New minimum frequency is $minimumFrequency")

    val reduced = orderedList.slice(itemsToRemove - 1, orderedList.size)
    results = collection.mutable.Map(reduced.toMap[Term, Int].toSeq: _*) // converting to mutable map
    logger.debug(s"Reduced map contains ${results.keys.size} terms")
  }
}
