package org.hibernatewrapper

import org.hibernate._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.hibernatewrapper.SessionWrapper._
import org.hibernatewrapper.NewCreatedSession._

class SessionFactoryWrapperSpec extends FunSpec {

  describe("SessionFactoryWrapper") {
    describe("withSession") {
      it("should not start new transaction") {
        val f = fixture
        f.sf.withSession { session => }
        verify(f.session, never()).getTransaction
        verify(f.session, never()).beginTransaction
      }
    }

    describe("withTransaction") {
      it("should open and close session") {
        val f = fixture
        f.sf.withTransaction() { session => }
        verify(f.sf).openSession
        verify(f.session).close
      }
      it("should begin and commit transaction") {
        val f = fixture
        f.sf.withTransaction() { session => }
        verify(f.transaction).begin()
        verify(f.transaction).setTimeout(-1)
        verify(f.session).flush()
        verify(f.transaction).commit()
      }
      it("should rollback by default when exception raised") {
        val f = fixture
        intercept[RuntimeException] {
          f.sf.withTransaction() { session => throw new RuntimeException }
        }
        verify(f.session).clear
        verify(f.transaction).rollback
      }
      it("should commit transaction for specific exception") {
        val f = fixture
        intercept[NullPointerException] {
          f.sf.withTransaction(commitOn = Set(classOf[NullPointerException]), timeout = 1) { session =>
            throw new NullPointerException
          }
        }
        verify(f.session).flush()
        verify(f.transaction).commit()
      }
      it("should rollback in rollback transaction") {
        val f = fixture
        f.sf.rollback { session => }
        verify(f.session).clear
        verify(f.transaction).rollback
      }
      it("should set timeout with timeout attr") {
        val f = fixture
        val timeout = 1
        f.sf.withTransaction(timeout = timeout) { session => }
        verify(f.transaction).setTimeout(timeout)
      }
      it("should commit for specified exception") {
        val f = fixture
        assert(fixture.sf.shouldCommitOn(new NullPointerException, Set(classOf[NullPointerException])))
        assert(!fixture.sf.shouldCommitOn(new IllegalStateException, Set(classOf[NullPointerException])))
      }
      it("should rollback on all exceptions by default") {
        List(new Throwable, new Exception, new RuntimeException, new NullPointerException).foreach { e =>
          assert(!fixture.sf.shouldCommitOn(e, Set()))
        }
      }
      it("should compile for default parameters") {
        val sf = fixture.sf
        sf.withTransaction(Set(classOf[RuntimeException]), 1) { session => }
        //        swf.withTransaction(commitOn = Set(classOf[RuntimeException])) { session => }
        //        swf.withTransaction(1) { session => }
        sf.withTransaction(timeout = 1) { session => }
        sf.withTransaction() { session => }
      }

    }

    describe("find") {
      it("should create query with parameters") {
        val f = fixture
        val hql = "hql"
        val query = mock(classOf[Query])

        when(f.session.createQuery(hql)).thenReturn(query)
        f.session.find(hql, "v1", "v2")
        verify(query).setParameter(0, "v1")
        verify(query).setParameter(1, "v2")
        verify(query).list
      }
    }

    describe("findUnique") {
      it("should work for single result") {
        val f = fixture
        val hql = "hql"
        val query = mock(classOf[Query])
        when(f.session.createQuery(hql)).thenReturn(query)
        f.session.findUnique(hql)
        verify(query).uniqueResult
      }
    }

    describe("getById") {
      it("should get entity from session") {
        val id: scala.Long = 1L
        val f = fixture
        f.session.getById[Object](id)
        verify(f.session).get(classOf[Object], id)
      }
    }

    describe("loadById") {
      it("should load entity from session") {
        val f = fixture
        f.session.loadById[Object](1L)
        verify(f.session).load(classOf[Object], 1L)
      }
    }

  }

  def fixture = new {
    val sf = mock(classOf[SessionFactory])
    val session = mock(classOf[Session])
    val transaction = mock(classOf[Transaction])
    when(sf.openSession()).thenReturn(session)
    when(sf.getCurrentSession).thenReturn(session)
    when(session.getTransaction).thenReturn(transaction)
//    val sfw = new SessionFactoryWrapper(sf)
  }

}
