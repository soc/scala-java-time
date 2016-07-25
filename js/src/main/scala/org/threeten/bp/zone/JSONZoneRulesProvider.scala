package org.threeten.bp.zone

import java.util.Collections

import scala.scalajs.js.{JSON, Dynamic}
import org.scalajs.dom

object JSONZoneRulesProvider {

  def fromUnpackedJSON: JSONZoneRulesProvider = {
    val request = new dom.raw.XMLHttpRequest
    request.onreadystatechange = (e: dom.raw.Event) =>
        if (request.readyState == 4 && request.status == 200) {
          //val json = JSON.parse(request.responseText)

        }
    //request.open("GET", "tzdb.json")
    //request.send()

    val json = JSON.parse(JSONZoneRulesData.tzdb)
    //println(json.version)
    val zoneIds = readZoneIds(json)

    new JSONZoneRulesProvider(zoneIds)
  }

  def readZoneIds(json: Dynamic): java.util.Set[String] = {
    val zoneIds = new java.util.HashSet[String]
    json.zones.forEach((zone: Dynamic) => zoneIds.add(zone.name.asInstanceOf[String]))
    zoneIds
  }
}

final class JSONZoneRulesProvider(private val zoneIds: java.util.Set[String]) extends ZoneRulesProvider {
  // Fails due to https://github.com/scala-js/scala-js/issues/2539
  ZoneRulesProvider.registerProvider(this)

  protected def provideZoneIds: java.util.Set[String] = zoneIds

  protected def provideRules(regionId: String, forCaching: Boolean): ZoneRules = null

  protected def provideVersions(zoneId: String): java.util.NavigableMap[String, ZoneRules] = Collections.emptyNavigableMap[String, ZoneRules]
}
