package org.hibernatewrapper

import org.hibernate.Session

trait NewCreatedSession extends ManagedSession {

  /**
   * always open a new session
   */
//  override abstract def create(): Session = sessionFactory.openSession()
  override def create(): Session = sessionFactory.openSession()

  /**
   * close session immediately
   */
//  override abstract def close(session: Session): Unit = session.close()
  override def close(session: Session): Unit = session.close()

}
