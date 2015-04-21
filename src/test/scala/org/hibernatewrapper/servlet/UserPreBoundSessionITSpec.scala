package org.hibernatewrapper.servlet

import org.hibernatewrapper.PreBoundSession._
import org.hibernatewrapper.SessionWrapper._
import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.servlet.model.{Task, User}
import org.scalatest.FunSpec

class UserPreBoundSessionITSpec extends FunSpec {

  val sf = SessionFactoryHolder.sessionFactory

  describe("User") {
    it("should load tasks lazily in the pre-bound session") {
      val (user, session) = sf.withTransaction(){ implicit session =>
        val user = User("lazy_load")
        User.register(user)
        val task = Task("test_task")
        User.saveTask(user, task)
        session.flush()
        session.clear()
        (session.loadById[User](user.getId), session)
      }

      assert(session.isOpen)
      assert(session.contains(user))
      //lazy load tasks
      assert(user.getTasks.size() === 1)
    }
  }

}
