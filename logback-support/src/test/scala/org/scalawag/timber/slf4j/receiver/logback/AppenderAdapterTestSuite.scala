package org.scalawag.timber.slf4j.receiver.logback

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalawag.timber.api.slf4j
import org.scalawag.timber.dsl._
import org.scalawag.timber.impl.formatter.DefaultEntryFormatter

import org.scalawag.timber.slf4j.receiver.logback
import ch.qos.logback.core.FileAppender
import org.scalawag.timber.impl.Entry

class AppenderAdapterTestSuite extends FunSuite with ShouldMatchers {
  import LogbackSupport._

  implicit val formatter = new DefaultEntryFormatter
  test("test file") {
    slf4j.LoggerManager.configure { IN =>
      withLogbackSupport { implicit context =>
        val file = new AppenderAdapter(logback.file("/tmp/blah")) with StopAtShutdown
        IN :: file
      }
    }

    val log = slf4j.LoggerManager.getLogger("timber.slf4j.receiver.logback")
    log.log(1,"log message")
  }

  test("test rolling file") {
    slf4j.LoggerManager.configure { IN =>
      withLogbackSupport { implicit context =>
        val rollingPolicy = logback.timeBasedRollingPolicy("/tmp/blahr-%d{yyyy-MM-dd-HH-mm-ss}.log")
        val file = new AppenderAdapter(logback.rollingFile("/tmp/blahr",rollingPolicy)) with StopAtShutdown
        IN :: file
      }
    }

    val log = slf4j.LoggerManager.getLogger("timber.slf4j.receiver.logback")
    (1 to 5) foreach { n =>
      log.log(1,"log message " + n)
      Thread.sleep(1000)
    }
  }

  test("test any logback ") {
    slf4j.LoggerManager.configure { IN =>
      withLogbackSupport { implicit context =>
        val encoder = new EncoderAdapter(new DefaultEntryFormatter)
        encoder.setContext(context)
        context.add(encoder)

        val appender = new FileAppender[Entry]
        appender.setContext(context)
        appender.setFile("/tmp/test.log")
        appender.setEncoder(encoder)
        appender.setName("test")
        context.add(appender)

        val file = new AppenderAdapter(appender) with StopAtShutdown
        IN :: file
      }
    }

    val log = slf4j.LoggerManager.getLogger("timber.slf4j.receiver.logback")
    log.log(1,"log message")
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
