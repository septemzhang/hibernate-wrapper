package org.hibernatewrapper

import java.io.FileNotFoundException

import org.scalatest.FunSpec

class TXAttrSpec extends FunSpec {

  describe("Transaction Attribute") {
    it("should rollback on all exceptions by default") {
      val defaultTXAttr = TXAttr()
      assertRollbackOn(defaultTXAttr, new Throwable, new Exception, new RuntimeException, new NullPointerException)
    }

    it("should commit on specific exceptions") {
      val attr = TXAttr(commitOn = Set(classOf[FileNotFoundException], classOf[IllegalArgumentException]))
      assertCommitOn(attr, new FileNotFoundException, new IllegalArgumentException)
      assertRollbackOn(attr, new IllegalStateException, new RuntimeException)
    }

//    it("should commit on checked exceptions and rollback on runtime exceptions") {
//      val attr = TXAttr(commitOn = Set(classOf[Exception]))
//      assertCommitOn(attr, new FileNotFoundException, new Exception)
//      assertRollbackOn(attr, new IllegalStateException, new RuntimeException)
//    }

  }

  private def assertCommitOn(attr: TXAttr, exceptions: Throwable*) {
    exceptions.foreach { e => assert(attr.shouldCommitOn(e) == true) }
  }

  private def assertRollbackOn(attr: TXAttr, exceptions: Throwable*) {
    exceptions.foreach { e => assert(attr.shouldCommitOn(e) == false) }
  }

}

