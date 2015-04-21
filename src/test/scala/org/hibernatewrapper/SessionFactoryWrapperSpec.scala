package org.hibernatewrapper

import org.hibernate._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.hibernatewrapper.SessionWrapper._

class SessionFactoryWrapperSpec extends FunSpec {

  describe("SessionFactoryWrapper") {
    describe("withSession") {
      it("should not start new transaction") {
        val f = fixture
        f.sfw.withSession { session => }
        verify(f.session, never()).getTransaction
        verify(f.session, never()).beginTransaction
      }
    }

    describe("withTransaction") {
      it("should open and close session") {
        val f = fixture
        f.sfw.withTransaction() { session => }
        verify(f.sf).openSession
        verify(f.session).close
      }
      it("should begin and commit transaction") {
        val f = fixture
        f.sfw.withTransaction() { session => }
        verify(f.transaction).begin()
        verify(f.transaction).setTimeout(-1)
        verify(f.session).flush()
        verify(f.transaction).commit()
      }
      it("should rollback by default when exception raised") {
        val f = fixture
        intercept[RuntimeException] {
          f.sfw.withTransaction() { session => throw new RuntimeException }
        }
        verify(f.session).clear
        verify(f.transaction).rollback
      }
      it("should commit transaction for specific exception") {
        val f = fixture
        intercept[NullPointerException] {
          f.sfw.withTransaction(commitOn = Set(classOf[NullPointerException]), timeout = 1) { session =>
            throw new NullPointerException
          }
        }
        verify(f.session).flush()
        verify(f.transaction).commit()
      }
      it("should rollback in rollback transaction") {
        val f = fixture
        f.sfw.rollback { session => }
        verify(f.session).clear
        verify(f.transaction).rollback
      }
      it("should set timeout with timeout attr") {
        val f = fixture
        val timeout = 1
        f.sfw.withTransaction(timeout = timeout) { session => }
        verify(f.transaction).setTimeout(timeout)
      }
      it("should commit for specified exception") {
        val f = fixture
        assert(fixture.sfw.shouldCommitOn(new NullPointerException, Set(classOf[NullPointerException])))
        assert(!fixture.sfw.shouldCommitOn(new IllegalStateException, Set(classOf[NullPointerException])))
      }
      it("should rollback on all exceptions by default") {
        List(new Throwable, new Exception, new RuntimeException, new NullPointerException).foreach { e =>
          assert(!fixture.sfw.shouldCommitOn(e, Set()))
        }
      }
      it("should compile for default parameters") {
        val f = fixture
        val swf = f.sfw
        swf.withTransaction(Set(classOf[RuntimeException]), 1) { session => }
        //        swf.withTransaction(commitOn = Set(classOf[RuntimeException])) { session => }
        //        swf.withTransaction(1) { session => }
        swf.withTransaction(timeout = 1) { session => }
        swf.withTransaction() { session => }
      }

    }

//    describe("withCurrentSession") {
//      it("should start a new transaction") {
//        val f = fixture
//        f.sfw.withCurrentSession { session => }
//        verify(f.sf).getCurrentSession
//        verify(f.sf, never()).openSession()
//        verify(f.transaction).begin()
//      }
//    }

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
        val f = fixture
        f.session.getById[Object](1L)
        verify(f.session).get(classOf[Object], 1L)
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
    val sfw = new SessionFactoryWrapper(sf)
  }

}
