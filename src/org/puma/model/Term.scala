package org.puma.model

/**
 * Project: puma
 * Package: org.puma
 *
 * Author: Sergio Álvarez
 * Date: 01/2014
 */
class Term(t: List[String]) {
  private[this] val _terms = t
  def terms = _terms

  override def equals(obj: Any): Boolean = {
    obj match {
      case term: Term => {
        if(this.terms.size != term.terms.size) return false
        this.terms.foreach(aTerm => { if(!term.terms.contains(aTerm)) return false })
        true
      }
      case _ => false
    }
  }

  override def hashCode: Int = {
    this.terms.hashCode()
  }

  override def toString = terms.mkString(" ")
}
