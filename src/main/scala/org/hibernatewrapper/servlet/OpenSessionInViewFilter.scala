package org.hibernatewrapper.servlet

import javax.servlet._

import org.hibernate.SessionFactory
import org.hibernatewrapper.PreBoundSession._

/**
 * Servlet Filter that binds a Hibernate Session to the thread of the request to allow for lazy loading in web views
 */
class OpenSessionInViewFilter(val sessionFactory: SessionFactory) extends Filter {

  override def init(filterConfig: FilterConfig): Unit = ()

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    val session = sessionFactory.openSession()
    sessionFactory.bindSession(session)
    try {
      chain.doFilter(request, response)
    }finally {
      sessionFactory.unbindSession()
    }
  }

  override def destroy(): Unit = ()

}
