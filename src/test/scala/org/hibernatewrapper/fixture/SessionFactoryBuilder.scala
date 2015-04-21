package org.hibernatewrapper.fixture

import java.util.Properties
import javax.sql.DataSource

import org.h2.jdbcx.JdbcConnectionPool
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings._
import org.hibernate.cfg.Configuration
import org.hibernate.context.internal.ThreadLocalSessionContext
import org.hibernate.dialect.H2Dialect
import org.hibernate.usertype.UserType
import org.hibernatewrapper.servlet.model.{Task, User}

object SessionFactoryBuilder {

  private val config = new Configuration()
  private val p = new Properties()

  def props(props: Properties) = {
    config.addProperties(props);
    this
  }

  def dataSource(dataSource: DataSource) = {
    p.put(DATASOURCE, dataSource)
    this
  }

  def dialect(dialect: Class[_]) = {
    p.put(DIALECT, dialect.getName)
    this
  }

  //  def currentSessionContext(context: Class[_]) = {
  //    p.put(CURRENT_SESSION_CONTEXT_CLASS, context.getName)
  //    this
  //  }

//  def namingStrategy(strategy: NamingStrategy) = {
//    config.setNamingStrategy(strategy)
//    this
//  }

  def typeOverride(userType: UserType, keys: Array[String]) = {
    config.registerTypeOverride(userType, keys)
    this
  }

  def annotatedClasses(classes: Class[_]*) = {
    classes foreach { config.addAnnotatedClass(_) }
    this
  }

  def build() : SessionFactory = {
    p.put(CURRENT_SESSION_CONTEXT_CLASS, classOf[ThreadLocalSessionContext].getName)
    config.addProperties(p)
    config.buildSessionFactory(
      new StandardServiceRegistryBuilder().applySettings(config.getProperties).build
    )
  }

  //singleton of SessionFactory
  lazy val sessionFactory =
    SessionFactoryBuilder.dataSource(newDataSource).props(newProps)
      .dialect(classOf[H2Dialect])
       //      .namingStrategy(ImprovedNamingStrategy.INSTANCE)
      .annotatedClasses(classOf[User], classOf[Task])
      .build()

  private def newProps = {
    val p = new Properties()
    p.put(HBM2DDL_AUTO, "update")
    //p.put(SHOW_SQL, "true")
    p
  }

  private def newDataSource = {
    JdbcConnectionPool.create("jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "sa", "")
  }

}
