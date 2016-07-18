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
import org.threeten.bp.temporal.ChronoField.OFFSET_SECONDS
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.ZoneOffset

/** Test ZoneOffsetPrinterParser. */
object TestZoneOffsetPrinter {
  private val OFFSET_0130: ZoneOffset = ZoneOffset.of("+01:30")
}

@Test class TestZoneOffsetPrinter extends AbstractTestPrinterParser {
  @DataProvider(name = "offsets") private[format] def provider_offsets: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("+HH", "NO-OFFSET", ZoneOffset.UTC), Array("+HH", "+01", ZoneOffset.ofHours(1)), Array("+HH", "-01", ZoneOffset.ofHours(-1)), Array("+HHMM", "NO-OFFSET", ZoneOffset.UTC), Array("+HHMM", "+0102", ZoneOffset.ofHoursMinutes(1, 2)), Array("+HHMM", "-0102", ZoneOffset.ofHoursMinutes(-1, -2)), Array("+HH:MM", "NO-OFFSET", ZoneOffset.UTC), Array("+HH:MM", "+01:02", ZoneOffset.ofHoursMinutes(1, 2)), Array("+HH:MM", "-01:02", ZoneOffset.ofHoursMinutes(-1, -2)), Array("+HHMMss", "NO-OFFSET", ZoneOffset.UTC), Array("+HHMMss", "+0100", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HHMMss", "+0102", ZoneOffset.ofHoursMinutesSeconds(1, 2, 0)), Array("+HHMMss", "+0159", ZoneOffset.ofHoursMinutesSeconds(1, 59, 0)), Array("+HHMMss", "+0200", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HHMMss", "+1800", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HHMMss", "+010215", ZoneOffset.ofHoursMinutesSeconds(1, 2, 15)), Array("+HHMMss", "-0100", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HHMMss", "-0200", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HHMMss", "-1800", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HHMMss", "NO-OFFSET", ZoneOffset.UTC), Array("+HHMMss", "+0100", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HHMMss", "+010203", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HHMMss", "+015959", ZoneOffset.ofHoursMinutesSeconds(1, 59, 59)), Array("+HHMMss", "+0200", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HHMMss", "+1800", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HHMMss", "-0100", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HHMMss", "-0200", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HHMMss", "-1800", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HH:MM:ss", "NO-OFFSET", ZoneOffset.UTC), Array("+HH:MM:ss", "+01:00", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HH:MM:ss", "+01:02", ZoneOffset.ofHoursMinutesSeconds(1, 2, 0)), Array("+HH:MM:ss", "+01:59", ZoneOffset.ofHoursMinutesSeconds(1, 59, 0)), Array("+HH:MM:ss", "+02:00", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HH:MM:ss", "+18:00", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HH:MM:ss", "+01:02:15", ZoneOffset.ofHoursMinutesSeconds(1, 2, 15)), Array("+HH:MM:ss", "-01:00", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HH:MM:ss", "-02:00", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HH:MM:ss", "-18:00", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HH:MM:ss", "NO-OFFSET", ZoneOffset.UTC), Array("+HH:MM:ss", "+01:00", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HH:MM:ss", "+01:02:03", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HH:MM:ss", "+01:59:59", ZoneOffset.ofHoursMinutesSeconds(1, 59, 59)), Array("+HH:MM:ss", "+02:00", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HH:MM:ss", "+18:00", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HH:MM:ss", "-01:00", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HH:MM:ss", "-02:00", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HH:MM:ss", "-18:00", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HHMMSS", "NO-OFFSET", ZoneOffset.UTC), Array("+HHMMSS", "+010203", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HHMMSS", "-010203", ZoneOffset.ofHoursMinutesSeconds(-1, -2, -3)), Array("+HHMMSS", "+010200", ZoneOffset.ofHoursMinutesSeconds(1, 2, 0)), Array("+HHMMSS", "-010200", ZoneOffset.ofHoursMinutesSeconds(-1, -2, 0)), Array("+HH:MM:SS", "NO-OFFSET", ZoneOffset.UTC), Array("+HH:MM:SS", "+01:02:03", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HH:MM:SS", "-01:02:03", ZoneOffset.ofHoursMinutesSeconds(-1, -2, -3)), Array("+HH:MM:SS", "+01:02:00", ZoneOffset.ofHoursMinutesSeconds(1, 2, 0)), Array("+HH:MM:SS", "-01:02:00", ZoneOffset.ofHoursMinutesSeconds(-1, -2, 0)))
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_print(pattern: String, expected: String, offset: ZoneOffset): Unit = {
    buf.append("EXISTING")
    printContext.setDateTime(new DateTimeBuilder(OFFSET_SECONDS, offset.getTotalSeconds))
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("NO-OFFSET", pattern)
    pp.print(printContext, buf)
    assertEquals(buf.toString, "EXISTING" + expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_toString(pattern: String, expected: String, offset: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("NO-OFFSET", pattern)
    assertEquals(pp.toString, "Offset(" + pattern + ",'NO-OFFSET')")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_emptyCalendrical(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    pp.print(printEmptyContext, buf)
  }

  @throws(classOf[Exception])
  def test_print_emptyAppendable(): Unit = {
    printContext.setDateTime(new DateTimeBuilder(OFFSET_SECONDS, TestZoneOffsetPrinter.OFFSET_0130.getTotalSeconds))
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    pp.print(printContext, buf)
    assertEquals(buf.toString, "+01:30")
  }
}
