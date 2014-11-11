package org.hibernatewrapper.servlet

import org.eclipse.jetty.client.HttpClient
import org.hibernatewrapper.fixture.JettyServer
import org.scalatest.{BeforeAndAfterAll, FunSpec}

/**
 * full integration test for UserController with embedded jetty
 * all the collaborators are real objects
 */
class UserControllerITSpec extends FunSpec with BeforeAndAfterAll {

  val httpClient = new HttpClient()

  override def beforeAll = {
    JettyServer.start
//    JettyServer.join
    httpClient.start
  }

  override def afterAll = {
    JettyServer.stop
    httpClient.stop
  }

  describe("UserController"){
    it("should respond with 200 OK for GET request") {
      val port = JettyServer.port
      val response = httpClient.newRequest(s"http://localhost:$port/users/1").send()
      assert(response.getStatus === 200)
    }
  }

}
