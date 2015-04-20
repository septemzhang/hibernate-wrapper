package org.hibernatewrapper

import org.hibernate.{SessionFactory, Session}

trait ManagedSession {

  val sessionFactory: SessionFactory

  def useSession[T](f: Session => T) : T = {
    val session = create()
    try {
      f(session)
    } finally {
      close(session)
    }
  }

//  abstract def create() : Session
  def create() : Session

//  abstract def close(session: Session) : Unit
  def close(session: Session) : Unit

}
