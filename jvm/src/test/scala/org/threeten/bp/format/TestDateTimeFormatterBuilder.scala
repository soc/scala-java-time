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
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import org.threeten.bp.temporal.ChronoField.YEAR
import java.text.ParsePosition
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.temporal.TemporalAccessor

/** Test DateTimeFormatterBuilder. */
@Test class TestDateTimeFormatterBuilder {
  private var builder: DateTimeFormatterBuilder = null

  @BeforeMethod def setUp(): Unit = {
    builder = new DateTimeFormatterBuilder
  }

  @Test
  @throws(classOf[Exception])
  def test_toFormatter_empty(): Unit = {
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "")
  }

  @Test
  @throws(classOf[Exception])
  def test_parseCaseSensitive(): Unit = {
    builder.parseCaseSensitive
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ParseCaseSensitive(true)")
  }

  @Test
  @throws(classOf[Exception])
  def test_parseCaseInsensitive(): Unit = {
    builder.parseCaseInsensitive
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ParseCaseSensitive(false)")
  }

  @Test
  @throws(classOf[Exception])
  def test_parseStrict(): Unit = {
    builder.parseStrict
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ParseStrict(true)")
  }

  @Test
  @throws(classOf[Exception])
  def test_parseLenient(): Unit = {
    builder.parseLenient
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ParseStrict(false)")
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_1arg(): Unit = {
    builder.appendValue(DAY_OF_MONTH)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(DayOfMonth)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendValue_1arg_null(): Unit = {
    builder.appendValue(null)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_2arg(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 3)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(DayOfMonth,3)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendValue_2arg_null(): Unit = {
    builder.appendValue(null, 3)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_2arg_widthTooSmall(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 0)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_2arg_widthTooBig(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 20)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_3arg(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 2, 3, SignStyle.NORMAL)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(DayOfMonth,2,3,NORMAL)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_nullField(): Unit = {
    builder.appendValue(null, 2, 3, SignStyle.NORMAL)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_minWidthTooSmall(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 0, 2, SignStyle.NORMAL)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_minWidthTooBig(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 20, 2, SignStyle.NORMAL)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_maxWidthTooSmall(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 2, 0, SignStyle.NORMAL)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_maxWidthTooBig(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 2, 20, SignStyle.NORMAL)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_maxWidthMinWidth(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 4, 2, SignStyle.NORMAL)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendValue_3arg_nullSignStyle(): Unit = {
    builder.appendValue(DAY_OF_MONTH, 2, 3, null)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_subsequent2_parse3(): Unit = {
    builder.appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL).appendValue(DAY_OF_MONTH, 2)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear,1,2,NORMAL)Value(DayOfMonth,2)")
    val cal: TemporalAccessor = f.parseUnresolved("123", new ParsePosition(0))
    assertEquals(cal.get(MONTH_OF_YEAR), 1)
    assertEquals(cal.get(DAY_OF_MONTH), 23)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_subsequent2_parse4(): Unit = {
    builder.appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL).appendValue(DAY_OF_MONTH, 2)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear,1,2,NORMAL)Value(DayOfMonth,2)")
    val cal: TemporalAccessor = f.parseUnresolved("0123", new ParsePosition(0))
    assertEquals(cal.get(MONTH_OF_YEAR), 1)
    assertEquals(cal.get(DAY_OF_MONTH), 23)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_subsequent2_parse5(): Unit = {
    builder.appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL).appendValue(DAY_OF_MONTH, 2).appendLiteral('4')
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear,1,2,NORMAL)Value(DayOfMonth,2)'4'")
    val cal: TemporalAccessor = f.parseUnresolved("01234", new ParsePosition(0))
    assertEquals(cal.get(MONTH_OF_YEAR), 1)
    assertEquals(cal.get(DAY_OF_MONTH), 23)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValue_subsequent3_parse6(): Unit = {
    builder.appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(Year,4,10,EXCEEDS_PAD)Value(MonthOfYear,2)Value(DayOfMonth,2)")
    val cal: TemporalAccessor = f.parseUnresolved("20090630", new ParsePosition(0))
    assertEquals(cal.get(YEAR), 2009)
    assertEquals(cal.get(MONTH_OF_YEAR), 6)
    assertEquals(cal.get(DAY_OF_MONTH), 30)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendValueReduced_null(): Unit = {
    builder.appendValueReduced(null, 2, 2, 2000)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValueReduced(): Unit = {
    builder.appendValueReduced(YEAR, 2, 2, 2000)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ReducedValue(Year,2,2,2000)")
    val cal: TemporalAccessor = f.parseUnresolved("12", new ParsePosition(0))
    assertEquals(cal.get(YEAR), 2012)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendValueReduced_subsequent_parse(): Unit = {
    builder.appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL).appendValueReduced(YEAR, 2, 2, 2000)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear,1,2,NORMAL)ReducedValue(Year,2,2,2000)")
    val cal: TemporalAccessor = f.parseUnresolved("123", new ParsePosition(0))
    assertEquals(cal.get(MONTH_OF_YEAR), 1)
    assertEquals(cal.get(YEAR), 2023)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendFraction_4arg(): Unit = {
    builder.appendFraction(MINUTE_OF_HOUR, 1, 9, false)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Fraction(MinuteOfHour,1,9)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_nullRule(): Unit = {
    builder.appendFraction(null, 1, 9, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_invalidRuleNotFixedSet(): Unit = {
    builder.appendFraction(DAY_OF_MONTH, 1, 9, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_minTooSmall(): Unit = {
    builder.appendFraction(MINUTE_OF_HOUR, -1, 9, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_minTooBig(): Unit = {
    builder.appendFraction(MINUTE_OF_HOUR, 10, 9, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_maxTooSmall(): Unit = {
    builder.appendFraction(MINUTE_OF_HOUR, 0, -1, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_maxTooBig(): Unit = {
    builder.appendFraction(MINUTE_OF_HOUR, 1, 10, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendFraction_4arg_maxWidthMinWidth(): Unit = {
    builder.appendFraction(MINUTE_OF_HOUR, 9, 3, false)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendText_1arg(): Unit = {
    builder.appendText(MONTH_OF_YEAR)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Text(MonthOfYear)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendText_1arg_null(): Unit = {
    builder.appendText(null)
  }

  @Test
  @throws(classOf[Exception])
  def test_appendText_2arg(): Unit = {
    builder.appendText(MONTH_OF_YEAR, TextStyle.SHORT)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Text(MonthOfYear,SHORT)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendText_2arg_nullRule(): Unit = {
    builder.appendText(null, TextStyle.SHORT)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendText_2arg_nullStyle(): Unit = {
    builder.appendText(MONTH_OF_YEAR, null.asInstanceOf[TextStyle])
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
    assertEquals(f.toString, "Text(MonthOfYear)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendTextMap_nullRule(): Unit = {
    builder.appendText(null, new java.util.HashMap[Long, String])
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendTextMap_nullStyle(): Unit = {
    builder.appendText(MONTH_OF_YEAR, null.asInstanceOf[java.util.Map[Long, String]])
  }

  @Test
  @throws(classOf[Exception])
  def test_appendOffsetId(): Unit = {
    builder.appendOffsetId
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Offset(+HH:MM:ss,'Z')")
  }

  @DataProvider(name = "offsetPatterns") private[format] def data_offsetPatterns: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("+HH"), Array("+HHMM"), Array("+HH:MM"), Array("+HHMMss"), Array("+HH:MM:ss"), Array("+HHMMSS"), Array("+HH:MM:SS"))
  }

  @Test(dataProvider = "offsetPatterns")
  @throws(classOf[Exception])
  def test_appendOffset(pattern: String): Unit = {
    builder.appendOffset(pattern, "Z")
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Offset(" + pattern + ",'Z')")
  }

  @DataProvider(name = "badOffsetPatterns") private[format] def data_badOffsetPatterns: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("HH"), Array("HHMM"), Array("HH:MM"), Array("HHMMss"), Array("HH:MM:ss"), Array("HHMMSS"), Array("HH:MM:SS"), Array("+H"), Array("+HMM"), Array("+HHM"), Array("+A"))
  }

  @Test(dataProvider = "badOffsetPatterns", expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendOffset_badPattern(pattern: String): Unit = {
    builder.appendOffset(pattern, "Z")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendOffset_3arg_nullText(): Unit = {
    builder.appendOffset("+HH:MM", null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendOffset_3arg_nullPattern(): Unit = {
    builder.appendOffset(null, "Z")
  }

  @Test
  @throws(classOf[Exception])
  def test_appendZoneId(): Unit = {
    builder.appendZoneId
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ZoneId()")
  }

  @Test
  @throws(classOf[Exception])
  def test_appendZoneText_1arg(): Unit = {
    builder.appendZoneText(TextStyle.FULL)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "ZoneText(FULL)")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Exception])
  def test_appendZoneText_1arg_nullText(): Unit = {
    builder.appendZoneText(null)
  }

  @Test
  @throws(classOf[Exception])
  def test_padNext_1arg(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).padNext(2).appendValue(DAY_OF_MONTH).appendValue(DAY_OF_WEEK)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)Pad(Value(DayOfMonth),2)Value(DayOfWeek)")
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_padNext_1arg_invalidWidth(): Unit = {
    builder.padNext(0)
  }

  @Test
  @throws(classOf[Exception])
  def test_padNext_2arg_dash(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).padNext(2, '-').appendValue(DAY_OF_MONTH).appendValue(DAY_OF_WEEK)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)Pad(Value(DayOfMonth),2,'-')Value(DayOfWeek)")
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_padNext_2arg_invalidWidth(): Unit = {
    builder.padNext(0, '-')
  }

  @Test
  @throws(classOf[Exception])
  def test_padOptional(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).padNext(5).optionalStart.appendValue(DAY_OF_MONTH).optionalEnd.appendValue(DAY_OF_WEEK)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)Pad([Value(DayOfMonth)],5)Value(DayOfWeek)")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalStart_noEnd(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.appendValue(DAY_OF_MONTH).appendValue(DAY_OF_WEEK)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[Value(DayOfMonth)Value(DayOfWeek)]")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalStart2_noEnd(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.appendValue(DAY_OF_MONTH).optionalStart.appendValue(DAY_OF_WEEK)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[Value(DayOfMonth)[Value(DayOfWeek)]]")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalStart_doubleStart(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.optionalStart.appendValue(DAY_OF_MONTH)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[[Value(DayOfMonth)]]")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalEnd(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.appendValue(DAY_OF_MONTH).optionalEnd.appendValue(DAY_OF_WEEK)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[Value(DayOfMonth)]Value(DayOfWeek)")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalEnd2(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.appendValue(DAY_OF_MONTH).optionalStart.appendValue(DAY_OF_WEEK).optionalEnd.appendValue(DAY_OF_MONTH).optionalEnd
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[Value(DayOfMonth)[Value(DayOfWeek)]Value(DayOfMonth)]")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalEnd_doubleStartSingleEnd(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.optionalStart.appendValue(DAY_OF_MONTH).optionalEnd
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[[Value(DayOfMonth)]]")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalEnd_doubleStartDoubleEnd(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.optionalStart.appendValue(DAY_OF_MONTH).optionalEnd.optionalEnd
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)[[Value(DayOfMonth)]]")
  }

  @Test
  @throws(classOf[Exception])
  def test_optionalStartEnd_immediateStartEnd(): Unit = {
    builder.appendValue(MONTH_OF_YEAR).optionalStart.optionalEnd.appendValue(DAY_OF_MONTH)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, "Value(MonthOfYear)Value(DayOfMonth)")
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException]))
  @throws(classOf[Exception])
  def test_optionalEnd_noStart(): Unit = {
    builder.optionalEnd
  }

  @DataProvider(name = "validPatterns") private[format] def dataValid: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("'a'", "'a'"), Array("''", "''"), Array("'!'", "'!'"), Array("!", "'!'"), Array("'hello_people,][)('", "'hello_people,][)('"), Array("'hi'", "'hi'"), Array("'yyyy'", "'yyyy'"), Array("''''", "''"), Array("'o''clock'", "'o''clock'"), Array("G", "Text(Era,SHORT)"), Array("GG", "Text(Era,SHORT)"), Array("GGG", "Text(Era,SHORT)"), Array("GGGG", "Text(Era)"), Array("GGGGG", "Text(Era,NARROW)"), Array("u", "Value(Year)"), Array("uu", "ReducedValue(Year,2,2,2000-01-01)"), Array("uuu", "Value(Year,3,19,NORMAL)"), Array("uuuu", "Value(Year,4,19,EXCEEDS_PAD)"), Array("uuuuu", "Value(Year,5,19,EXCEEDS_PAD)"), Array("y", "Value(YearOfEra)"), Array("yy", "ReducedValue(YearOfEra,2,2,2000-01-01)"), Array("yyy", "Value(YearOfEra,3,19,NORMAL)"), Array("yyyy", "Value(YearOfEra,4,19,EXCEEDS_PAD)"), Array("yyyyy", "Value(YearOfEra,5,19,EXCEEDS_PAD)"), Array("M", "Value(MonthOfYear)"), Array("MM", "Value(MonthOfYear,2)"), Array("MMM", "Text(MonthOfYear,SHORT)"), Array("MMMM", "Text(MonthOfYear)"), Array("MMMMM", "Text(MonthOfYear,NARROW)"), Array("D", "Value(DayOfYear)"), Array("DD", "Value(DayOfYear,2)"), Array("DDD", "Value(DayOfYear,3)"), Array("d", "Value(DayOfMonth)"), Array("dd", "Value(DayOfMonth,2)"), Array("F", "Value(AlignedDayOfWeekInMonth)"), Array("E", "Text(DayOfWeek,SHORT)"), Array("EE", "Text(DayOfWeek,SHORT)"), Array("EEE", "Text(DayOfWeek,SHORT)"), Array("EEEE", "Text(DayOfWeek)"), Array("EEEEE", "Text(DayOfWeek,NARROW)"), Array("a", "Text(AmPmOfDay,SHORT)"), Array("H", "Value(HourOfDay)"), Array("HH", "Value(HourOfDay,2)"), Array("K", "Value(HourOfAmPm)"), Array("KK", "Value(HourOfAmPm,2)"), Array("k", "Value(ClockHourOfDay)"), Array("kk", "Value(ClockHourOfDay,2)"), Array("h", "Value(ClockHourOfAmPm)"), Array("hh", "Value(ClockHourOfAmPm,2)"), Array("m", "Value(MinuteOfHour)"), Array("mm", "Value(MinuteOfHour,2)"), Array("s", "Value(SecondOfMinute)"), Array("ss", "Value(SecondOfMinute,2)"), Array("S", "Fraction(NanoOfSecond,1,1)"), Array("SS", "Fraction(NanoOfSecond,2,2)"), Array("SSS", "Fraction(NanoOfSecond,3,3)"), Array("SSSSSSSSS", "Fraction(NanoOfSecond,9,9)"), Array("A", "Value(MilliOfDay)"), Array("AA", "Value(MilliOfDay,2)"), Array("AAA", "Value(MilliOfDay,3)"), Array("n", "Value(NanoOfSecond)"), Array("nn", "Value(NanoOfSecond,2)"), Array("nnn", "Value(NanoOfSecond,3)"), Array("N", "Value(NanoOfDay)"), Array("NN", "Value(NanoOfDay,2)"), Array("NNN", "Value(NanoOfDay,3)"), Array("z", "ZoneText(SHORT)"), Array("zz", "ZoneText(SHORT)"), Array("zzz", "ZoneText(SHORT)"), Array("zzzz", "ZoneText(FULL)"), Array("VV", "ZoneId()"), Array("Z", "Offset(+HHMM,'+0000')"), Array("ZZ", "Offset(+HHMM,'+0000')"), Array("ZZZ", "Offset(+HHMM,'+0000')"), Array("X", "Offset(+HHmm,'Z')"), Array("XX", "Offset(+HHMM,'Z')"), Array("XXX", "Offset(+HH:MM,'Z')"), Array("XXXX", "Offset(+HHMMss,'Z')"), Array("XXXXX", "Offset(+HH:MM:ss,'Z')"), Array("x", "Offset(+HHmm,'+00')"), Array("xx", "Offset(+HHMM,'+0000')"), Array("xxx", "Offset(+HH:MM,'+00:00')"), Array("xxxx", "Offset(+HHMMss,'+0000')"), Array("xxxxx", "Offset(+HH:MM:ss,'+00:00')"), Array("ppH", "Pad(Value(HourOfDay),2)"), Array("pppDD", "Pad(Value(DayOfYear,2),3)"), Array("uuuu[-MM[-dd", "Value(Year,4,19,EXCEEDS_PAD)['-'Value(MonthOfYear,2)['-'Value(DayOfMonth,2)]]"), Array("uuuu[-MM[-dd]]", "Value(Year,4,19,EXCEEDS_PAD)['-'Value(MonthOfYear,2)['-'Value(DayOfMonth,2)]]"), Array("uuuu[-MM[]-dd]", "Value(Year,4,19,EXCEEDS_PAD)['-'Value(MonthOfYear,2)'-'Value(DayOfMonth,2)]"), Array("uuuu-MM-dd'T'HH:mm:ss.SSS", "Value(Year,4,19,EXCEEDS_PAD)'-'Value(MonthOfYear,2)'-'Value(DayOfMonth,2)" + "'T'Value(HourOfDay,2)':'Value(MinuteOfHour,2)':'Value(SecondOfMinute,2)'.'Fraction(NanoOfSecond,3,3)"))
  }

  @Test(dataProvider = "validPatterns")
  @throws(classOf[Exception])
  def test_appendPattern_valid(input: String, expected: String): Unit = {
    builder.appendPattern(input)
    val f: DateTimeFormatter = builder.toFormatter
    assertEquals(f.toString, expected)
  }

  @DataProvider(name = "invalidPatterns") private[format] def dataInvalid: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("'"), Array("'hello"), Array("'hel''lo"), Array("'hello''"), Array("]"), Array("{"), Array("}"), Array("#"), Array("yyyy]"), Array("yyyy]MM"), Array("yyyy[MM]]"), Array("MMMMMM"), Array("QQQQQQ"), Array("EEEEEE"), Array("aaaaaa"), Array("XXXXXX"), Array("RO"), Array("p"), Array("pp"), Array("p:"), Array("f"), Array("ff"), Array("f:"), Array("fy"), Array("fa"), Array("fM"), Array("ddd"), Array("FF"), Array("FFF"), Array("aa"), Array("aaa"), Array("aaaa"), Array("aaaaa"), Array("HHH"), Array("KKK"), Array("kkk"), Array("hhh"), Array("mmm"), Array("sss"))
  }

  @Test(dataProvider = "invalidPatterns", expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws(classOf[Exception])
  def test_appendPattern_invalid(input: String): Unit = {
    try {
      builder.appendPattern(input)
    }
    catch {
      case ex: IllegalArgumentException =>
        throw ex
    }
  }
}
