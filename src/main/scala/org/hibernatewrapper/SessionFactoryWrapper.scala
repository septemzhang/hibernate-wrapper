package org.hibernatewrapper

import org.hibernate.context.internal.ThreadLocalSessionContext
import org.hibernate.{Session, SessionFactory}
import org.slf4j.LoggerFactory

/**
 * A scala wrapper for hibernate session factory built on top of loan pattern and higher order functions
 */
class SessionFactoryWrapper(val sessionFactory: SessionFactory) {

  type ExceptionClass = Class[_ <: Throwable]

  private val logger = LoggerFactory.getLogger(classOf[SessionFactoryWrapper])

  /**
   * apply function f to a new session in a transaction with given transaction attribute.
   *
   * use loan pattern to deal with managing session and cross cutting concerns like transaction.
   *
   * loan pattern is much more simpler than aop solution from java.
   */
//  def withTransaction[T](txAttr: TXAttr)(f: Session => T): T = {
//    useSession { session =>
//      inTransaction(txAttr, session)(f)
//    }
//  }

  /**
   * apply function f to a new session in a transaction with given transaction attribute.
   *
   * use loan pattern to deal with managing session and cross cutting concerns like transaction.
   *
   * loan pattern is much more simpler than aop solution from java.
   *
   * it will rollback for all exceptions by default
   *
   * you can also specify no rollback rules, if you do not want a transaction rolled back when an exception is thrown
   */
  def withTransaction[T](commitOn: Set[ExceptionClass] = Set(), timeout : Int = -1)(f: Session => T): T = {
    useSession { session =>
      inTransaction(session, timeout, commitOn)(f)
    }
  }

//  def withTransaction[T](commitOn: Set[ExceptionClass] = Set())(f: Session => T): T = {
//    useSession { session =>
//      inTransaction(session, -1, commitOn)(f)
//    }
//  }

  def withTransaction[T](f: Session => T): T = {
    withTransaction(Set(), -1)(f)
  }

//  def withTransaction[T](commitOn: Set[ExceptionClass] = Set())(f: Session => T): T = {
//    withTransaction(commitOn, -1)(f)
//  }

//  def withTransaction[T](commitOn: ExceptionClass, timeout : Int = -1)(f: Session => T): T = {
    /*
    Error:(55, 5) overloaded method value withTransaction with alternatives:
  [T](commitOn: SessionFactoryWrapper.this.ExceptionClass, timeout: Int)(f: org.hibernate.Session => T)T <and>
  [T](commitOn: Set[SessionFactoryWrapper.this.ExceptionClass], timeout: Int)(f: org.hibernate.Session => T)T
 cannot be applied to (scala.collection.immutable.Set[Class[_$1]], Int)
    withTransaction(Set.apply(commitOn), timeout)(f)
    ^
     */
//    withTransaction(Set(commitOn), timeout)(f)
//    withTransaction(Set.apply(commitOn), timeout)(f)
//  }

//  def withTransaction[T](timeout : Int)(f: Session => T): T = {
//    withTransaction(timeout, Set())(f)
//  }
//
//  def withTransaction[T](commitOn: Set[Class[_ <: Throwable]])(f: Session => T): T = {
//    withTransaction(-1, commitOn)(f)
//  }

  private def inTransaction[T](session: Session, timeout: Int, commitOn: Set[ExceptionClass])(f: Session => T): T = {
    try {
      val transaction = session.getTransaction
      transaction.setTimeout(timeout)
      transaction.begin()
      val result = f(session)
      //TODO flush session before commit?
      transaction.commit()
      result
    } catch {
      case e: Throwable =>
        if (shouldCommitOn(e, commitOn)) {
          logger.info("commit for exception: {}", e.getMessage)
          session.getTransaction.commit()
        }else {
          session.clear()
          session.getTransaction.rollback()
        }
        throw e
    }
  }

  def shouldCommitOn(e: Throwable, commitOn: Set[ExceptionClass]) : Boolean = {
    commitOn.exists(_.isAssignableFrom(e.getClass))
  }

  /**
   * apply function f to a new session with default transaction attribute
   */
//  def withTransaction[T](f: Session => T): T = withTransaction(TXAttr())(f)

  /**
   * apply function f to a new session and rollback transaction.
   * it is useful in unit test.
   */
  def rollback[T](f: Session => T): T = {
    useSession { session =>
      try {
        val transaction = session.getTransaction
        transaction.begin()
        val result = f(session)
        result
      } finally {
        session.clear()
        session.getTransaction.rollback()
      }
    }
  }

  /**
   * apply the given function f to a pre bound session with current executing thread
   * and respect the given transaction attribute
   */
//  def withCurrentSession[T](txAttr: TXAttr)(f: Session => T): T = {
    //save and restore flush mode?
//    inTransaction(txAttr, sessionFactory.getCurrentSession)(f)
//  }

  /**
   * apply the given function f to a pre bound session with current executing thread
   */
//  def withCurrentSession[T](f: Session => T): T = withCurrentSession(TXAttr())(f)

  /**
   * apply function f in a non-transactional session
   * it is recommended to use withCurrentSession which executes in a transaction
   */
  def withSession[T](f: Session => T) : T = useSession(f)

  private def useSession[T](f: Session => T) : T = {
    val session = sessionFactory.openSession()
    try {
      f(session)
    } finally {
      session.close()
    }
  }

  def bindSession(session: Session) =  ThreadLocalSessionContext.bind(session)

  //TODO remove param
  def unbindSession(sessionFactory: SessionFactory) =  {
    val session = ThreadLocalSessionContext.unbind(sessionFactory)
    if (session != null) session.close()
  }

}


