package org.puma.analyzer

import scala.io.Source
import scala.xml.pull._
import scala.xml.pull.EvElemStart
import scala.xml.pull.EvText
import scala.collection.mutable
import org.puma.analyzer.filter.ExtractorFilter
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.puma.configuration.ConfigurationUtil

/**
 * Project: puma
 * Package: org.puma.analyzer
 *
 * Author: Sergio Álvarez
 * Date: 09/2013
 */
class Extractor extends LazyLogging{
  private[this] var _path: String = null
  private[this] var _filter: ExtractorFilter = null

  private[this] var results               = mutable.Map.empty[List[String], Int]
  private[this] var minimumFreq           = 1
  private[this] val MaximumExtractedTerms = ConfigurationUtil.getMaximumExtractedTerms
  private[this] val FactorToRemove        = ConfigurationUtil.getFactorToRemove

  def path(value: String): Extractor = {
    _path = value
    this
  }

  def filter(value: ExtractorFilter): Extractor = {
    _filter = value
    this
  }

  def extract: Map[List[String], Int] = {
    if(_filter == null || _path == null){
      throw new IllegalArgumentException("You must provide a filter and valid path for making the extraction")
    }

    logger.debug("Extracting: " + _path + " with filter: " + _filter.getClass.getSimpleName)

    val reader = new XMLEventReader(Source.fromFile(_path))
    var in = false
    reader.foreach({
        case e: EvElemStart if e.label == _filter.field => in = true
        case EvText(text) if in => applyFilter(text)
        case e: EvElemEnd if e.label == _filter.field => in = false
        case _ => ;
    })
    results.toMap
  }

  private[this] def applyFilter(tweet: String) = {
    checkMemoryStatus()
    _filter.extract(tweet).foreach(term => {
      if(results.contains(term)){
        results(term) += 1
      }else{
        results(term) = minimumFreq
      }
    })
  }

  private[this] def checkMemoryStatus() = {
    if(results.keys.size >= MaximumExtractedTerms) {
      logger.debug("Memory overload. Maximum limit for extracted terms have been reached. Reducing map...")
      reduceMapLoad()
    }
  }

  private[this] def reduceMapLoad() = {
    val itemsToRemove = (results.keys.size * FactorToRemove).toInt
    logger.debug("They are going to be removed " + itemsToRemove + " items")

    val orderedList = results.toList.sortBy({_._2})
    minimumFreq = orderedList(itemsToRemove - 1)._2
    logger.debug("New minimum frequency is " + minimumFreq)

    val reduced = orderedList.slice(itemsToRemove - 1, orderedList.size)
    results = collection.mutable.Map(reduced.toMap[List[String], Int].toSeq: _*) // converting to mutable map
    logger.debug("Reduced map contains " + results.keys.size + " terms")
  }
}
