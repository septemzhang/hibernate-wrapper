package org.hibernatewrapper.servlet

import org.hibernatewrapper.NewCreatedSession._
import org.hibernatewrapper.SessionWrapper._
import org.hibernatewrapper.fixture.SessionFactoryBuilder
import org.hibernatewrapper.servlet.model.{Task, User}
import org.scalatest.FunSpec

class UserITSpec extends FunSpec {

  val sf = SessionFactoryBuilder.sessionFactory

  describe("User") {
    it("should generate primary key after registration") {
      val user = User("primary key")
      assert(user.id === 0 )
      sf.rollback { implicit session =>
        User.register(user)
      }
      assert(user.id !== 0)
    }

    it("should load tasks for user in the same session") {
      sf.rollback { implicit session =>
        val user = User("load_user_and_task")
        User.register(user)
        val task = Task("new task")
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

    it("should rollback any change in rollback transaction") {
      val name = "rollback_" + System.currentTimeMillis();
      val user = sf.rollback { implicit session =>
        val user = User(name)
        User.register(user)
        user
      }

      assert(user.getId !== 0)

      val count = sf.withSession { session =>
        session.findUnique[Long]("select count(*) from User where name = ?", name)
      }

      assert(count === 0)
    }

    it("dirty check of session can not cover operations executed by query") {
      val user = sf.withTransaction() { implicit session =>
        val user = User("dirty_check_" + System.currentTimeMillis())
        User.register(user)
        user
      }

      sf.withSession { session =>
        val count = session.createQuery(s"delete User where id = ${user.getId}").executeUpdate()
        assert(count === 1)
        assert(!session.isDirty)
      }
    }

  }

}