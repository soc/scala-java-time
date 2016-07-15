package org.threeten.bp.chrono

import java.util.Iterator
import java.util.ServiceLoader

private[chrono] object ChronologyPlatformHelper {
  def loadAdditionalChronologies: Iterator[Chronology] = ServiceLoader.load(classOf[Chronology], classOf[Chronology].getClassLoader).iterator
}
