package org.hibernatewrapper

import org.hibernate.{SessionFactory, Session}
import org.slf4j.LoggerFactory

trait NewCreatedSession extends ManagedSession {

  private val logger = LoggerFactory.getLogger(classOf[NewCreatedSession])
  /**
   * always open a new session
   */
//  override abstract def create(): Session = sessionFactory.openSession()
  override def create(): Session = {
    logger.debug("open new session")
    sessionFactory.openSession()
  }

  /**
   * close session immediately
   */
//  override abstract def close(session: Session): Unit = session.close()
  override def close(session: Session): Unit = {
    logger.debug("close session")
    session.close()
  }

}

object NewCreatedSession {

  implicit def sfWrapper(sf: SessionFactory) : SessionFactoryWrapper = new SessionFactoryWrapper(sf)

}
