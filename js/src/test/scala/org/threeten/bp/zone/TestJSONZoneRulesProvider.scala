package org.threeten.bp.zone

import org.testng.annotations.Test
import org.testng.Assert._

import org.threeten.bp._

class TestJSONZoneRulesProvider {
  @Test def test: Unit = {
    val x = JSONZoneRulesProvider.fromUnpackedJSON
    val zonedDateTime2 = ZonedDateTime.now(ZoneRegion.ofId("Europe/Berlin", true))

  }
}
