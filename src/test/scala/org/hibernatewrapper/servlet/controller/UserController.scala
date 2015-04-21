package org.hibernatewrapper.servlet.controller

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.http.HttpStatus
import org.hibernatewrapper.PreBoundSession._
import org.hibernatewrapper.fixture.SessionFactoryBuilder
import org.hibernatewrapper.servlet.model.{Task, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class UserController extends HttpServlet {

  val sf = SessionFactoryBuilder.sessionFactory

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {

    val preBoundSession = sf.withTransaction() { session => session }
    sf.withTransaction() { session =>
      //always use the same session in the current thread
      assert(session eq preBoundSession)
    }

    val f = Future {
      sf.withTransaction() { session => session }
    }

    for (session <- f) {
      //open another session in different thread
      assert(!(session eq preBoundSession))
    }

    Await.ready(f, 1.seconds)

    val id = registerUser.getId
    val user = sf.withTransaction() { implicit session =>
      User.get(id)
    }

    //should lazy load tasks in the pre-bound session
    println(s"user: ${user.getName}, with task: ${user.getTasks.get(0).getName}")
    resp.setStatus(HttpStatus.OK_200)
  }

  private def registerUser: User = {
    sf.withSession { implicit session =>
      val user = newUserWithTask
      User.register(user)
      user
    }
  }

  private def newUserWithTask = {
    val task = Task("test_task")
    val user = User("user_name")
    user.addTask(task)
    user
  }

}
