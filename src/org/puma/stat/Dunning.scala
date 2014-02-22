package org.puma.stat

import com.typesafe.scalalogging.log4j.Logging

/**
 * Project: puma
 * Package: org.puma.stat
 *
 * Author: Daniel Gayo-Avello
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

    val E3 = if(a == 0) 1/E1 else a/E1
    val E4 = if(b == 0) 1/E2 else b/E2
    val result = Math.sqrt(2 * (a * Math.log(E3) + b * Math.log(E4)))

    if ((a / c.toDouble) < (b / d.toDouble)) -result else result
  }

  /**
   *  A normalized version of LLR. To the best of my knowledge this has not
   *  been described anywhere before.
   *
   *  The idea is rather simple: the most representative of a dataset a token
   *  is the highest the LLR value. However, we cannot use that value as a
   *  measure of the confidence we have in a given tweet containing that token
   *  to belong to a given dataset.
   *
   *  For instance, "españa" is highly  representative of Spanish websites
   *  but we cannot assign a huge weight to  every website containing that
   *  token. In fact "españa" is pretty common in Chilean websites.
   *
   *  In contrast, "culín de sidra" is not highly representative in the sense
   *  that relatively few websites in .es contain the token and virtually
   *  none in .cl. However, we can have a strong suspicion that a website
   *  containing such a token is in fact a Spanish website.
   *
   *  Therefore, to capture that intuition we are defining a "normalized"
   *  value for LLR that is computed in three steps:
   *
   *  1. LLR is really normalized against the minimum (negative) and maximum
   *  (positive) theoretical values that LLR can achieve given two datasets.
   *  Minimum would be obtained with a=0 and b=d while the maximum with a=c
   *  and b=0.
   *
   *  2. Then, values a and b are normalized against c and d (the size of
   *  each dataset).
   *
   *  3. Finally, the normalized value of LLR is divided by the corresponding
   *  normalized value of a or b depending on LLR being positive or negative
   *  (i.e. probably belonging to dataset A or dataset B).
   *
   *  This way the normalized LLR value could be use as a proxy for the
   *  confidence we have in assigning a given item to one of the two datasets
   *  depending on the presence of one token. For instance, if "españa" appears
   *  in a website we'd assign a relatively low positive weight (0.589) but if
   *  "culín de sidra" appears the weight would be quite high (95.08). In
   *  contrast, "chile" would suppose a relative low negative value (-0.39) but
   *  "achunchar" would be a huge negative weight (-711.88).
   *
   * @param a frequency of token of interest in dataset A
   * @param b	frequency of token of interest in dataset B
   * @param c	total number of observations in dataset A
   * @param d	total number of observations in dataset B
   */
  def normalizedRootLogLikelihoodRatio (a: Long, b: Long, c: Long, d: Long): Double = {
    val min    = rootLogLikelihoodRatio(0,d,c,d)
    val max    = rootLogLikelihoodRatio(c,0,c,d)

    val a_norm = a / c.toDouble
    val b_norm = b / d.toDouble
    val llr    = rootLogLikelihoodRatio(a,b,c,d)

    if (llr > 0) llr / (max * a_norm) else -llr / (min * b_norm)
  }

}
