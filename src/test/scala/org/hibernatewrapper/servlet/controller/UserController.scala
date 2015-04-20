package org.hibernatewrapper.servlet.controller

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.http.HttpStatus
import org.hibernatewrapper.{PreBoundSession, SessionFactoryWrapper}
import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.servlet.model.{Task, User}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserController extends HttpServlet {

  val sfw = new SessionFactoryWrapper(SessionFactoryHolder.sessionFactory) with PreBoundSession

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {

    val preBoundSession = sfw.withTransaction() { session => session }
    sfw.withTransaction() { session =>
      //always use the same session in the current thread
      assert(session eq preBoundSession)
    }

    val f = Future {
      sfw.withTransaction() { session => session }
    }

    for (session <- f) {
      //open another session in different thread
      assert(!(session eq preBoundSession))
    }

    Await.ready(f, 1.seconds)

    val id = registerUser.getId
    val user = sfw.withTransaction() { implicit session =>
      User.get(id)
    }

    //should lazy load tasks in the pre-bound session
    println(s"user: ${user.getName}, with task: ${user.getTasks.get(0).getName}")
    resp.setStatus(HttpStatus.OK_200)
  }

  private def registerUser: User = {
    sfw.withSession { implicit session =>
      val user = newUserWithTask
      User.register(user)
      user
    }
  }

  private def newUser = {
    val user = new User
    user.setName("user_name")
    user
  }

  private def newUserWithTask = {
    val task = new Task
    task.setName("test_task")
    val user = newUser
    user.addTask(task)
    user
  }

}
