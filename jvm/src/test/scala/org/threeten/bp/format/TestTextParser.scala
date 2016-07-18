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
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import java.util.Locale
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries

/** Test TextPrinterParser. */
object TestTextParser {
  private val PROVIDER: DateTimeTextProvider = DateTimeTextProvider.getInstance
}

@Test class TestTextParser extends AbstractTestPrinterParser {
  @DataProvider(name = "error") private[format] def data_error: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextParser.PROVIDER), "Monday", -1, classOf[IndexOutOfBoundsException]), Array(new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextParser.PROVIDER), "Monday", 7, classOf[IndexOutOfBoundsException]))
  }

  @Test(dataProvider = "error") def test_parse_error(pp: DateTimeFormatterBuilder.TextPrinterParser, text: String, pos: Int, expected: Class[_]): Unit = {
    try {
      pp.parse(parseContext, text, pos)
    }
    catch {
      case ex: RuntimeException =>
        assertTrue(expected.isInstance(ex))
        assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
        assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
    }
  }

  @throws(classOf[Exception])
  def test_parse_midStr(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "XxxMondayXxx", 3)
    assertEquals(newPos, 9)
    assertParsed(parseContext, DAY_OF_WEEK, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_remainderIgnored(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Wednesday", 0)
    assertEquals(newPos, 3)
    assertParsed(parseContext, DAY_OF_WEEK, 3L)
  }

  @throws(classOf[Exception])
  def test_parse_noMatch1(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Munday", 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @throws(classOf[Exception])
  def test_parse_noMatch2(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Monday", 3)
    assertEquals(newPos, ~3)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @throws(classOf[Exception])
  def test_parse_noMatch_atEnd(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Monday", 6)
    assertEquals(newPos, ~6)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @DataProvider(name = "parseText") private[format] def provider_text: Array[Array[Any]] = {
    Array[Array[Any]](Array(DAY_OF_WEEK, TextStyle.FULL, 1, "Monday"), Array(DAY_OF_WEEK, TextStyle.FULL, 2, "Tuesday"), Array(DAY_OF_WEEK, TextStyle.FULL, 3, "Wednesday"), Array(DAY_OF_WEEK, TextStyle.FULL, 4, "Thursday"), Array(DAY_OF_WEEK, TextStyle.FULL, 5, "Friday"), Array(DAY_OF_WEEK, TextStyle.FULL, 6, "Saturday"), Array(DAY_OF_WEEK, TextStyle.FULL, 7, "Sunday"), Array(DAY_OF_WEEK, TextStyle.SHORT, 1, "Mon"), Array(DAY_OF_WEEK, TextStyle.SHORT, 2, "Tue"), Array(DAY_OF_WEEK, TextStyle.SHORT, 3, "Wed"), Array(DAY_OF_WEEK, TextStyle.SHORT, 4, "Thu"), Array(DAY_OF_WEEK, TextStyle.SHORT, 5, "Fri"), Array(DAY_OF_WEEK, TextStyle.SHORT, 6, "Sat"), Array(DAY_OF_WEEK, TextStyle.SHORT, 7, "Sun"), Array(MONTH_OF_YEAR, TextStyle.FULL, 1, "January"), Array(MONTH_OF_YEAR, TextStyle.FULL, 12, "December"), Array(MONTH_OF_YEAR, TextStyle.SHORT, 1, "Jan"), Array(MONTH_OF_YEAR, TextStyle.SHORT, 12, "Dec"))
  }

  @DataProvider(name = "parseNumber") private[format] def provider_number: Array[Array[Any]] = {
    Array[Array[Any]](Array(DAY_OF_MONTH, TextStyle.FULL, 1, "1"), Array(DAY_OF_MONTH, TextStyle.FULL, 2, "2"), Array(DAY_OF_MONTH, TextStyle.FULL, 30, "30"), Array(DAY_OF_MONTH, TextStyle.FULL, 31, "31"), Array(DAY_OF_MONTH, TextStyle.SHORT, 1, "1"), Array(DAY_OF_MONTH, TextStyle.SHORT, 2, "2"), Array(DAY_OF_MONTH, TextStyle.SHORT, 30, "30"), Array(DAY_OF_MONTH, TextStyle.SHORT, 31, "31"))
  }

  @Test(dataProvider = "parseText")
  @throws(classOf[Exception])
  def test_parseText(field: TemporalField, style: TextStyle, value: Int, input: String): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, input, 0)
    assertEquals(newPos, input.length)
    assertParsed(parseContext, field, value.toLong)
  }

  @Test(dataProvider = "parseNumber")
  @throws(classOf[Exception])
  def test_parseNumber(field: TemporalField, style: TextStyle, value: Int, input: String): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, input, 0)
    assertEquals(newPos, input.length)
    assertParsed(parseContext, field, value.toLong)
  }

  @Test(dataProvider = "parseText")
  @throws(classOf[Exception])
  def test_parse_strict_caseSensitive_parseUpper(field: TemporalField, style: TextStyle, value: Int, input: String): Unit = {
    parseContext.setCaseSensitive(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, input.toUpperCase, 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @Test(dataProvider = "parseText")
  @throws(classOf[Exception])
  def test_parse_strict_caseInsensitive_parseUpper(field: TemporalField, style: TextStyle, value: Int, input: String): Unit = {
    parseContext.setCaseSensitive(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, input.toUpperCase, 0)
    assertEquals(newPos, input.length)
    assertParsed(parseContext, field, value.toLong)
  }

  @Test(dataProvider = "parseText")
  @throws(classOf[Exception])
  def test_parse_strict_caseSensitive_parseLower(field: TemporalField, style: TextStyle, value: Int, input: String): Unit = {
    parseContext.setCaseSensitive(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, input.toLowerCase, 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @Test(dataProvider = "parseText")
  @throws(classOf[Exception])
  def test_parse_strict_caseInsensitive_parseLower(field: TemporalField, style: TextStyle, value: Int, input: String): Unit = {
    parseContext.setCaseSensitive(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, input.toLowerCase, 0)
    assertEquals(newPos, input.length)
    assertParsed(parseContext, field, value.toLong)
  }

  @throws(classOf[Exception])
  def test_parse_full_strict_full_match(): Unit = {
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "January", 0)
    assertEquals(newPos, 7)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_full_strict_short_noMatch(): Unit = {
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Janua", 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @throws(classOf[Exception])
  def test_parse_full_strict_number_noMatch(): Unit = {
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "1", 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @throws(classOf[Exception])
  def test_parse_short_strict_full_match(): Unit = {
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "January", 0)
    assertEquals(newPos, 3)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_short_strict_short_match(): Unit = {
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Janua", 0)
    assertEquals(newPos, 3)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_short_strict_number_noMatch(): Unit = {
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "1", 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @throws(classOf[Exception])
  def test_parse_french_short_strict_full_noMatch(): Unit = {
    parseContext.setLocale(Locale.FRENCH)
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "janvier", 0)
    assertEquals(newPos, ~0)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @throws(classOf[Exception])
  def test_parse_french_short_strict_short_match(): Unit = {
    parseContext.setLocale(Locale.FRENCH)
    parseContext.setStrict(true)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "janv.", 0)
    assertEquals(newPos, 5)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_full_lenient_full_match(): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "January", 0)
    assertEquals(newPos, 7)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_full_lenient_short_match(): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Janua", 0)
    assertEquals(newPos, 3)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_full_lenient_number_match(): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "1", 0)
    assertEquals(newPos, 1)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_short_lenient_full_match(): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "January", 0)
    assertEquals(newPos, 7)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_short_lenient_short_match(): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "Janua", 0)
    assertEquals(newPos, 3)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  @throws(classOf[Exception])
  def test_parse_short_lenient_number_match(): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextParser.PROVIDER)
    val newPos: Int = pp.parse(parseContext, "1", 0)
    assertEquals(newPos, 1)
    assertParsed(parseContext, MONTH_OF_YEAR, 1L)
  }

  private def assertParsed(context: DateTimeParseContext, field: TemporalField, value: java.lang.Long): Unit = {
    if (value == null) {
      assertEquals(context.getParsed(field), null)
    }
    else {
      assertEquals(context.getParsed(field), value)
    }
  }
}
