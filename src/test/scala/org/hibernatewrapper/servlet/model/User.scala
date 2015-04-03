package org.hibernatewrapper.servlet.model


import java.util.{ArrayList, List}
import javax.persistence._

import org.hibernate.Session
import org.hibernatewrapper.SessionWrapper

import scala.beans.BeanProperty

@Entity @Table
class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  //scala Long?
  @BeanProperty var id: java.lang.Long = _

  @BeanProperty var name: String = _

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = Array(CascadeType.ALL))
  @BeanProperty var tasks: List[Task] = new ArrayList[Task]

  def addTask(task: Task): User = {
    this.tasks.add(task)
    task.setUser(this)
    this
  }

}

object User {

  def register(user: User)(implicit session: Session) : User.type = {
    session.save(user)
    User
  }

  def findByName(name: String)(implicit session: Session) : User = {
    val hql = "from User where name = ?"
    SessionWrapper(session).findUnique(hql, name)
  }

  def get(id: java.lang.Long)(implicit session: Session) : User = SessionWrapper(session).get[User](id)

  def saveTask(user: User, task: Task)(implicit session: Session) : User.type = {
    val u = get(user.getId)
    u.addTask(task)
    session.save(u)
    User
  }

}
