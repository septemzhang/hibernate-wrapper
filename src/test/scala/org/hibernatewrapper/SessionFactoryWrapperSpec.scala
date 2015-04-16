package org.hibernatewrapper

import org.hibernate._
import org.mockito.Mockito._
import org.scalatest.FunSpec

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
        f.sfw.withTransaction { session => }
        verify(f.sf).openSession
        verify(f.session).close
      }
      it("should begin and commit transaction") {
        val f = fixture
        f.sfw.withTransaction { session => }
        verify(f.session).getTransaction
        verify(f.session).setFlushMode(FlushMode.AUTO)
        verify(f.transaction).begin
        verify(f.transaction).setTimeout(-1)
        verify(f.transaction).commit
      }
      it("should rollback and clear session when exception raised") {
        val f = fixture
        intercept[RuntimeException] {
          f.sfw.withTransaction { session => throw new RuntimeException }
        }
        verify(f.session).clear
        verify(f.transaction).rollback
      }
      it("should rollback with rollback attr") {
        val f = fixture
        f.sfw.withTransaction(TXAttr().rollback(true)) { session => }
        verify(f.session).clear
        verify(f.transaction).rollback
      }
      it("should flush manually with readonly attr") {
        val f = fixture
        f.sfw.withTransaction(TXAttr().readOnly(true)) { session => }
        verify(f.session).setFlushMode(FlushMode.MANUAL)
      }
      it("should set timeout with timeout attr") {
        val f = fixture
        val timeout = 1
        f.sfw.withTransaction(TXAttr().timeout(timeout)) { session => }
        verify(f.transaction).setTimeout(timeout)
      }
      it("should commit for exception specified in attr") {
        val f = fixture
        intercept[NullPointerException] {
          f.sfw.withTransaction(TXAttr(commitOn = Set(classOf[NullPointerException]))) {
            session => throw new NullPointerException
          }
        }
        verify(f.transaction).commit
      }
    }

    describe("withCurrentSession") {
      it("should start a new transaction") {
        val f = fixture
        f.sfw.withCurrentSession { session => }
        verify(f.sf).getCurrentSession
        verify(f.sf, never()).openSession()
        verify(f.transaction).begin()
      }
    }

    describe("find") {
      it("should create query with parameters") {
        val f = fixture
        val hql = "hql"
        val query = mock(classOf[Query])

        when(f.session.createQuery(hql)).thenReturn(query)
        SessionWrapper(f.session).find(hql, "v1", "v2")
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
        SessionWrapper(f.session).findUnique(hql)
        verify(query).uniqueResult
      }
    }

    describe("get") {
      it("should get entity from session") {
        val f = fixture
        SessionWrapper(f.session).get[Object](1L)
        verify(f.session).get(classOf[Object], 1L)
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
