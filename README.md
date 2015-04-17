#a simple wrapper for working with hibernate in scala

hibernate is the leading ORM framework in java, but just like any other libraries from java, it is cumbersome to use in scala. `hibernate-wrapper` provides a way to smoothly integrate hibernate in scala world.

it delivers the following benefits:

* transaction/session management via high order function and loan pattern 
* some convenient functions to load/query entities from session

here is some examples:

    val sessionFactory = ... //build hibernate session factory
    val sfw = new SessionFactoryWrapper(sessionFactory)

    //all the operations in the high order function will be run in transaction
    //SessionFactoryWrapper wil take care of opening/closing session and starting/committing transaction
    val taskCount = sfw.withTransaction { session =>
      val user = new User
      user.setName("new_user")
      session.save(user)

      val task = new Task
      task.setName("add more doc")

      user.addTask(task)
      session.save(user)

      //SessionWrapper provides some convenient functions to load/query entities 
      SessionWrapper(session).findUnique[Long]("select count(id) from Task where user = ?", user)
    }

or you can implement all the database operations in a Rich Domain Model style:

    //Rich Domain Model
    object User {

      def register(user: User)(implicit session: Session) : User.type = {
        session.save(user)
        User
      }

      def saveTask(user: User, task: Task)(implicit session: Session) : User.type = {
        val u = get(user.getId)
        u.addTask(task)
        session.save(u)
        User
      }

      def countTask(id: java.lang.Long)(implicit session: Session) : Long = {
        SessionWrapper(session).findUnique[Long]("select count(id) from Task where user.id = ?", id)
      }

    }

    val taskCount = sfw.withTransaction{ implicit session =>
      val user = new User
      user.setName("new_user")
      User.register(user)

      val task = new Task
      task.setName("add more doc")

      User.saveTask(user, task)
      User.countTask(user.getId)
    }

`withTransaction` will rollback for all exceptions by default. you can specify no rollback rules, if you do not want a transaction rolled back when an exception is thrown

  //rollback on all exceptions except FileNotFoundException and NullPointerException
  sfw.withTransaction(commitOn = Set(classOf[FileNotFoundException], classOf[NullPointerException])) { session =>
    throw new FileNotFoundException()
  }

and set timeout by:

    sfw.withTransaction(commitOn = Set(classOf[FileNotFoundException]), timeout = 3) { session => }

partial applied function:

    def withDefaultTransaction[T] = sfw.withTransaction[T](Set(classOf[RuntimeException]), 3)(_ : Session => T)

    withDefaultTransaction { session =>
        //run in transaction with default attribute
    }

    //reuse default transaction
    withDefaultTransaction { session => }

#TODO

* support pre-bound session and provide a consistent api with new created session
* add scala implict conversion to extends SessionFactory and Session

