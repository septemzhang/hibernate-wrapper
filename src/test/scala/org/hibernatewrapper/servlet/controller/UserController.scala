package org.hibernatewrapper.servlet.controller

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import org.eclipse.jetty.http.HttpStatus
import org.hibernatewrapper.SessionFactoryWrapper
import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.servlet.model.{Task, User}

class UserController extends HttpServlet {

  val sfw = new SessionFactoryWrapper(SessionFactoryHolder.sessionFactory)

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val id = registerUser.getId
    val user = sfw.withTransaction{ implicit session =>
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
