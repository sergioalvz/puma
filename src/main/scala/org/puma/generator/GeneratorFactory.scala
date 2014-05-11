package org.puma.generator

import org.puma.configuration.ConfigurationUtil

/**
 * Project: puma
 * Package: org.puma.generator
 *
 * Author: Sergio Álvarez
 * Date: 03/2014
 */
object GeneratorFactory {
  def get:Generator = {
    val mode = ConfigurationUtil.getMode
    mode match {
      case "scoreGenerator" => new ScoreGenerator
      case "llrGenerator" => new LLRGenerator
      case _ => throw new IllegalStateException("Invalid mode")
    }
  }
}
