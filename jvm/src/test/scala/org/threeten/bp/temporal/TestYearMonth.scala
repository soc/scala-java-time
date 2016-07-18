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
package org.threeten.bp.temporal

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import org.threeten.bp.temporal.ChronoField.PROLEPTIC_MONTH
import org.threeten.bp.temporal.ChronoField.YEAR
import org.threeten.bp.temporal.ChronoField.YEAR_OF_ERA
import java.io.IOException
import java.util.Arrays
import org.threeten.bp._
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

/** Test YearMonth. */
@Test class TestYearMonth extends AbstractDateTimeTest {
  private var TEST_2008_06: YearMonth = null

  @BeforeMethod def setUp(): Unit = {
    TEST_2008_06 = YearMonth.of(2008, 6)
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_2008_06)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR_OF_ERA, YEAR, ERA)
    Arrays.asList(array: _*)
  }

  protected def invalidFields: java.util.List[TemporalField] = {
    val list: java.util.List[TemporalField] = new java.util.ArrayList[TemporalField](Arrays.asList[TemporalField](ChronoField.values: _*))
    list.removeAll(validFields)
    list.add(JulianFields.JULIAN_DAY)
    list.add(JulianFields.MODIFIED_JULIAN_DAY)
    list.add(JulianFields.RATA_DIE)
    list
  }

  @Test def test_immutable(): Unit = {
    throw new SkipException("private constructor shows up public due to companion object")
    AbstractTest.assertImmutable(classOf[YearMonth])
  }

  @Test
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(TEST_2008_06)
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization_format(): Unit = {
    AbstractTest.assertEqualsSerialisedForm(YearMonth.of(2012, 9))
  }

  private def check(test: YearMonth, y: Int, m: Int): Unit = {
    assertEquals(test.getYear, y)
    assertEquals(test.getMonth.getValue, m)
  }

  @Test def now(): Unit = {
    var expected: YearMonth = YearMonth.now(Clock.systemDefaultZone)
    var test: YearMonth = YearMonth.now
    var i: Int = 0
    while (i < 100) {
      if (expected == test) {
        return
      }
      expected = YearMonth.now(Clock.systemDefaultZone)
      test = YearMonth.now
      i += 1
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_ZoneId_nullZoneId(): Unit = {
    YearMonth.now(null.asInstanceOf[ZoneId])
  }

  @Test def now_ZoneId(): Unit = {
    val zone: ZoneId = ZoneId.of("UTC+01:02:03")
    var expected: YearMonth = YearMonth.now(Clock.system(zone))
    var test: YearMonth = YearMonth.now(zone)

    {
      var i: Int = 0
      while (i < 100) {
        {
          if (expected == test) {
            return
          }
          expected = YearMonth.now(Clock.system(zone))
          test = YearMonth.now(zone)
        }
        {
          i += 1
          i - 1
        }
      }
    }
    assertEquals(test, expected)
  }

  @Test def now_Clock(): Unit = {
    val instant: Instant = LocalDateTime.of(2010, 12, 31, 0, 0).toInstant(ZoneOffset.UTC)
    val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
    val test: YearMonth = YearMonth.now(clock)
    assertEquals(test.getYear, 2010)
    assertEquals(test.getMonth, Month.DECEMBER)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    YearMonth.now(null.asInstanceOf[Clock])
  }

  @Test def factory_intsMonth(): Unit = {
    val test: YearMonth = YearMonth.of(2008, Month.FEBRUARY)
    check(test, 2008, 2)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_intsMonth_yearTooLow(): Unit = {
    YearMonth.of(Year.MIN_VALUE - 1, Month.JANUARY)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_intsMonth_dayTooHigh(): Unit = {
    YearMonth.of(Year.MAX_VALUE + 1, Month.JANUARY)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_intsMonth_nullMonth(): Unit = {
    YearMonth.of(2008, null)
  }

  @Test def factory_ints(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 2)
    check(test, 2008, 2)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_yearTooLow(): Unit = {
    YearMonth.of(Year.MIN_VALUE - 1, 2)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_dayTooHigh(): Unit = {
    YearMonth.of(Year.MAX_VALUE + 1, 2)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_monthTooLow(): Unit = {
    YearMonth.of(2008, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_monthTooHigh(): Unit = {
    YearMonth.of(2008, 13)
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(YearMonth.from(LocalDate.of(2007, 7, 15)), YearMonth.of(2007, 7))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    YearMonth.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_CalendricalObject_null(): Unit = {
    YearMonth.from(null.asInstanceOf[TemporalAccessor])
  }

  @DataProvider(name = "goodParseData") private[temporal] def provider_goodParseData: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("0000-01", YearMonth.of(0, 1)), Array("0000-12", YearMonth.of(0, 12)), Array("9999-12", YearMonth.of(9999, 12)), Array("2000-01", YearMonth.of(2000, 1)), Array("2000-02", YearMonth.of(2000, 2)), Array("2000-03", YearMonth.of(2000, 3)), Array("2000-04", YearMonth.of(2000, 4)), Array("2000-05", YearMonth.of(2000, 5)), Array("2000-06", YearMonth.of(2000, 6)), Array("2000-07", YearMonth.of(2000, 7)), Array("2000-08", YearMonth.of(2000, 8)), Array("2000-09", YearMonth.of(2000, 9)), Array("2000-10", YearMonth.of(2000, 10)), Array("2000-11", YearMonth.of(2000, 11)), Array("2000-12", YearMonth.of(2000, 12)), Array("+12345678-03", YearMonth.of(12345678, 3)), Array("+123456-03", YearMonth.of(123456, 3)), Array("0000-03", YearMonth.of(0, 3)), Array("-1234-03", YearMonth.of(-1234, 3)), Array("-12345678-03", YearMonth.of(-12345678, 3)), Array("+" + Year.MAX_VALUE + "-03", YearMonth.of(Year.MAX_VALUE, 3)), Array(Year.MIN_VALUE + "-03", YearMonth.of(Year.MIN_VALUE, 3)))
  }

  @Test(dataProvider = "goodParseData") def factory_parse_success(text: String, expected: YearMonth): Unit = {
    val yearMonth: YearMonth = YearMonth.parse(text)
    assertEquals(yearMonth, expected)
  }

  @DataProvider(name = "badParseData") private[temporal] def provider_badParseData: Array[Array[Any]] = {
    Array[Array[Any]](Array("", 0), Array("-00", 1), Array("--01-0", 1), Array("A01-3", 0), Array("200-01", 0), Array("2009/12", 4), Array("-0000-10", 0), Array("-12345678901-10", 11), Array("+1-10", 1), Array("+12-10", 1), Array("+123-10", 1), Array("+1234-10", 0), Array("12345-10", 0), Array("+12345678901-10", 11))
  }

  @Test(dataProvider = "badParseData", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_fail(text: String, pos: Int): Unit = {
    try {
      YearMonth.parse(text)
      fail(f"Parse should have failed for $text%s at position $pos%d")
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getParsedString, text)
        assertEquals(ex.getErrorIndex, pos)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalValue_Month(): Unit = {
    YearMonth.parse("2008-13")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    YearMonth.parse(null)
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M")
    val test: YearMonth = YearMonth.parse("2010 12", f)
    assertEquals(test, YearMonth.of(2010, 12))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M")
    YearMonth.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    YearMonth.parse("ANY", null)
  }

  @Test def test_get_TemporalField(): Unit = {
    assertEquals(TEST_2008_06.get(YEAR), 2008)
    assertEquals(TEST_2008_06.get(MONTH_OF_YEAR), 6)
    assertEquals(TEST_2008_06.get(YEAR_OF_ERA), 2008)
    assertEquals(TEST_2008_06.get(ERA), 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_tooBig(): Unit = {
    TEST_2008_06.get(PROLEPTIC_MONTH)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_get_TemporalField_null(): Unit = {
    TEST_2008_06.get(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_invalidField(): Unit = {
    TEST_2008_06.get(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_timeField(): Unit = {
    TEST_2008_06.get(ChronoField.AMPM_OF_DAY)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    assertEquals(TEST_2008_06.getLong(YEAR), 2008)
    assertEquals(TEST_2008_06.getLong(MONTH_OF_YEAR), 6)
    assertEquals(TEST_2008_06.getLong(YEAR_OF_ERA), 2008)
    assertEquals(TEST_2008_06.getLong(ERA), 1)
    assertEquals(TEST_2008_06.getLong(PROLEPTIC_MONTH), 2008 * 12 + 6 - 1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getLong_TemporalField_null(): Unit = {
    TEST_2008_06.getLong(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_getLong_TemporalField_invalidField(): Unit = {
    TEST_2008_06.getLong(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_getLong_TemporalField_timeField(): Unit = {
    TEST_2008_06.getLong(ChronoField.AMPM_OF_DAY)
  }

  @DataProvider(name = "sampleDates") private[temporal] def provider_sampleDates: Array[Array[Any]] =
    Array[Array[Any]](Array(2008, 1), Array(2008, 2), Array(-1, 3), Array(0, 12))

  @Test def test_with_Year(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.`with`(Year.of(2000)), YearMonth.of(2000, 6))
  }

  @Test def test_with_Year_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.`with`(Year.of(2008)), test)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_Year_null(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.`with`(null.asInstanceOf[Year])
  }

  @Test def test_with_Month(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.`with`(Month.JANUARY), YearMonth.of(2008, 1))
  }

  @Test def test_with_Month_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.`with`(Month.JUNE), test)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_Month_null(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.`with`(null.asInstanceOf[Month])
  }

  @Test def test_withYear(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.withYear(1999), YearMonth.of(1999, 6))
  }

  @Test def test_withYear_int_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.withYear(2008), test)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withYear_tooLow(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.withYear(Year.MIN_VALUE - 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withYear_tooHigh(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.withYear(Year.MAX_VALUE + 1)
  }

  @Test def test_withMonth(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.withMonth(1), YearMonth.of(2008, 1))
  }

  @Test def test_withMonth_int_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.withMonth(6), test)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMonth_tooLow(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.withMonth(0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMonth_tooHigh(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.withMonth(13)
  }

  @Test def test_plusYears_long(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusYears(1), YearMonth.of(2009, 6))
  }

  @Test def test_plusYears_long_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusYears(0), test)
  }

  @Test def test_plusYears_long_negative(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusYears(-1), YearMonth.of(2007, 6))
  }

  @Test def test_plusYears_long_big(): Unit = {
    val test: YearMonth = YearMonth.of(-40, 6)
    assertEquals(test.plusYears(20L + Year.MAX_VALUE), YearMonth.of((-40L + 20L + Year.MAX_VALUE).asInstanceOf[Int], 6))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooLarge(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 6)
    test.plusYears(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooLargeMaxAddMax(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.plusYears(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooLargeMaxAddMin(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.plusYears(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooSmall(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MIN_VALUE, 6)
    test.plusYears(-1)
  }

  @Test def test_plusMonths_long(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusMonths(1), YearMonth.of(2008, 7))
  }

  @Test def test_plusMonths_long_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusMonths(0), test)
  }

  @Test def test_plusMonths_long_overYears(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusMonths(7), YearMonth.of(2009, 1))
  }

  @Test def test_plusMonths_long_negative(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusMonths(-1), YearMonth.of(2008, 5))
  }

  @Test def test_plusMonths_long_negativeOverYear(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.plusMonths(-6), YearMonth.of(2007, 12))
  }

  @Test def test_plusMonths_long_big(): Unit = {
    val test: YearMonth = YearMonth.of(-40, 6)
    val months: Long = 20L + Integer.MAX_VALUE
    assertEquals(test.plusMonths(months), YearMonth.of((-40L + months / 12).toInt, 6 + (months % 12).toInt))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooLarge(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.plusMonths(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooLargeMaxAddMax(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.plusMonths(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooLargeMaxAddMin(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.plusMonths(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooSmall(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MIN_VALUE, 1)
    test.plusMonths(-1)
  }

  @Test def test_minusYears_long(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusYears(1), YearMonth.of(2007, 6))
  }

  @Test def test_minusYears_long_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusYears(0), test)
  }

  @Test def test_minusYears_long_negative(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusYears(-1), YearMonth.of(2009, 6))
  }

  @Test def test_minusYears_long_big(): Unit = {
    val test: YearMonth = YearMonth.of(40, 6)
    assertEquals(test.minusYears(20L + Year.MAX_VALUE), YearMonth.of((40L - 20L - Year.MAX_VALUE).asInstanceOf[Int], 6))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooLarge(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 6)
    test.minusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooLargeMaxSubtractMax(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MIN_VALUE, 12)
    test.minusYears(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooLargeMaxSubtractMin(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MIN_VALUE, 12)
    test.minusYears(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooSmall(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MIN_VALUE, 6)
    test.minusYears(1)
  }

  @Test def test_minusMonths_long(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusMonths(1), YearMonth.of(2008, 5))
  }

  @Test def test_minusMonths_long_noChange_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusMonths(0), test)
  }

  @Test def test_minusMonths_long_overYears(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusMonths(6), YearMonth.of(2007, 12))
  }

  @Test def test_minusMonths_long_negative(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusMonths(-1), YearMonth.of(2008, 7))
  }

  @Test def test_minusMonths_long_negativeOverYear(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.minusMonths(-7), YearMonth.of(2009, 1))
  }

  @Test def test_minusMonths_long_big(): Unit = {
    val test: YearMonth = YearMonth.of(40, 6)
    val months: Long = 20L + Integer.MAX_VALUE
    assertEquals(test.minusMonths(months), YearMonth.of((40L - months / 12).toInt, 6 - (months % 12).toInt))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooLarge(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.minusMonths(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooLargeMaxSubtractMax(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.minusMonths(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooLargeMaxSubtractMin(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MAX_VALUE, 12)
    test.minusMonths(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooSmall(): Unit = {
    val test: YearMonth = YearMonth.of(Year.MIN_VALUE, 1)
    test.minusMonths(1)
  }

  @Test def test_adjustDate(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    val date: LocalDate = LocalDate.of(2007, 1, 1)
    assertEquals(test.adjustInto(date), LocalDate.of(2008, 6, 1))
  }

  @Test def test_adjustDate_preserveDoM(): Unit = {
    val test: YearMonth = YearMonth.of(2011, 3)
    val date: LocalDate = LocalDate.of(2008, 2, 29)
    assertEquals(test.adjustInto(date), LocalDate.of(2011, 3, 29))
  }

  @Test def test_adjustDate_resolve(): Unit = {
    val test: YearMonth = YearMonth.of(2007, 2)
    val date: LocalDate = LocalDate.of(2008, 3, 31)
    assertEquals(test.adjustInto(date), LocalDate.of(2007, 2, 28))
  }

  @Test def test_adjustDate_equal(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    val date: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(test.adjustInto(date), date)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_adjustDate_null(): Unit = {
    TEST_2008_06.adjustInto(null.asInstanceOf[LocalDate])
  }

  @Test def test_isLeapYear(): Unit = {
    assertEquals(YearMonth.of(2007, 6).isLeapYear, false)
    assertEquals(YearMonth.of(2008, 6).isLeapYear, true)
  }

  @Test def test_lengthOfMonth_june(): Unit = {
    val test: YearMonth = YearMonth.of(2007, 6)
    assertEquals(test.lengthOfMonth, 30)
  }

  @Test def test_lengthOfMonth_febNonLeap(): Unit = {
    val test: YearMonth = YearMonth.of(2007, 2)
    assertEquals(test.lengthOfMonth, 28)
  }

  @Test def test_lengthOfMonth_febLeap(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 2)
    assertEquals(test.lengthOfMonth, 29)
  }

  @Test def test_lengthOfYear(): Unit = {
    assertEquals(YearMonth.of(2007, 6).lengthOfYear, 365)
    assertEquals(YearMonth.of(2008, 6).lengthOfYear, 366)
  }

  @Test def test_isValidDay_int_june(): Unit = {
    val test: YearMonth = YearMonth.of(2007, 6)
    assertEquals(test.isValidDay(1), true)
    assertEquals(test.isValidDay(30), true)
    assertEquals(test.isValidDay(-1), false)
    assertEquals(test.isValidDay(0), false)
    assertEquals(test.isValidDay(31), false)
    assertEquals(test.isValidDay(32), false)
  }

  @Test def test_isValidDay_int_febNonLeap(): Unit = {
    val test: YearMonth = YearMonth.of(2007, 2)
    assertEquals(test.isValidDay(1), true)
    assertEquals(test.isValidDay(28), true)
    assertEquals(test.isValidDay(-1), false)
    assertEquals(test.isValidDay(0), false)
    assertEquals(test.isValidDay(29), false)
    assertEquals(test.isValidDay(32), false)
  }

  @Test def test_isValidDay_int_febLeap(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 2)
    assertEquals(test.isValidDay(1), true)
    assertEquals(test.isValidDay(29), true)
    assertEquals(test.isValidDay(-1), false)
    assertEquals(test.isValidDay(0), false)
    assertEquals(test.isValidDay(30), false)
    assertEquals(test.isValidDay(32), false)
  }

  @Test def test_atDay_int(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    assertEquals(test.atDay(30), LocalDate.of(2008, 6, 30))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atDay_int_invalidDay(): Unit = {
    val test: YearMonth = YearMonth.of(2008, 6)
    test.atDay(31)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_2008_06.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(TEST_2008_06.query(TemporalQueries.localDate), null)
    assertEquals(TEST_2008_06.query(TemporalQueries.localTime), null)
    assertEquals(TEST_2008_06.query(TemporalQueries.offset), null)
    assertEquals(TEST_2008_06.query(TemporalQueries.precision), ChronoUnit.MONTHS)
    assertEquals(TEST_2008_06.query(TemporalQueries.zone), null)
    assertEquals(TEST_2008_06.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_2008_06.query(null)
  }

  @Test def test_comparisons(): Unit = {
    doTest_comparisons_YearMonth(YearMonth.of(-1, 1), YearMonth.of(0, 1), YearMonth.of(0, 12), YearMonth.of(1, 1), YearMonth.of(1, 2), YearMonth.of(1, 12), YearMonth.of(2008, 1), YearMonth.of(2008, 6), YearMonth.of(2008, 12))
  }

  private def doTest_comparisons_YearMonth(localDates: YearMonth*): Unit = {
    var i: Int = 0
    while (i < localDates.length) {
      val a: YearMonth = localDates(i)
      var j: Int = 0
      while (j < localDates.length) {
        val b: YearMonth = localDates(j)
        if (i < j) {
          assertTrue(a.compareTo(b) < 0, a + " <=> " + b)
          assertEquals(a.isBefore(b), true, a + " <=> " + b)
          assertEquals(a.isAfter(b), false, a + " <=> " + b)
          assertEquals(a == b, false, a + " <=> " + b)
        }
        else if (i > j) {
          assertTrue(a.compareTo(b) > 0, a + " <=> " + b)
          assertEquals(a.isBefore(b), false, a + " <=> " + b)
          assertEquals(a.isAfter(b), true, a + " <=> " + b)
          assertEquals(a == b, false, a + " <=> " + b)
        }
        else {
          assertEquals(a.compareTo(b), 0, a + " <=> " + b)
          assertEquals(a.isBefore(b), false, a + " <=> " + b)
          assertEquals(a.isAfter(b), false, a + " <=> " + b)
          assertEquals(a == b, true, a + " <=> " + b)
        }
        j += 1
      }
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_ObjectNull(): Unit = {
    TEST_2008_06.compareTo(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_ObjectNull(): Unit = {
    TEST_2008_06.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_ObjectNull(): Unit = {
    TEST_2008_06.isAfter(null)
  }

  @Test def test_equals(): Unit = {
    val a: YearMonth = YearMonth.of(2008, 6)
    val b: YearMonth = YearMonth.of(2008, 6)
    val c: YearMonth = YearMonth.of(2007, 6)
    val d: YearMonth = YearMonth.of(2008, 5)
    assertEquals(a == a, true)
    assertEquals(a == b, true)
    assertEquals(a == c, false)
    assertEquals(a == d, false)
    assertEquals(b == a, true)
    assertEquals(b == b, true)
    assertEquals(b == c, false)
    assertEquals(b == d, false)
    assertEquals(c == a, false)
    assertEquals(c == b, false)
    assertEquals(c == c, true)
    assertEquals(c == d, false)
    assertEquals(d == a, false)
    assertEquals(d == b, false)
    assertEquals(d == c, false)
    assertEquals(d == d, true)
  }

  @Test def test_equals_itself_true(): Unit = {
    assertEquals(TEST_2008_06 == TEST_2008_06, true)
  }

  @Test def test_equals_string_false(): Unit = {
    assertEquals(TEST_2008_06 == "2007-07-15", false)
  }

  @Test def test_equals_null_false(): Unit = {
    assertEquals(TEST_2008_06 == null, false)
  }

  @Test(dataProvider = "sampleDates") def test_hashCode(y: Int, m: Int): Unit = {
    val a: YearMonth = YearMonth.of(y, m)
    assertEquals(a.hashCode, a.hashCode)
    val b: YearMonth = YearMonth.of(y, m)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test def test_hashCode_unique(): Unit = {
    val uniques: java.util.Set[Integer] = new java.util.HashSet[Integer](201 * 12)
    var i: Int = 1900
    while (i <= 2100) {
      var j: Int = 1
      while (j <= 12) {
        assertTrue(uniques.add(YearMonth.of(i, j).hashCode))
        j += 1
      }
      i += 1
    }
  }

  @DataProvider(name = "sampleToString") private[temporal] def provider_sampleToString: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, 1, "2008-01"), Array(2008, 12, "2008-12"), Array(7, 5, "0007-05"), Array(0, 5, "0000-05"), Array(-1, 1, "-0001-01"))
  }

  @Test(dataProvider = "sampleToString") def test_toString(y: Int, m: Int, expected: String): Unit = {
    val test: YearMonth = YearMonth.of(y, m)
    val str: String = test.toString
    assertEquals(str, expected)
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("y M")
    val t: String = YearMonth.of(2010, 12).format(f)
    assertEquals(t, "2010 12")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_format_formatter_null(): Unit = {
    YearMonth.of(2010, 12).format(null)
  }
}
