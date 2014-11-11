package org.hibernatewrapper.servlet

import javax.servlet._

import org.hibernate.SessionFactory
import org.hibernatewrapper.SessionFactoryWrapper

class OpenSessionInViewFilter(sf: SessionFactory) extends Filter {

  val sfw = new SessionFactoryWrapper(sf)

  override def init(filterConfig: FilterConfig): Unit = ()

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    val session = sf.openSession()
    sfw.bindSession(session)
    try {
      chain.doFilter(request, response)
    }finally {
      sfw.unbindSession(sf)
    }
  }

  override def destroy(): Unit = ()

}
