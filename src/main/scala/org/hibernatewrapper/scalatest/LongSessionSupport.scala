package org.hibernatewrapper.scalatest

import org.hibernate.SessionFactory
import org.hibernatewrapper.SessionFactoryWrapper
import org.scalatest.{Outcome, Suite, SuiteMixin}

trait LongSessionSupport extends SuiteMixin { self: Suite =>

  val sessionFactory : SessionFactory

  abstract override def withFixture(test: NoArgTest): Outcome = {
    val sfw = new SessionFactoryWrapper(sessionFactory)
    val session = sessionFactory.openSession()
    sfw.bindSession(session)
    try super.withFixture(test)
    finally {
      sfw.unbindSession(sessionFactory)
    }
  }

}
