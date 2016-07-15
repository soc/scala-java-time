package org.threeten.bp.zone

import java.util.Iterator
import java.util.ServiceLoader

private[zone] object ZoneRulesProviderPlatformHelper {
  def loadAdditionalZoneRulesProviders: Iterator[ZoneRulesProvider] = ServiceLoader.load(classOf[ZoneRulesProvider], classOf[ZoneRulesProvider].getClassLoader).iterator
}
