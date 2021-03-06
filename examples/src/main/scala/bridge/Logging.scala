package bridge

import org.scalawag.timber.api.slf4j._
import org.scalawag.timber.api.LoggerFactory
import org.scalawag.timber.impl.dispatcher.SynchronousEntryDispatcher
import org.scalawag.timber.impl.LoggerImpl
import org.scalawag.timber.bridge.slf4j.Slf4jBridgeLoggerFactory

object Logging {

  class LoggerManager extends SynchronousEntryDispatcher[Logger] with LoggerFactory[Logger] {
    /* I'm just reversing the name here to make is easy to tell that we're using the correct factory. */
    override def getLogger(name:String):Logger =
      new LoggerImpl(name.reverse,this) with Trace with Debug with Info with Warn with Error
  }

  /* This tells the slf4j bridge to use our factory here instead of the default one.  It must be called by the
   * application prior to any slf4j loggers being created (for homogeneous Loggers).  It's
   */

  def configurate {
    Slf4jBridgeLoggerFactory.factory = new LoggerManager
  }

}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
