package org.puma.stat

import com.typesafe.scalalogging.log4j.Logging

/**
 * Project: puma
 * Package: org.puma.stat
 *
 * Author: Sergio Álvarez
 * Date: 02/2014
 */
object Dunning {

  /**
   *  This implementation is based on LLR interpretation provided in the following
   *  paper (see table and equations in page 7):
   *
   *  Java, Akshay, et al. "Why we twitter: understanding microblogging usage and
   *  communities." Proceedings of the 9th WebKDD and 1st SNA-KDD 2007 workshop on
   *  Web mining and social network analysis. ACM, 2007.
   *  Available at: http://aisl.umbc.edu/resources/369.pdf
   *
   * @param a frequency of token of interest in dataset A
   * @param b	frequency of token of interest in dataset B
   * @param c	total number of observations in dataset A
   * @param d	total number of observations in dataset B
   */
  def rootLogLikelihoodRatio(a: Long, b: Long, c: Long, d: Long): Double = {
    val E1 = c * ((a + b) / (c + d).toDouble)
    val E2 = d * ((a + b) / (c + d).toDouble)
    var result = 2 * (a * Math.log(a / E1) + b * Math.log(b / E2))

    result = Math.sqrt(result)

    if ((a / c.toDouble) < (b / d.toDouble)) result * -1 else result
  }

}
