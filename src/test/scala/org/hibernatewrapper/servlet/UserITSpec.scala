package org.hibernatewrapper.servlet

import org.hibernatewrapper.{TXAttr, SessionFactoryWrapper}
import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.scalatest.LongSessionSupport
import org.hibernatewrapper.servlet.model.{Task, User}
import org.scalatest.FunSpec

class UserITSpec extends FunSpec with LongSessionSupport {

  val sessionFactory = SessionFactoryHolder.sessionFactory
  val sfw = new SessionFactoryWrapper(sessionFactory)

  val rollback = TXAttr(rollback = true)

  describe("User") {
    it("should generate primary key after registration") {
      val user = createUser("primary key")
      assert(user.getId == null)
      sfw.withTransaction(rollback) { implicit session =>
        User.register(user)
      }
      assert(user.getId > 0)
    }

    it("should work for updating after readonly transaction in the pre-bound session") {
      val user = createUser("update")
      sfw.withSession { implicit session =>
        User.register(user)
      }

      def getName = sfw.withCurrentSession(TXAttr(readOnly = true)) { implicit session =>
        User.get(user.getId).getName
      }
      //get user with readonly transaction
      assert(getName === user.getName)

      val newName = "new_name"
      //then update user with same pre-bound session
      sfw.withCurrentSession {implicit session =>
        val u = User.get(user.getId)
        u.setName(newName)
        session.save(u)
      }

      assert(getName === newName)
    }

    it("should load tasks for user in the same session") {
      sfw.withTransaction(rollback) { implicit session =>
        val user = createUser("load_user_and_task")
        User.register(user)
        val task = createTask
        User.saveTask(user, task)
        session.flush()
        session.clear()

        val created = User.findByName(user.getName)
        val tasks = created.getTasks
        assert(user ne created)
        assert(tasks.size === 1)
        assert(tasks.get(0).getName === task.getName)
      }
    }

    it("should load tasks lazily in the pre-bound session") {
      val newUser: User = sfw.withCurrentSession { implicit session =>
        val user = createUser("lazy_load")
        User.register(user)
        val task = createTask
        User.saveTask(user, task)
        session.flush()
        session.clear()
        User.findByName(user.getName)
      }
      //lazy load tasks
      assert(newUser.getTasks.size() === 1)
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