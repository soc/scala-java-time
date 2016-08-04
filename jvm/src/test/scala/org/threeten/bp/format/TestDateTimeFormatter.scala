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
import org.testng.Assert.assertNull
import org.testng.Assert.assertTrue
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import java.lang.StringBuilder
import java.io.IOException
import java.text.Format
import java.text.ParseException
import java.text.ParsePosition
import java.util.Locale
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalQuery

/** Test DateTimeFormatter. */
object TestDateTimeFormatter {
  private val BASIC_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("'ONE'd")
  private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("'ONE'uuuu MM dd")
}

@Test class TestDateTimeFormatter {
  private var fmt: DateTimeFormatter = null

  def toTemporalQuery[T](f: TemporalAccessor => T): TemporalQuery[T] =
    new TemporalQuery[T] {
      override def queryFrom(temporal: TemporalAccessor): T = f(temporal)
    }

  @BeforeMethod def setUp(): Unit = {
    fmt = new DateTimeFormatterBuilder().appendLiteral("ONE").appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE).toFormatter
  }

  @Test
  @throws(classOf[Exception])
  def test_withLocale(): Unit = {
    val base: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val test: DateTimeFormatter = base.withLocale(Locale.GERMAN)
    assertEquals(test.getLocale, Locale.GERMAN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_withLocale_null(): Unit = {
    val base: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    base.withLocale(null.asInstanceOf[Locale])
  }

  @Test
  @throws(classOf[Exception])
  def test_print_Calendrical(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val result: String = test.format(LocalDate.of(2008, 6, 30))
    assertEquals(result, "ONE30")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_Calendrical_noSuchField(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.format(LocalTime.of(11, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_print_Calendrical_null(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.format(null.asInstanceOf[TemporalAccessor])
  }

  @Test
  @throws(classOf[Exception])
  def test_print_CalendricalAppendable(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val buf: StringBuilder = new StringBuilder
    test.formatTo(LocalDate.of(2008, 6, 30), buf)
    assertEquals(buf.toString, "ONE30")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_CalendricalAppendable_noSuchField(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val buf: StringBuilder = new StringBuilder
    test.formatTo(LocalTime.of(11, 30), buf)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_print_CalendricalAppendable_nullCalendrical(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val buf: StringBuilder = new StringBuilder
    test.formatTo(null.asInstanceOf[TemporalAccessor], buf)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_print_CalendricalAppendable_nullAppendable(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.formatTo(LocalDate.of(2008, 6, 30), null.asInstanceOf[Appendable])
  }

  @Test(expectedExceptions = Array(classOf[IOException]))
  @throws(classOf[Throwable])
  def test_print_CalendricalAppendable_ioError(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    try {
      test.formatTo(LocalDate.of(2008, 6, 30), new MockIOExceptionAppendable)
    }
    catch {
      case ex: DateTimeException =>
        assertEquals(ex.getCause.isInstanceOf[IOException], true)
        throw ex.getCause
    }
  }

  @Test
  @throws(classOf[Exception])
  def test_parse_Class_String(): Unit = {
    val result: LocalDate = TestDateTimeFormatter.DATE_FORMATTER.parse("ONE2012 07 27", toTemporalQuery(LocalDate.from))
    assertEquals(result, LocalDate.of(2012, 7, 27))
  }

  @Test
  @throws(classOf[Exception])
  def test_parse_Class_CharSequence(): Unit = {
    val result: LocalDate = TestDateTimeFormatter.DATE_FORMATTER.parse(new StringBuilder("ONE2012 07 27"), toTemporalQuery(LocalDate.from))
    assertEquals(result, LocalDate.of(2012, 7, 27))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException]))
  @throws(classOf[Exception])
  def test_parse_Class_String_parseError(): Unit = {
    try {
      TestDateTimeFormatter.DATE_FORMATTER.parse("ONE2012 07 XX", toTemporalQuery(LocalDate.from))
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("could not be parsed"), true)
        assertEquals(ex.getMessage.contains("ONE2012 07 XX"), true)
        assertEquals(ex.getParsedString, "ONE2012 07 XX")
        assertEquals(ex.getErrorIndex, 11)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException]))
  @throws(classOf[Exception])
  def test_parse_Class_String_parseErrorLongText(): Unit = {
    try {
      TestDateTimeFormatter.DATE_FORMATTER.parse("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789", toTemporalQuery(LocalDate.from))
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("could not be parsed"), true)
        assertEquals(ex.getMessage.contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true)
        assertEquals(ex.getParsedString, "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789")
        assertEquals(ex.getErrorIndex, 3)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException]))
  @throws(classOf[Exception])
  def test_parse_Class_String_parseIncomplete(): Unit = {
    try {
      TestDateTimeFormatter.DATE_FORMATTER.parse("ONE2012 07 27SomethingElse", toTemporalQuery(LocalDate.from))
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("could not be parsed"), true)
        assertEquals(ex.getMessage.contains("ONE2012 07 27SomethingElse"), true)
        assertEquals(ex.getParsedString, "ONE2012 07 27SomethingElse")
        assertEquals(ex.getErrorIndex, 13)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_parse_Class_String_nullText(): Unit = {
    TestDateTimeFormatter.DATE_FORMATTER.parse(null.asInstanceOf[String], toTemporalQuery(LocalDate.from))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_parse_Class_String_nullRule(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.parse("30", null.asInstanceOf[TemporalQuery[Any]])
  }

  @Test
  @throws(classOf[Exception])
  def test_parseBest_firstOption(): Unit = {
    val test: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM[-dd]")
    val result: TemporalAccessor = test.parseBest("2011-06-30", toTemporalQuery(LocalDate.from), toTemporalQuery(YearMonth.from))
    assertEquals(result, LocalDate.of(2011, 6, 30))
  }

  @Test
  @throws(classOf[Exception])
  def test_parseBest_secondOption(): Unit = {
    val test: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM[-dd]")
    val result: TemporalAccessor = test.parseBest("2011-06", toTemporalQuery(LocalDate.from), toTemporalQuery(YearMonth.from))
    assertEquals(result, YearMonth.of(2011, 6))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException]))
  @throws(classOf[Exception])
  def test_parseBest_String_parseError(): Unit = {
    val test: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM[-dd]")
    try test.parseBest("2011-XX-30", toTemporalQuery(LocalDate.from), toTemporalQuery(YearMonth.from))
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("could not be parsed"), true)
        assertEquals(ex.getMessage.contains("XX"), true)
        assertEquals(ex.getParsedString, "2011-XX-30")
        assertEquals(ex.getErrorIndex, 5)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException]))
  @throws(classOf[Exception])
  def test_parseBest_String_parseErrorLongText(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    try test.parseBest("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789", toTemporalQuery(LocalDate.from), toTemporalQuery(YearMonth.from))
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("could not be parsed"), true)
        assertEquals(ex.getMessage.contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true)
        assertEquals(ex.getParsedString, "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789")
        assertEquals(ex.getErrorIndex, 3)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException]))
  @throws(classOf[Exception])
  def test_parseBest_String_parseIncomplete(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    try test.parseBest("ONE30SomethingElse", toTemporalQuery(YearMonth.from), toTemporalQuery(LocalDate.from))
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("could not be parsed"), true)
        assertEquals(ex.getMessage.contains("ONE30SomethingElse"), true)
        assertEquals(ex.getParsedString, "ONE30SomethingElse")
        assertEquals(ex.getErrorIndex, 5)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_parseBest_String_nullText(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.parseBest(null.asInstanceOf[String], toTemporalQuery(YearMonth.from), toTemporalQuery(LocalDate.from))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_parseBest_String_nullRules(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.parseBest("30", null.asInstanceOf[Array[TemporalQuery[Any]]]: _*)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_parseBest_String_zeroRules(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.parseBest("30", new Array[TemporalQuery[Any]](0): _*)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_parseBest_String_oneRule(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.parseBest("30", toTemporalQuery(LocalDate.from))
  }

  @Test
  @throws(classOf[Exception])
  def test_parseToBuilder_StringParsePosition(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val pos: ParsePosition = new ParsePosition(0)
    val result: TemporalAccessor = test.parseUnresolved("ONE30XXX", pos)
    assertEquals(pos.getIndex, 5)
    assertEquals(pos.getErrorIndex, -1)
    assertEquals(result.getLong(DAY_OF_MONTH), 30L)
  }

  @Test
  @throws(classOf[Exception])
  def test_parseToBuilder_StringParsePosition_parseError(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val pos: ParsePosition = new ParsePosition(0)
    val result: TemporalAccessor = test.parseUnresolved("ONEXXX", pos)
    assertEquals(pos.getIndex, 0)
    assertEquals(pos.getErrorIndex, 3)
    assertEquals(result, null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_parseToBuilder_StringParsePosition_nullString(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val pos: ParsePosition = new ParsePosition(0)
    test.parseUnresolved(null.asInstanceOf[String], pos)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_parseToBuilder_StringParsePosition_nullParsePosition(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    test.parseUnresolved("ONE30", null.asInstanceOf[ParsePosition])
  }

  @Test(expectedExceptions = Array(classOf[IndexOutOfBoundsException]))
  @throws(classOf[Exception])
  def test_parseToBuilder_StringParsePosition_invalidPosition(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val pos: ParsePosition = new ParsePosition(6)
    test.parseUnresolved("ONE30", pos)
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_format(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    val result: String = format.format(LocalDate.of(2008, 6, 30))
    assertEquals(result, "ONE30")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_toFormat_format_null(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    format.format(null)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_toFormat_format_notCalendrical(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    format.format("Not a Calendrical")
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_parseObject_String(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    val result: DateTimeBuilder = format.parseObject("ONE30").asInstanceOf[DateTimeBuilder]
    assertEquals(result.getLong(DAY_OF_MONTH), 30L)
  }

  @Test(expectedExceptions = Array(classOf[ParseException]))
  @throws(classOf[Exception])
  def test_toFormat_parseObject_String_parseError(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    try format.parseObject("ONEXXX")
    catch {
      case ex: ParseException =>
        assertEquals(ex.getMessage.contains("ONEXXX"), true)
        assertEquals(ex.getErrorOffset, 3)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[ParseException]))
  @throws(classOf[Exception])
  def test_toFormat_parseObject_String_parseErrorLongText(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    try format.parseObject("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789")
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getMessage.contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true)
        assertEquals(ex.getParsedString, "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789")
        assertEquals(ex.getErrorIndex, 3)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_toFormat_parseObject_String_null(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    format.parseObject(null.asInstanceOf[String])
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    val pos: ParsePosition = new ParsePosition(0)
    val result: DateTimeBuilder = format.parseObject("ONE30XXX", pos).asInstanceOf[DateTimeBuilder]
    assertEquals(pos.getIndex, 5)
    assertEquals(pos.getErrorIndex, -1)
    assertEquals(result.getLong(DAY_OF_MONTH), 30L)
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition_parseError(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    val pos: ParsePosition = new ParsePosition(0)
    val result: TemporalAccessor = format.parseObject("ONEXXX", pos).asInstanceOf[TemporalAccessor]
    assertEquals(pos.getIndex, 0)
    assertEquals(pos.getErrorIndex, 3)
    assertEquals(result, null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition_nullString(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    val pos: ParsePosition = new ParsePosition(0)
    format.parseObject(null.asInstanceOf[String], pos)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition_nullParsePosition(): Unit = {
    val test: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val format: Format = test.toFormat
    format.parseObject("ONE30", null.asInstanceOf[ParsePosition])
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition_invalidPosition_tooBig(): Unit = {
    val dtf: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val pos: ParsePosition = new ParsePosition(6)
    val test: Format = dtf.toFormat
    assertNull(test.parseObject("ONE30", pos))
    assertTrue(pos.getErrorIndex >= 0)
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition_invalidPosition_tooSmall(): Unit = {
    val dtf: DateTimeFormatter = fmt.withLocale(Locale.ENGLISH).withDecimalStyle(DecimalStyle.STANDARD)
    val pos: ParsePosition = new ParsePosition(-1)
    val test: Format = dtf.toFormat
    assertNull(test.parseObject("ONE30", pos))
    assertTrue(pos.getErrorIndex >= 0)
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_Class_format(): Unit = {
    val format: Format = TestDateTimeFormatter.BASIC_FORMATTER.toFormat
    val result: String = format.format(LocalDate.of(2008, 6, 30))
    assertEquals(result, "ONE30")
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormat_Class_parseObject_String(): Unit = {
    val format: Format = TestDateTimeFormatter.DATE_FORMATTER.toFormat(toTemporalQuery(LocalDate.from))
    val result: LocalDate = format.parseObject("ONE2012 07 27").asInstanceOf[LocalDate]
    assertEquals(result, LocalDate.of(2012, 7, 27))
  }

  @Test(expectedExceptions = Array(classOf[ParseException]))
  @throws(classOf[Exception])
  def test_toFormat_parseObject_StringParsePosition_dateTimeError(): Unit = {
    val format: Format = TestDateTimeFormatter.DATE_FORMATTER.toFormat(toTemporalQuery(LocalDate.from))
    format.parseObject("ONE2012 07 32")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_toFormat_Class(): Unit = {
    TestDateTimeFormatter.BASIC_FORMATTER.toFormat(null)
  }

  @throws(classOf[Exception])
  def test_parse_allZones(): Unit = {
    import scala.collection.JavaConversions._
    for (zoneStr <- ZoneId.getAvailableZoneIds) {
      val zone: ZoneId = ZoneId.of(zoneStr)
      val base: ZonedDateTime = ZonedDateTime.of(2014, 12, 31, 12, 0, 0, 0, zone)
      val test: ZonedDateTime = ZonedDateTime.parse(base.toString)
      assertEquals(test, base)
    }
  }
}
