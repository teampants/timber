package org.scalawag.timber.api

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.util.concurrent.CyclicBarrier
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.Await._
import collection.immutable.Stack

class LoggingContextTestSuite extends FunSuite with ShouldMatchers with BeforeAndAfter {

  before {
    LoggingContext.clear
  }

  after {
    LoggingContext.clear
  }

  test("start empty") {
    LoggingContext.get should be (Map())
  }

  test("push") {
    LoggingContext.push("a","1")
    LoggingContext.get should be (Map("a" -> Stack("1")))
  }

  test("push map") {
    LoggingContext.push(Map("a" -> "1","b" -> "2"))
    LoggingContext.get should be (Map("a" -> Stack("1"),"b" -> Stack("2")))
  }

  test("multiple pushes, different keys") {
    LoggingContext.push("a","1")
    LoggingContext.push("b","2")
    LoggingContext.get should be (Map("a" -> Stack("1"),"b" -> Stack("2")))
  }

  test("multiple pushes, same key") {
    LoggingContext.push("a","1")
    LoggingContext.push("a","2")
    LoggingContext.get should be (Map("a" -> Stack("2","1")))
  }

  test("multiple push maps") {
    LoggingContext.push(Map("a" -> "1","b" -> "2"))
    LoggingContext.push(Map("a" -> "3","c" -> "4"))
    LoggingContext.get should be (Map("a" -> Stack("3","1"),"b" -> Stack("2"),"c" -> Stack("4")))
  }

  test("push/getInnermost") {
    LoggingContext.push("a","1")
    LoggingContext.getInnermost should be (Map("a" -> "1"))
  }

  test("push multiple/getInnermost") {
    LoggingContext.push("a","1")
    LoggingContext.push("a","2")
    LoggingContext.getInnermost should be (Map("a" -> "2"))
  }


  test("separate contexts for separate threads") {
    import scala.concurrent.ExecutionContext.Implicits.global

    val barrier = new CyclicBarrier(2)

    val f1 = future {
      LoggingContext.push("ip","127.0.0.1")
      barrier.await
      LoggingContext.get.get("ip").head should be ("127.0.0.1")
      barrier.await
    }

    val f2 = future {
      LoggingContext.push("ip","127.0.0.2")
      barrier.await
      LoggingContext.get.get("ip").head should be ("127.0.0.2")
      barrier.await
    }

    ready(f1,Duration.Inf)
    ready(f2,Duration.Inf)
  }

  test("pop") {
    LoggingContext.push("a","1")
    LoggingContext.pop("a","1")
    LoggingContext.get should be (Map())
  }

  test("pop with remains") {
    LoggingContext.push("a","1")
    LoggingContext.push("a","2")
    LoggingContext.pop("a","2")
    LoggingContext.get should be (Map("a" -> Stack("1")))
  }

  test("pop doesn't affect other keys") {
    LoggingContext.push("a","1")
    LoggingContext.push("b","2")
    LoggingContext.pop("a","1")
    LoggingContext.get should be (Map("b" -> Stack("2")))
  }

  test("pop - empty stack (fail)") {
    evaluating(LoggingContext.pop("a","1")) should produce [IllegalStateException]
  }

  test("pop - missing key (fail)") {
    LoggingContext.push("a","1")
    evaluating(LoggingContext.pop("b","1")) should produce [IllegalStateException]
  }

  test("pop - wrong value (fail)") {
    LoggingContext.push("a","1")
    evaluating(LoggingContext.pop("a","2")) should produce [IllegalStateException]
  }

  test("in") {
    LoggingContext.in("a","1") {
      LoggingContext.get should be (Map("a" -> Stack("1")))
    }
    LoggingContext.get should be (Map())
  }

  test("in (deeper stack)") {
    LoggingContext.push("a","1")
    LoggingContext.in("a","2") {
      LoggingContext.get should be (Map("a" -> Stack("2","1")))
    }
    LoggingContext.get should be (Map("a" -> Stack("1")))
  }

  test("in map") {
    LoggingContext.push("a","1")
    LoggingContext.in("a","2") {
      LoggingContext.get should be (Map("a" -> Stack("2","1")))
    }
    LoggingContext.get should be (Map("a" -> Stack("1")))
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
