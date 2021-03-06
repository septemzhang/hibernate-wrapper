package org.hibernatewrapper

import java.util.List

import org.hibernate.{Query, Session}
import org.slf4j.LoggerFactory

/**
 * some convenient functions to load/query entities from session
 */
class SessionWrapper(val session: Session) {

  private val logger = LoggerFactory.getLogger(classOf[SessionWrapper])

  /*
  Error:(126, 19) overloaded method value get with alternatives:
  (x$1: String,x$2: java.io.Serializable,x$3: org.hibernate.LockOptions)Object <and>
  (x$1: String,x$2: java.io.Serializable,x$3: org.hibernate.LockMode)Object <and>
  (x$1: String,x$2: java.io.Serializable)Object <and>
  (x$1: Class[_],x$2: java.io.Serializable,x$3: org.hibernate.LockOptions)Object <and>
  (x$1: Class[_],x$2: java.io.Serializable,x$3: org.hibernate.LockMode)Object <and>
  (x$1: Class[_],x$2: java.io.Serializable)Object
 does not take type parameters
        f.session.get[Object](1L)
                  ^
   */
//  def get[T](id: java.lang.Long)(implicit m: Manifest[T]) : T = session.get(m.runtimeClass, id).asInstanceOf[T]
//  def getById[T: Manifest[T]](id: java.lang.Long) : T = session.get(manifest[T].runtimeClass, id).asInstanceOf[T]
//  def getById[T: Manifest](id: java.lang.Long) : T = session.get(manifest[T].runtimeClass, id).asInstanceOf[T]
  def getById[T: Manifest](id: java.io.Serializable) : T = session.get(manifest[T].runtimeClass, id).asInstanceOf[T]

  /**
   * load entity with given id
   */
  def loadById[T: Manifest](id: java.io.Serializable) : T = session.load(manifest[T].runtimeClass, id).asInstanceOf[T]

  /**
   * find with hql and parameters
   */
  //TODO return collection type of scala
  // find(hql).params(...).list()
  // find(hql).params(...).unique()
  def find[T](hql: String, params: Any*): List[T] = {
    createQuery(hql, params: _*).list().asInstanceOf[List[T]]
  }

  /**
   * find unique result with hql and parameters
   */
  def findUnique[T](hql: String, params: Any*): T = {
    createQuery(hql, params: _*).uniqueResult().asInstanceOf[T]
  }

  //WARN  org.hibernate.hql.internal.ast.HqlSqlWalker - [DEPRECATION] Encountered positional parameter near line 1, column 75.
  //Positional parameter are considered deprecated; use named parameters or JPA-style positional parameters instead.
  private def createQuery(queryString: String, values: Any*): Query = {
    require(queryString != null && !queryString.isEmpty, "queryString cannot be empty")
//    logger.debug("create query: {}, params: {}", queryString, values: _*)
//    logger.debug("create query: {}, params: {}", queryString, values)
    val query: Query = session.createQuery(queryString)
    values.zipWithIndex.foreach {
      case (v, i) => query.setParameter(i, v)
    }
    query
  }

}

object SessionWrapper {

  def apply(session: Session) = new SessionWrapper(session)

  implicit def sessionToSessoinWrapper(s: Session) : SessionWrapper = SessionWrapper(s)

}
