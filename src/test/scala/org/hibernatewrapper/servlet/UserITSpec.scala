package org.hibernatewrapper.servlet

import org.hibernatewrapper.{SessionWrapper, SessionFactoryWrapper}
import org.hibernatewrapper.fixture.SessionFactoryHolder
import org.hibernatewrapper.servlet.model.{Task, User}
import org.scalatest.FunSpec

class UserITSpec extends FunSpec {

  val sessionFactory = SessionFactoryHolder.sessionFactory
  val sfw = new SessionFactoryWrapper(sessionFactory)

  describe("User") {
    it("should generate primary key after registration") {
      val user = createUser("primary key")
      assert(user.getId == null)
      sfw.rollback { implicit session =>
        User.register(user)
      }
      assert(user.getId > 0)
    }

    it("should load tasks for user in the same session") {
      sfw.rollback { implicit session =>
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

    //TODO
    /*
    it("should load tasks lazily in the pre-bound session") {
      val newUser: User = sfw.withTransaction{ implicit session =>
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
    */

    it("should rollback any change in rollback transaction") {
      val name = "rollback_" + System.currentTimeMillis();
      val user = sfw.rollback { implicit session =>
        val user = createUser(name)
        User.register(user)
        user
      }

      assert(user.getId != null)

      val count = sfw.withSession { session =>
        SessionWrapper(session).findUnique[Long]("select count(*) from User where name = ?", name)
      }

      assert(count === 0)
    }

    it("dirty check of session can not cover operations executed by query") {
      val user = sfw.withTransaction() { implicit session =>
        val user = new User
        user.setName("dirty_check_" + System.currentTimeMillis())
        User.register(user)
        user
      }

      sfw.withSession { session =>
        val count = session.createQuery(s"delete User where id = ${user.getId}").executeUpdate()
        assert(count === 1)
        assert(!session.isDirty)
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