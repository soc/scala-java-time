package org.threeten.bp.chrono

import java.util.Iterator
import java.util.Collections

private[chrono] object ChronologyPlatformHelper {
  def loadAdditionalChronologies: Iterator[Chronology] = Collections.emptyIterator[Chronology]
}
