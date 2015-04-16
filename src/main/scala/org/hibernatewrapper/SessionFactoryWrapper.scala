package org.hibernatewrapper

import org.hibernate.context.internal.ThreadLocalSessionContext
import org.hibernate.{FlushMode, Session, SessionFactory}
import org.slf4j.LoggerFactory

/**
 * A scala wrapper for hibernate session factory built on top of loan pattern and higher order functions
 */
class SessionFactoryWrapper(val sessionFactory: SessionFactory) {

  private val logger = LoggerFactory.getLogger(classOf[SessionFactoryWrapper])

  /**
   * apply function f to a new session in a transaction with given transaction attribute
   * use loan pattern to deal with managing session and cross cutting concerns like transaction
   * loan pattern is much more simpler than aop solution from java
   */
  def withTransaction[T](txAttr: TXAttr)(f: Session => T): T = {
    useSession { session =>
      inTransaction(txAttr, session)(f)
    }
  }

  /**
   * apply function f to a new session with default transaction attribute
   */
  def withTransaction[T](f: Session => T): T = withTransaction(TXAttr())(f)

  /**
   * apply the given function f to a pre bound session with current executing thread
   * and respect the given transaction attribute
   */
  def withCurrentSession[T](txAttr: TXAttr)(f: Session => T): T = {
    //save and restore flush mode?
    inTransaction(txAttr, sessionFactory.getCurrentSession)(f)
  }

  /**
   * apply the given function f to a pre bound session with current executing thread
   */
  //TODO remove
  def withCurrentSession[T](f: Session => T): T = withCurrentSession(TXAttr())(f)

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

  private def inTransaction[T](txAttr: TXAttr, session: Session)(f: Session => T): T = {
    try {
      //TODO restore flush mode after transaction complete?
      session.setFlushMode(if (txAttr.readOnly) FlushMode.MANUAL else FlushMode.AUTO)
      val transaction = session.getTransaction
      transaction.setTimeout(txAttr.timeout)
      transaction.begin
      val result = f(session)
      //TODO check rollback in catch clause
      if(txAttr.rollback) {
        session.clear
        transaction.rollback
      }
      else transaction.commit
      result
    } catch {
      case e: Throwable =>
        if (txAttr.shouldCommitOn(e)) {
          logger.info("commit for exception: {}", e.getMessage)
          session.getTransaction.commit
        }else {
          session.clear
          session.getTransaction.rollback
        }
        throw e
    }
  }

  def bindSession(session: Session) =  ThreadLocalSessionContext.bind(session)

  //TODO remove param
  def unbindSession(sessionFactory: SessionFactory) =  {
    val session = ThreadLocalSessionContext.unbind(sessionFactory)
    if (session != null) session.close()
  }

}

/**
 * case class based, immutable transaction attribute used by withSession
 * it will rollback for all exceptions by default
 * you can also specify no rollback rules, if you do not want a transaction rolled back when an exception is thrown
 */
case class TXAttr(rollback: Boolean = false, readOnly: Boolean = false
                  ,timeout: Int = -1 , private val commitOn: Set[Class[_]] = Set()) {

  def rollback(value: Boolean) : TXAttr = copy(rollback = value)
  def readOnly(value: Boolean) : TXAttr = copy(readOnly = value)
  def timeout(seconds: Int) : TXAttr = copy(timeout = seconds)

  def shouldCommitOn(ex: Throwable) : Boolean = {
    commitOn.exists(_.isAssignableFrom(ex.getClass));
  }

}




