package org.threeten.bp.zone

import java.util.Iterator
import java.util.Collections

private[zone] object ZoneRulesProviderPlatformHelper {
  def loadAdditionalZoneRulesProviders: Iterator[ZoneRulesProvider] = Collections.emptyIterator[ZoneRulesProvider]
}
