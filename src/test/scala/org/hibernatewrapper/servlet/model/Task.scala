package org.hibernatewrapper.servlet.model


import javax.persistence._

import scala.beans.BeanProperty

@Entity @Table
class Task {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @BeanProperty var id: Long = _

  @BeanProperty var name: String = "created"
  @BeanProperty var status: String = _

  @ManyToOne @JoinColumn(name = "user_id")
  @BeanProperty var user: User = _

}

object Task {

  def apply(name: String) = {
    val task = new Task
    task.name = name
    task
  }

}

