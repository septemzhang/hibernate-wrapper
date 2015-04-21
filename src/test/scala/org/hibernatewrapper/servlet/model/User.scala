package org.hibernatewrapper.servlet.model


import java.util.{ArrayList, List}
import javax.persistence._

import org.hibernate.Session
import org.hibernatewrapper.SessionWrapper._

import scala.beans.BeanProperty

@Entity @Table
class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @BeanProperty var id: Long = _

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
    session.findUnique(hql, name)
  }

  def get(id: Long)(implicit session: Session) : User = session.getById[User](id)

  def saveTask(user: User, task: Task)(implicit session: Session) : User.type = {
    val u = get(user.getId)
    u.addTask(task)
    session.save(u)
    User
  }

  def countTask(id: Long)(implicit session: Session) : Long = {
    session.findUnique[Long]("select count(id) from Task where user.id = ?", id)
  }

}
