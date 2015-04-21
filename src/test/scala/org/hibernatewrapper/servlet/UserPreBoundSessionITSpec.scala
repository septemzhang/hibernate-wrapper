package org.hibernatewrapper.servlet

import org.hibernatewrapper.PreBoundSession._
import org.hibernatewrapper.{SessionWrapper, PreBoundSession, SessionFactoryWrapper}
import org.hibernatewrapper.SessionWrapper._
import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.servlet.model.{Task, User}
import org.scalatest.FunSpec

class UserPreBoundSessionITSpec extends FunSpec {

  val sf = SessionFactoryHolder.sessionFactory

  describe("User") {
    it("should load tasks lazily in the pre-bound session") {
      val (user, session) = sf.withTransaction(){ implicit session =>
        val user = createUser("lazy_load")
        User.register(user)
        val task = createTask
        User.saveTask(user, task)
        session.flush()
        session.clear()
        (session.loadById[User](user.getId), session)
      }

      assert(session.isOpen)
      assert(session.contains(user))
      //lazy load tasks
      assert(user.getTasks.size() === 1)


      val sessionFactory = sf
      val sfw = new SessionFactoryWrapper(sessionFactory) with PreBoundSession

      sfw.withTransaction() { session =>
        SessionWrapper(session).getById[User](1L)
      }

      import org.hibernatewrapper.PreBoundSession._

      sessionFactory.withTransaction() { session =>
        session.getById[User](1L)
      }

    }
  }

  private def createUser(name: String) : User = {
    val user = new User
    user.setName(name)
    user
  }

  private def createTask : Task = {
    val task = new Task
    task.setName("test_task")
    task
  }

}
