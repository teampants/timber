package org.scalawag.timber.impl.receiver

import org.scalawag.timber.impl.Entry

trait EntryReceiver {
  def receive(entry:Entry)
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
