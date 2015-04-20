package org.hibernatewrapper.servlet

import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.servlet.model.{Task, User}
import org.hibernatewrapper.{SessionWrapper, PreBoundSession, SessionFactoryWrapper}
import org.scalatest.FunSpec

class UserPreBoundSessionITSpec extends FunSpec {

  val sessionFactory = SessionFactoryHolder.sessionFactory
  val sfw = new SessionFactoryWrapper(sessionFactory) with PreBoundSession

  describe("User") {
    it("should load tasks lazily in the pre-bound session") {
      val (user, session) = sfw.withTransaction(){ implicit session =>
        val user = createUser("lazy_load")
        User.register(user)
        val task = createTask
        User.saveTask(user, task)
        session.flush()
        session.clear()
        (SessionWrapper(session).load[User](user.getId), session)
      }

      assert(session.isOpen)
      assert(session.contains(user))
      //lazy load tasks
      assert(user.getTasks.size() === 1)
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
