package org.hibernatewrapper.servlet.model


import javax.persistence._

import scala.beans.BeanProperty

@Entity @Table
class Task {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @BeanProperty var id: java.lang.Long = _

  @BeanProperty var name: String = _
  @BeanProperty var status: String = _

  @ManyToOne @JoinColumn(name = "user_id")
  @BeanProperty var user: User = _

}

