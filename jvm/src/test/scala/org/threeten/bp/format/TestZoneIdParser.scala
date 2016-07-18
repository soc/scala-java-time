/*
 * Copyright (c) 2007-present, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.threeten.bp.format

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.TemporalQueries
import org.threeten.bp.zone.ZoneRulesProvider

/** Test ZonePrinterParser. */
object TestZoneIdParser {
  private val AMERICA_DENVER: String = "America/Denver"
  private val TIME_ZONE_DENVER: ZoneId = ZoneId.of(AMERICA_DENVER)
}

@Test class TestZoneIdParser extends AbstractTestPrinterParser {
  @DataProvider(name = "error") private[format] def data_error: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null), "hello", -1, classOf[IndexOutOfBoundsException]), Array(new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null), "hello", 6, classOf[IndexOutOfBoundsException]))
  }

  @Test(dataProvider = "error") def test_parse_error(pp: DateTimeFormatterBuilder.ZoneIdPrinterParser, text: String, pos: Int, expected: Class[_]): Unit = {
    try {
      pp.parse(parseContext, text, pos)
    }
    catch {
      case ex: RuntimeException =>
        assertTrue(expected.isInstance(ex))
        assertEquals(parseContext.toParsed.fieldValues.size, 0)
    }
  }

  @throws(classOf[Exception])
  def test_parse_exactMatch_Denver(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, TestZoneIdParser.AMERICA_DENVER, 0)
    assertEquals(result, TestZoneIdParser.AMERICA_DENVER.length)
    assertParsed(TestZoneIdParser.TIME_ZONE_DENVER)
  }

  @throws(classOf[Exception])
  def test_parse_startStringMatch_Denver(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, TestZoneIdParser.AMERICA_DENVER + "OTHER", 0)
    assertEquals(result, TestZoneIdParser.AMERICA_DENVER.length)
    assertParsed(TestZoneIdParser.TIME_ZONE_DENVER)
  }

  @throws(classOf[Exception])
  def test_parse_midStringMatch_Denver(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHER" + TestZoneIdParser.AMERICA_DENVER + "OTHER", 5)
    assertEquals(result, 5 + TestZoneIdParser.AMERICA_DENVER.length)
    assertParsed(TestZoneIdParser.TIME_ZONE_DENVER)
  }

  @throws(classOf[Exception])
  def test_parse_endStringMatch_Denver(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHER" + TestZoneIdParser.AMERICA_DENVER, 5)
    assertEquals(result, 5 + TestZoneIdParser.AMERICA_DENVER.length)
    assertParsed(TestZoneIdParser.TIME_ZONE_DENVER)
  }

  @throws(classOf[Exception])
  def test_parse_partialMatch(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHERAmerica/Bogusville", 5)
    assertEquals(result, -6)
    assertParsed(null)
  }

  @DataProvider(name = "zones") private[format] def populateTestData: Array[Array[AnyRef]] = {
    val ids: java.util.Set[String] = ZoneRulesProvider.getAvailableZoneIds
    val rtnval: Array[Array[AnyRef]] = new Array[Array[AnyRef]](ids.size)
    var i: Int = 0
    import scala.collection.JavaConversions._
    for (id <- ids) {
      rtnval({
        i += 1
        i - 1
      }) = Array[AnyRef](id, ZoneId.of(id))
    }
    rtnval
  }

  @Test(dataProvider = "zones")
  @throws(classOf[Exception])
  def test_parse_exactMatch(parse: String, expected: ZoneId): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, parse, 0)
    assertEquals(result, parse.length)
    assertParsed(expected)
  }

  @Test
  @throws(classOf[Exception])
  def test_parse_lowerCase(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    parseContext.setCaseSensitive(false)
    val result: Int = pp.parse(parseContext, "europe/london", 0)
    assertEquals(result, 13)
    assertParsed(ZoneId.of("Europe/London"))
  }

  @throws(classOf[Exception])
  def test_parse_endStringMatch_utc(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHERZ", 5)
    assertEquals(result, 6)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_endStringMatch_utc_plus1(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHER+01:00", 5)
    assertEquals(result, 11)
    assertParsed(ZoneId.of("+01:00"))
  }

  @throws(classOf[Exception])
  def test_parse_midStringMatch_utc(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHERZOTHER", 5)
    assertEquals(result, 6)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_midStringMatch_utc_plus1(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, null)
    val result: Int = pp.parse(parseContext, "OTHER+01:00OTHER", 5)
    assertEquals(result, 11)
    assertParsed(ZoneId.of("+01:00"))
  }

  def test_toString_id(): Unit = {
    val pp: DateTimeFormatterBuilder.ZoneIdPrinterParser = new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, "ZoneId()")
    assertEquals(pp.toString, "ZoneId()")
  }

  private def assertParsed(expectedZone: ZoneId): Unit = {
    assertEquals(parseContext.toParsed.zone, expectedZone)
  }
}
