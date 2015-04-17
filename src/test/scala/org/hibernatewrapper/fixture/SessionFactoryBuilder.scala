package org.hibernatewrapper.fixture

import java.util.Properties
import javax.sql.DataSource

import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings._
import org.hibernate.cfg.{Configuration, NamingStrategy}
import org.hibernate.context.internal.ThreadLocalSessionContext
import org.hibernate.usertype.UserType

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

}
