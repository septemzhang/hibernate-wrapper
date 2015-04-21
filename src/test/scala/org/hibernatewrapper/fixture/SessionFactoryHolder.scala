package org.hibernatewrapper.fixture

import java.util.Properties
import javax.sql.DataSource

import org.h2.jdbcx.JdbcConnectionPool
import org.hibernate.cfg.AvailableSettings._
import org.hibernate.dialect.H2Dialect
import org.hibernatewrapper.servlet.model.{Task, User}

object SessionFactoryHolder {

  private val p = new Properties()
  p.put(HBM2DDL_AUTO, "update")
    //p.put(SHOW_SQL, "true")


  lazy val sessionFactory =
    SessionFactoryBuilder.dataSource(dataSource)
      .dialect(classOf[H2Dialect])
//      .namingStrategy(ImprovedNamingStrategy.INSTANCE)
      .annotatedClasses(classOf[User], classOf[Task]).props(p).build()

  def dataSource: DataSource = {
    JdbcConnectionPool.create("jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "sa", "")
  }

}
