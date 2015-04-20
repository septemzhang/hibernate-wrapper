package org.hibernatewrapper.servlet

import javax.servlet._

import org.hibernate.SessionFactory
import org.hibernatewrapper.{PreBoundSession, SessionFactoryWrapper}

/**
 * Servlet Filter that binds a Hibernate Session to the thread of the request to allow for lazy loading in web views
 */
class OpenSessionInViewFilter(val sessionFactory: SessionFactory) extends Filter {

  val sfw = new SessionFactoryWrapper(sessionFactory) with PreBoundSession

  override def init(filterConfig: FilterConfig): Unit = ()

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    val session = sessionFactory.openSession()
    sfw.bindSession(session)
    try {
      chain.doFilter(request, response)
    }finally {
      sfw.unbindSession()
    }
  }

  override def destroy(): Unit = ()

}
