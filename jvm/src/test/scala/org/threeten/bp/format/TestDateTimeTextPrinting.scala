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
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import java.util.Locale
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.temporal.TemporalField

/** Test text printing. */
@Test class TestDateTimeTextPrinting {
  private var builder: DateTimeFormatterBuilder = null

  @BeforeMethod def setUp(): Unit = {
    builder = new DateTimeFormatterBuilder
  }

  @DataProvider(name = "printText") private[format] def data_text: Array[Array[Any]] = {
    Array[Array[Any]](Array(DAY_OF_WEEK, TextStyle.FULL, 1, "Monday"), Array(DAY_OF_WEEK, TextStyle.FULL, 2, "Tuesday"), Array(DAY_OF_WEEK, TextStyle.FULL, 3, "Wednesday"), Array(DAY_OF_WEEK, TextStyle.FULL, 4, "Thursday"), Array(DAY_OF_WEEK, TextStyle.FULL, 5, "Friday"), Array(DAY_OF_WEEK, TextStyle.FULL, 6, "Saturday"), Array(DAY_OF_WEEK, TextStyle.FULL, 7, "Sunday"), Array(DAY_OF_WEEK, TextStyle.SHORT, 1, "Mon"), Array(DAY_OF_WEEK, TextStyle.SHORT, 2, "Tue"), Array(DAY_OF_WEEK, TextStyle.SHORT, 3, "Wed"), Array(DAY_OF_WEEK, TextStyle.SHORT, 4, "Thu"), Array(DAY_OF_WEEK, TextStyle.SHORT, 5, "Fri"), Array(DAY_OF_WEEK, TextStyle.SHORT, 6, "Sat"), Array(DAY_OF_WEEK, TextStyle.SHORT, 7, "Sun"), Array(DAY_OF_MONTH, TextStyle.FULL, 1, "1"), Array(DAY_OF_MONTH, TextStyle.FULL, 2, "2"), Array(DAY_OF_MONTH, TextStyle.FULL, 3, "3"), Array(DAY_OF_MONTH, TextStyle.FULL, 28, "28"), Array(DAY_OF_MONTH, TextStyle.FULL, 29, "29"), Array(DAY_OF_MONTH, TextStyle.FULL, 30, "30"), Array(DAY_OF_MONTH, TextStyle.FULL, 31, "31"), Array(DAY_OF_MONTH, TextStyle.SHORT, 1, "1"), Array(DAY_OF_MONTH, TextStyle.SHORT, 2, "2"), Array(DAY_OF_MONTH, TextStyle.SHORT, 3, "3"), Array(DAY_OF_MONTH, TextStyle.SHORT, 28, "28"), Array(DAY_OF_MONTH, TextStyle.SHORT, 29, "29"), Array(DAY_OF_MONTH, TextStyle.SHORT, 30, "30"), Array(DAY_OF_MONTH, TextStyle.SHORT, 31, "31"), Array(MONTH_OF_YEAR, TextStyle.FULL, 1, "January"), Array(MONTH_OF_YEAR, TextStyle.FULL, 12, "December"), Array(MONTH_OF_YEAR, TextStyle.SHORT, 1, "Jan"), Array(MONTH_OF_YEAR, TextStyle.SHORT, 12, "Dec"))
  }

  @Test(dataProvider = "printText")
  @throws(classOf[Exception])
  def test_appendText2arg_print(field: TemporalField, style: TextStyle, value: Int, expected: String): Unit = {
    val f: DateTimeFormatter = builder.appendText(field, style).toFormatter(Locale.ENGLISH)
    var dt: LocalDateTime = LocalDateTime.of(2010, 1, 1, 0, 0)
    dt = dt.`with`(field, value)
    val text: String = f.format(dt)
    assertEquals(text, expected)
  }

  @Test(dataProvider = "printText")
  @throws(classOf[Exception])
  def test_appendText1arg_print(field: TemporalField, style: TextStyle, value: Int, expected: String): Unit = {
    if (style eq TextStyle.FULL) {
      val f: DateTimeFormatter = builder.appendText(field).toFormatter(Locale.ENGLISH)
      var dt: LocalDateTime = LocalDateTime.of(2010, 1, 1, 0, 0)
      dt = dt.`with`(field, value)
      val text: String = f.format(dt)
      assertEquals(text, expected)
    }
  }

  @Test
  @throws(classOf[Exception])
  def test_print_appendText2arg_french_long(): Unit = {
    val f: DateTimeFormatter = builder.appendText(MONTH_OF_YEAR, TextStyle.FULL).toFormatter(Locale.FRENCH)
    val dt: LocalDateTime = LocalDateTime.of(2010, 1, 1, 0, 0)
    val text: String = f.format(dt)
    assertEquals(text, "janvier")
  }

  @Test
  @throws(classOf[Exception])
  def test_print_appendText2arg_french_short(): Unit = {
    val f: DateTimeFormatter = builder.appendText(MONTH_OF_YEAR, TextStyle.SHORT).toFormatter(Locale.FRENCH)
    val dt: LocalDateTime = LocalDateTime.of(2010, 1, 1, 0, 0)
    val text: String = f.format(dt)
    assertEquals(text, "janv.")
  }

  @Test
  @throws(classOf[Exception])
  def test_appendTextMap(): Unit = {
    val map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
    map.put(1L, "JNY")
    map.put(2L, "FBY")
    map.put(3L, "MCH")
    map.put(4L, "APL")
    map.put(5L, "MAY")
    map.put(6L, "JUN")
    map.put(7L, "JLY")
    map.put(8L, "AGT")
    map.put(9L, "SPT")
    map.put(10L, "OBR")
    map.put(11L, "NVR")
    map.put(12L, "DBR")
    builder.appendText(MONTH_OF_YEAR, map)
    val f: DateTimeFormatter = builder.toFormatter
    val dt: LocalDateTime = LocalDateTime.of(2010, 1, 1, 0, 0)
    for (month <- Month.values) {
      assertEquals(f.format(dt.`with`(month)), map.get(month.getValue.toLong))
    }
  }

  @Test
  @throws(classOf[Exception])
  def test_appendTextMap_DOM(): Unit = {
    val map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
    map.put(1L, "1st")
    map.put(2L, "2nd")
    map.put(3L, "3rd")
    builder.appendText(DAY_OF_MONTH, map)
    val f: DateTimeFormatter = builder.toFormatter
    val dt: LocalDateTime = LocalDateTime.of(2010, 1, 1, 0, 0)
    assertEquals(f.format(dt.withDayOfMonth(1)), "1st")
    assertEquals(f.format(dt.withDayOfMonth(2)), "2nd")
    assertEquals(f.format(dt.withDayOfMonth(3)), "3rd")
  }

  @Test
  @throws(classOf[Exception])
  def test_appendTextMapIncomplete(): Unit = {
    val map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
    map.put(1L, "JNY")
    builder.appendText(MONTH_OF_YEAR, map)
    val f: DateTimeFormatter = builder.toFormatter
    val dt: LocalDateTime = LocalDateTime.of(2010, 2, 1, 0, 0)
    assertEquals(f.format(dt), "2")
  }
}
