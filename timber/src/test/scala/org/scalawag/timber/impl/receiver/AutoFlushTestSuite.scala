package org.scalawag.timber.impl.receiver

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import java.io.PrintWriter
import org.scalawag.timber.impl.Entry
import org.mockito.Mockito._
import org.scalawag.timber.impl.formatter.DefaultEntryFormatter

class AutoFlushTestSuite extends FunSuite with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  private val pw = mock[PrintWriter]
  private val oneLineEntry = new Entry("foo","logger",0,"DEBUG")
  private val twoLineEntry = new Entry("foo\nbar","logger",0,"DEBUG")

  test("receive without AutoFlush") {
    val receiver = new WriterReceiver(pw,new DefaultEntryFormatter)
    receiver.receive(oneLineEntry)
    verify(pw,times(0)).flush()
  }

  test("receive with AutoFlush") {
    val receiver = new WriterReceiver(pw,new DefaultEntryFormatter) with AutoFlush
    receiver.receive(oneLineEntry)
    verify(pw,times(1)).flush()
  }

  test("receive with AutoFlush (and multi-line message)") {
    val receiver = new WriterReceiver(pw,new DefaultEntryFormatter) with AutoFlush
    receiver.receive(twoLineEntry)
    verify(pw,times(1)).flush()
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
