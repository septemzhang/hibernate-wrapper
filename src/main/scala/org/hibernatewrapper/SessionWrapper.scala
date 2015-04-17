package org.hibernatewrapper

import java.util.List

import org.hibernate.{Query, Session}

/**
 * some convenient functions to load/query entities from session
 */
class SessionWrapper(val session: Session) {

  //TODO make type of id a type parameter
  def get[T](id: java.lang.Long)(implicit m: Manifest[T]) : T = session.get(m.runtimeClass, id).asInstanceOf[T]

  /**
   * load entity with given id
   */
  def load[T](id: java.lang.Long)(implicit m: Manifest[T]) : T = session.load(m.runtimeClass, id).asInstanceOf[T]

  /**
   * find with hql and parameters
   */
  def find[T](hql: String, params: Any*): List[T] = {
    createQuery(hql, params: _*).list().asInstanceOf[List[T]]
  }

  /**
   * find unique result with hql and parameters
   */
  def findUnique[T](hql: String, params: Any*): T = {
    createQuery(hql, params: _*).uniqueResult().asInstanceOf[T]
  }

  private def createQuery(queryString: String, values: Any*): Query = {
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
