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
import org.testng.Assert.fail
import org.testng.SkipException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.YEAR
import org.threeten.bp.temporal.ChronoField.YEAR_OF_ERA
import java.io.IOException
import java.util.Arrays
import org.threeten.bp._
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

/** Test Year. */
object TestYear {
  private val TEST_2008: Year = Year.of(2008)
}

@Test class TestYear extends AbstractDateTimeTest {
  @BeforeMethod def setUp(): Unit = {
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TestYear.TEST_2008)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(YEAR_OF_ERA, YEAR, ERA)
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
    AbstractTest.assertImmutable(classOf[Year])
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(Year.of(-1))
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization_format(): Unit = {
    AbstractTest.assertEqualsSerialisedForm(Year.of(2012))
  }

  @Test def now(): Unit = {
    var expected: Year = Year.now(Clock.systemDefaultZone)
    var test: Year = Year.now
    var i: Int = 0
    while (i < 100) {
      if (expected == test)
        return
      expected = Year.now(Clock.systemDefaultZone)
      test = Year.now
      i += 1
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_ZoneId_nullZoneId(): Unit = {
    Year.now(null.asInstanceOf[ZoneId])
  }

  @Test def now_ZoneId(): Unit = {
    val zone: ZoneId = ZoneId.of("UTC+01:02:03")
    var expected: Year = Year.now(Clock.system(zone))
    var test: Year = Year.now(zone)
    var i: Int = 0
    while (i < 100) {
      if (expected == test)
        return
      expected = Year.now(Clock.system(zone))
      test = Year.now(zone)
      i += 1
    }
    assertEquals(test, expected)
  }

  @Test def now_Clock(): Unit = {
    val instant: Instant = LocalDateTime.of(2010, 12, 31, 0, 0).toInstant(ZoneOffset.UTC)
    val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
    val test: Year = Year.now(clock)
    assertEquals(test.getValue, 2010)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    Year.now(null.asInstanceOf[Clock])
  }

  @Test def test_factory_int_singleton(): Unit = {
    var i: Int = -4
    while (i <= 2104) {
      val test: Year = Year.of(i)
      assertEquals(test.getValue, i)
      assertEquals(Year.of(i), test)
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_int_tooLow(): Unit = {
    Year.of(Year.MIN_VALUE - 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_int_tooHigh(): Unit = {
    Year.of(Year.MAX_VALUE + 1)
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(Year.from(LocalDate.of(2007, 7, 15)), Year.of(2007))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    Year.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_CalendricalObject_null(): Unit = {
    Year.from(null.asInstanceOf[TemporalAccessor])
  }

  @DataProvider(name = "goodParseData") private[temporal] def provider_goodParseData: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("0000", Year.of(0)), Array("9999", Year.of(9999)), Array("2000", Year.of(2000)), Array("+12345678", Year.of(12345678)), Array("+123456", Year.of(123456)), Array("-1234", Year.of(-1234)), Array("-12345678", Year.of(-12345678)), Array("+" + Year.MAX_VALUE, Year.of(Year.MAX_VALUE)), Array("" + Year.MIN_VALUE, Year.of(Year.MIN_VALUE)))
  }

  @Test(dataProvider = "goodParseData") def factory_parse_success(text: String, expected: Year): Unit = {
    val year: Year = Year.parse(text)
    assertEquals(year, expected)
  }

  @DataProvider(name = "badParseData") private[temporal] def provider_badParseData: Array[Array[Any]] = {
    Array[Array[Any]](Array("", 0), Array("-00", 1), Array("--01-0", 1), Array("A01", 0), Array("200", 0), Array("2009/12", 4), Array("-0000-10", 0), Array("-12345678901-10", 11), Array("+1-10", 1), Array("+12-10", 1), Array("+123-10", 1), Array("+1234-10", 0), Array("12345-10", 0), Array("+12345678901-10", 11))
  }

  @Test(dataProvider = "badParseData", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_fail(text: String, pos: Int): Unit = {
    try {
      Year.parse(text)
      fail(f"Parse should have failed for $text%s at position $pos%d")
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getParsedString, text)
        assertEquals(ex.getErrorIndex, pos)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    Year.parse(null)
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u")
    val test: Year = Year.parse("2010", f)
    assertEquals(test, Year.of(2010))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u")
    Year.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    Year.parse("ANY", null)
  }

  @Test def test_get_DateTimeField(): Unit = {
    assertEquals(TestYear.TEST_2008.getLong(ChronoField.YEAR), 2008)
    assertEquals(TestYear.TEST_2008.getLong(ChronoField.YEAR_OF_ERA), 2008)
    assertEquals(TestYear.TEST_2008.getLong(ChronoField.ERA), 1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_get_DateTimeField_null(): Unit = {
    TestYear.TEST_2008.getLong(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_DateTimeField_invalidField(): Unit = {
    TestYear.TEST_2008.getLong(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_DateTimeField_timeField(): Unit = {
    TestYear.TEST_2008.getLong(ChronoField.AMPM_OF_DAY)
  }

  @Test def test_isLeap(): Unit = {
    assertEquals(Year.of(1999).isLeap, false)
    assertEquals(Year.of(2000).isLeap, true)
    assertEquals(Year.of(2001).isLeap, false)
    assertEquals(Year.of(2007).isLeap, false)
    assertEquals(Year.of(2008).isLeap, true)
    assertEquals(Year.of(2009).isLeap, false)
    assertEquals(Year.of(2010).isLeap, false)
    assertEquals(Year.of(2011).isLeap, false)
    assertEquals(Year.of(2012).isLeap, true)
    assertEquals(Year.of(2095).isLeap, false)
    assertEquals(Year.of(2096).isLeap, true)
    assertEquals(Year.of(2097).isLeap, false)
    assertEquals(Year.of(2098).isLeap, false)
    assertEquals(Year.of(2099).isLeap, false)
    assertEquals(Year.of(2100).isLeap, false)
    assertEquals(Year.of(2101).isLeap, false)
    assertEquals(Year.of(2102).isLeap, false)
    assertEquals(Year.of(2103).isLeap, false)
    assertEquals(Year.of(2104).isLeap, true)
    assertEquals(Year.of(2105).isLeap, false)
    assertEquals(Year.of(-500).isLeap, false)
    assertEquals(Year.of(-400).isLeap, true)
    assertEquals(Year.of(-300).isLeap, false)
    assertEquals(Year.of(-200).isLeap, false)
    assertEquals(Year.of(-100).isLeap, false)
    assertEquals(Year.of(0).isLeap, true)
    assertEquals(Year.of(100).isLeap, false)
    assertEquals(Year.of(200).isLeap, false)
    assertEquals(Year.of(300).isLeap, false)
    assertEquals(Year.of(400).isLeap, true)
    assertEquals(Year.of(500).isLeap, false)
  }

  @Test def test_plusYears(): Unit = {
    assertEquals(Year.of(2007).plusYears(-1), Year.of(2006))
    assertEquals(Year.of(2007).plusYears(0), Year.of(2007))
    assertEquals(Year.of(2007).plusYears(1), Year.of(2008))
    assertEquals(Year.of(2007).plusYears(2), Year.of(2009))
    assertEquals(Year.of(Year.MAX_VALUE - 1).plusYears(1), Year.of(Year.MAX_VALUE))
    assertEquals(Year.of(Year.MAX_VALUE).plusYears(0), Year.of(Year.MAX_VALUE))
    assertEquals(Year.of(Year.MIN_VALUE + 1).plusYears(-1), Year.of(Year.MIN_VALUE))
    assertEquals(Year.of(Year.MIN_VALUE).plusYears(0), Year.of(Year.MIN_VALUE))
  }

  @Test def test_plusYear_zero_equals(): Unit = {
    val base: Year = Year.of(2007)
    assertEquals(base.plusYears(0), base)
  }

  @Test def test_plusYears_big(): Unit = {
    val years: Long = 20L + Year.MAX_VALUE
    assertEquals(Year.of(-40).plusYears(years), Year.of((-40L + years).toInt))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_max(): Unit = {
    Year.of(Year.MAX_VALUE).plusYears(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_maxLots(): Unit = {
    Year.of(Year.MAX_VALUE).plusYears(1000)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_min(): Unit = {
    Year.of(Year.MIN_VALUE).plusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_minLots(): Unit = {
    Year.of(Year.MIN_VALUE).plusYears(-1000)
  }

  @Test def test_minusYears(): Unit = {
    assertEquals(Year.of(2007).minusYears(-1), Year.of(2008))
    assertEquals(Year.of(2007).minusYears(0), Year.of(2007))
    assertEquals(Year.of(2007).minusYears(1), Year.of(2006))
    assertEquals(Year.of(2007).minusYears(2), Year.of(2005))
    assertEquals(Year.of(Year.MAX_VALUE - 1).minusYears(-1), Year.of(Year.MAX_VALUE))
    assertEquals(Year.of(Year.MAX_VALUE).minusYears(0), Year.of(Year.MAX_VALUE))
    assertEquals(Year.of(Year.MIN_VALUE + 1).minusYears(1), Year.of(Year.MIN_VALUE))
    assertEquals(Year.of(Year.MIN_VALUE).minusYears(0), Year.of(Year.MIN_VALUE))
  }

  @Test def test_minusYear_zero_equals(): Unit = {
    val base: Year = Year.of(2007)
    assertEquals(base.minusYears(0), base)
  }

  @Test def test_minusYears_big(): Unit = {
    val years: Long = 20L + Year.MAX_VALUE
    assertEquals(Year.of(40).minusYears(years), Year.of((40L - years).toInt))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_max(): Unit = {
    Year.of(Year.MAX_VALUE).minusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_maxLots(): Unit = {
    Year.of(Year.MAX_VALUE).minusYears(-1000)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_min(): Unit = {
    Year.of(Year.MIN_VALUE).minusYears(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_minLots(): Unit = {
    Year.of(Year.MIN_VALUE).minusYears(1000)
  }

  @Test def test_adjustDate(): Unit = {
    val base: LocalDate = LocalDate.of(2007, 2, 12)
    var i: Int = -4
    while (i <= 2104) {
      val result: Temporal = Year.of(i).adjustInto(base)
      assertEquals(result, LocalDate.of(i, 2, 12))
      i += 1
    }
  }

  @Test def test_adjustDate_resolve(): Unit = {
    val test: Year = Year.of(2011)
    assertEquals(test.adjustInto(LocalDate.of(2012, 2, 29)), LocalDate.of(2011, 2, 28))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_adjustDate_nullLocalDate(): Unit = {
    val test: Year = Year.of(1)
    test.adjustInto(null.asInstanceOf[LocalDate])
  }

  @Test def test_length(): Unit = {
    assertEquals(Year.of(1999).length, 365)
    assertEquals(Year.of(2000).length, 366)
    assertEquals(Year.of(2001).length, 365)
    assertEquals(Year.of(2007).length, 365)
    assertEquals(Year.of(2008).length, 366)
    assertEquals(Year.of(2009).length, 365)
    assertEquals(Year.of(2010).length, 365)
    assertEquals(Year.of(2011).length, 365)
    assertEquals(Year.of(2012).length, 366)
    assertEquals(Year.of(2095).length, 365)
    assertEquals(Year.of(2096).length, 366)
    assertEquals(Year.of(2097).length, 365)
    assertEquals(Year.of(2098).length, 365)
    assertEquals(Year.of(2099).length, 365)
    assertEquals(Year.of(2100).length, 365)
    assertEquals(Year.of(2101).length, 365)
    assertEquals(Year.of(2102).length, 365)
    assertEquals(Year.of(2103).length, 365)
    assertEquals(Year.of(2104).length, 366)
    assertEquals(Year.of(2105).length, 365)
    assertEquals(Year.of(-500).length, 365)
    assertEquals(Year.of(-400).length, 366)
    assertEquals(Year.of(-300).length, 365)
    assertEquals(Year.of(-200).length, 365)
    assertEquals(Year.of(-100).length, 365)
    assertEquals(Year.of(0).length, 366)
    assertEquals(Year.of(100).length, 365)
    assertEquals(Year.of(200).length, 365)
    assertEquals(Year.of(300).length, 365)
    assertEquals(Year.of(400).length, 366)
    assertEquals(Year.of(500).length, 365)
  }

  @Test def test_isValidMonthDay_june(): Unit = {
    val test: Year = Year.of(2007)
    val monthDay: MonthDay = MonthDay.of(6, 30)
    assertEquals(test.isValidMonthDay(monthDay), true)
  }

  @Test def test_isValidMonthDay_febNonLeap(): Unit = {
    val test: Year = Year.of(2007)
    val monthDay: MonthDay = MonthDay.of(2, 29)
    assertEquals(test.isValidMonthDay(monthDay), false)
  }

  @Test def test_isValidMonthDay_febLeap(): Unit = {
    val test: Year = Year.of(2008)
    val monthDay: MonthDay = MonthDay.of(2, 29)
    assertEquals(test.isValidMonthDay(monthDay), true)
  }

  @Test def test_isValidMonthDay_null(): Unit = {
    val test: Year = Year.of(2008)
    assertEquals(test.isValidMonthDay(null), false)
  }

  @Test def test_atMonth(): Unit = {
    val test: Year = Year.of(2008)
    assertEquals(test.atMonth(Month.JUNE), YearMonth.of(2008, 6))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atMonth_nullMonth(): Unit = {
    val test: Year = Year.of(2008)
    test.atMonth(null.asInstanceOf[Month])
  }

  @Test def test_atMonth_int(): Unit = {
    val test: Year = Year.of(2008)
    assertEquals(test.atMonth(6), YearMonth.of(2008, 6))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atMonth_int_invalidMonth(): Unit = {
    val test: Year = Year.of(2008)
    test.atMonth(13)
  }

  @DataProvider(name = "atMonthDay") private[temporal] def data_atMonthDay: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(Year.of(2008), MonthDay.of(6, 30), LocalDate.of(2008, 6, 30)), Array(Year.of(2008), MonthDay.of(2, 29), LocalDate.of(2008, 2, 29)), Array(Year.of(2009), MonthDay.of(2, 29), LocalDate.of(2009, 2, 28)))
  }

  @Test(dataProvider = "atMonthDay") def test_atMonthDay(year: Year, monthDay: MonthDay, expected: LocalDate): Unit = {
    assertEquals(year.atMonthDay(monthDay), expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atMonthDay_nullMonthDay(): Unit = {
    val test: Year = Year.of(2008)
    test.atMonthDay(null.asInstanceOf[MonthDay])
  }

  @Test def test_atDay_notLeapYear(): Unit = {
    val test: Year = Year.of(2007)
    var expected: LocalDate = LocalDate.of(2007, 1, 1)
    var i: Int = 1
    while (i <= 365) {
      assertEquals(test.atDay(i), expected)
      expected = expected.plusDays(1)
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atDay_notLeapYear_day366(): Unit = {
    val test: Year = Year.of(2007)
    test.atDay(366)
  }

  @Test def test_atDay_leapYear(): Unit = {
    val test: Year = Year.of(2008)
    var expected: LocalDate = LocalDate.of(2008, 1, 1)
    var i: Int = 1
    while (i <= 366) {
      assertEquals(test.atDay(i), expected)
      expected = expected.plusDays(1)
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atDay_day0(): Unit = {
    val test: Year = Year.of(2007)
    test.atDay(0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atDay_day367(): Unit = {
    val test: Year = Year.of(2007)
    test.atDay(367)
  }

  @Test def test_query(): Unit = {
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.localDate), null)
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.localTime), null)
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.offset), null)
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.precision), ChronoUnit.YEARS)
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.zone), null)
    assertEquals(TestYear.TEST_2008.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TestYear.TEST_2008.query(null)
  }

  @Test def test_compareTo(): Unit = {
    var i: Int = -4
    while (i <= 2104) {
      val a: Year = Year.of(i)
      var j: Int = -4
      while (j <= 2104) {
        val b: Year = Year.of(j)
        if (i < j) {
          assertEquals(a.compareTo(b) < 0, true)
          assertEquals(b.compareTo(a) > 0, true)
          assertEquals(a.isAfter(b), false)
          assertEquals(a.isBefore(b), true)
          assertEquals(b.isAfter(a), true)
          assertEquals(b.isBefore(a), false)
        }
        else if (i > j) {
          assertEquals(a.compareTo(b) > 0, true)
          assertEquals(b.compareTo(a) < 0, true)
          assertEquals(a.isAfter(b), true)
          assertEquals(a.isBefore(b), false)
          assertEquals(b.isAfter(a), false)
          assertEquals(b.isBefore(a), true)
        }
        else {
          assertEquals(a.compareTo(b), 0)
          assertEquals(b.compareTo(a), 0)
          assertEquals(a.isAfter(b), false)
          assertEquals(a.isBefore(b), false)
          assertEquals(b.isAfter(a), false)
          assertEquals(b.isBefore(a), false)
        }
        j += 1
      }
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_nullYear(): Unit = {
    val doy: Year = null
    val test: Year = Year.of(1)
    test.compareTo(doy)
  }

  @Test def test_equals(): Unit = {
    var i: Int = -4
    while (i <= 2104) {
      val a: Year = Year.of(i)
      var j: Int = -4
      while (j <= 2104) {
        val b: Year = Year.of(j)
        assertEquals(a == b, i == j)
        assertEquals(a.hashCode == b.hashCode, i == j)
        j += 1
      }
      i += 1
    }
  }

  @Test def test_equals_same(): Unit = {
    val test: Year = Year.of(2011)
    assertEquals(test == test, true)
  }

  @Test def test_equals_nullYear(): Unit = {
    val doy: Year = null
    val test: Year = Year.of(1)
    assertEquals(test == doy, false)
  }

  @Test def test_equals_incorrectType(): Unit = {
    val test: Year = Year.of(1)
    assertEquals(test == "Incorrect type", false)
  }

  @Test def test_toString(): Unit = {
    var i: Int = -4
    while (i <= 2104) {
      val a: Year = Year.of(i)
      assertEquals(a.toString, "" + i)
      i += 1
    }
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("y")
    val t: String = Year.of(2010).format(f)
    assertEquals(t, "2010")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def format_formatter_null(): Unit = {
    Year.of(2010).format(null)
  }
}
