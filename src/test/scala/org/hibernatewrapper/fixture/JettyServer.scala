package org.hibernatewrapper.fixture

import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{FilterHolder, FilterMapping, ServletHandler}
import org.hibernatewrapper.servlet.OpenSessionInViewFilter
import org.hibernatewrapper.servlet.controller.UserController

/**
 * embedded jetty server for testing purpose
 */
object JettyServer {

  //set a random port
  private val server = new Server(0)
  private val sf = SessionFactoryHolder.sessionFactory

  def start : Unit = {
    val handler = new ServletHandler
    handler.addFilterWithMapping(new FilterHolder(new OpenSessionInViewFilter(sf)), "/*", FilterMapping.ALL)
    handler.addServletWithMapping(classOf[UserController], "/users/*")

    server.setHandler(handler)
    server.start()
    println("server started at: http://localhost:" + port)
//    println("dump: " + webapp.dump())
  }

  def stop : Unit = server.stop()

  def join : Unit = server.join()

  def port : Int = server.getConnectors()(0).asInstanceOf[ServerConnector].getLocalPort

}
