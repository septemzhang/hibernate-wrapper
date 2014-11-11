package org.hibernatewrapper

import java.util.List

import org.hibernate.{Query, Session}

class SessionWrapper(val session: Session) {

  def get[T](id: java.lang.Long)(implicit m: Manifest[T]) : T = session.get(m.runtimeClass, id).asInstanceOf[T]

  /**
   * load entity with given id
   */
  def load[T](id: java.lang.Long)(implicit m: Manifest[T]) : T = session.load(m.runtimeClass, id).asInstanceOf[T]

  /**
   * find with hql and parameters
   */
  def find[T](hql: String, params: Vector[Any] = Vector()): List[T] = {
    createQuery(hql, params).list().asInstanceOf[List[T]]
  }

  /**
   * find unique result with hql and parameters
   */
  def findUnique[T](hql: String, params: Vector[Any] = Vector()): T = {
    createQuery(hql, params).uniqueResult().asInstanceOf[T]
  }

  private def createQuery(queryString: String, values: Vector[Any]): Query = {
    require(queryString != null && !queryString.isEmpty, "queryString cannot be empty")
    val query: Query = session.createQuery(queryString)
    values.zipWithIndex.foreach {
      case (v, i) => query.setParameter(i, v)
    }
    query
  }

}

object SessionWrapper {

  def apply(session: Session) = new SessionWrapper(session)

}
