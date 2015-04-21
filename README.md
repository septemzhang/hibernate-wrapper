#a simple wrapper for working with hibernate in scala

hibernate is the leading ORM framework in java, but just like any other libraries from java, it is cumbersome to use in scala. `hibernate-wrapper` provides a way to smoothly integrate hibernate in scala world.

it delivers the following benefits:

* transaction/session management via high order function and loan pattern 
* some convenient functions to load/query entities from session

#transaction/session manager

here is some examples:

```scala
    import org.hibernatewrapper.{SessionFactoryWrapper, SessionWrapper}

    val sessionFactory = ... //build hibernate session factory
    val sfw = new SessionFactoryWrapper(sessionFactory)

    //all the operations in the high order function will be run in transaction
    //SessionFactoryWrapper wil take care of opening/closing session and starting/committing transaction
    val taskCount = sfw.withTransaction() { session =>
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
```

or you can implement all the database operations in a Rich Domain Model style:

```scala

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

      def countTask(id: Long)(implicit session: Session) : Long = {
        SessionWrapper(session).findUnique[Long]("select count(id) from Task where user.id = ?", id)
      }

    }

    val taskCount = sfw.withTransaction() { implicit session =>
      val user = new User
      user.setName("new_user")
      User.register(user)

      val task = new Task
      task.setName("add more doc")

      User.saveTask(user, task)
      User.countTask(user.getId)
    }
```

`withTransaction` will rollback for all exceptions by default. you can specify no rollback rules, if you do not want a transaction rolled back when an exception is thrown

```scala
    //rollback on all exceptions except FileNotFoundException and NullPointerException
    sfw.withTransaction(commitOn = Set(classOf[FileNotFoundException], classOf[NullPointerException])) { session =>
      throw new FileNotFoundException()
    }
```

and set timeout by:

```scala
    sfw.withTransaction(commitOn = Set(classOf[FileNotFoundException]), timeout = 3) { session => }
```

and reuse transaction attributes with partially applied function:

```scala
    def withDefaultTransaction[T] = sfw.withTransaction[T](Set(classOf[RuntimeException]), 3)(_ : Session => T)

    withDefaultTransaction { session =>
        //run in transaction with default attribute
    }

    //reuse default transaction
    withDefaultTransaction { session => }
```

#Pre-bound session support

Pre-bound session, aka `Open Session in View Pattern` is a pattern that binds a Hibernate Session to the thread of the request to allow for lazy loading in web views.

to add pre-bound session support, you first need to add `OpenSessionInViewFilter` to your servlet deployment descriptor, take jetty as example:

```scala
    import org.hibernatewrapper.{PreBoundSession, SessionFactoryWrapper}
    import org.hibernatewrapper.servlet.OpenSessionInViewFilter

    val sessionFactory = ...

    //jetty servlet handler
    val handler = new ServletHandler
    handler.addFilterWithMapping(new FilterHolder(new OpenSessionInViewFilter(sf)), "/*", FilterMapping.ALL)
```

and then mixin `PreBoundSession` for SessionFactoryWrapper:

```scala
    import org.hibernatewrapper.{PreBoundSession, SessionFactoryWrapper}

    val sessionFactory = ...
    //mixin PreBoundSession 
    val sfw = new SessionFactoryWrapper(sessionFactory) with PreBoundSession
    //will be run in pre-bound session
    sfw.withTransaction() { session => ... }
```

#implicit conversions

hibernate-wrapper comes with some implicit conversions to make `Session` and `SessionFactoryWrapper` more pleasant to use

so

```scala
    val sfw = new SessionFactoryWrapper(sessionFactory) with PreBoundSession

    sfw.withTransaction() { session =>
        SessionWrapper(session).getById[User](1L)
    }
```

can be replaced with:

```scala
      import org.hibernatewrapper.PreBoundSession._
      import org.hibernatewrapper.SessionWrapper._

      sessionFactory.withTransaction() { session =>
        session.getById[User](1L)
      }
```

