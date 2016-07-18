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

import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import java.io.IOException
import java.util.Arrays

import org.threeten.bp._
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

/** Test MonthDay. */
class TestMonthDay extends AbstractDateTimeTest {
  private var TEST_07_15: MonthDay = null

  @BeforeMethod def setUp(): Unit = {
    TEST_07_15 = MonthDay.of(7, 15)
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_07_15)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(DAY_OF_MONTH, MONTH_OF_YEAR)
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
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(TEST_07_15)
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization_format(): Unit = {
    AbstractTest.assertEqualsSerialisedForm(MonthDay.of(9, 16))
  }

  private[temporal] def check(test: MonthDay, m: Int, d: Int): Unit = {
    assertEquals(test.getMonth.getValue, m)
    assertEquals(test.getDayOfMonth, d)
  }

  @Test def now(): Unit = {
    var expected: MonthDay = MonthDay.now(Clock.systemDefaultZone)
    var test: MonthDay = MonthDay.now
    var i: Int = 0
    while (i < 100) {
      if (expected == test)
        return
      expected = MonthDay.now(Clock.systemDefaultZone)
      test = MonthDay.now
      i += 1
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_ZoneId_nullZoneId(): Unit = {
    MonthDay.now(null.asInstanceOf[ZoneId])
  }

  @Test def now_ZoneId(): Unit = {
    val zone: ZoneId = ZoneId.of("UTC+01:02:03")
    var expected: MonthDay = MonthDay.now(Clock.system(zone))
    var test: MonthDay = MonthDay.now(zone)
    var i: Int = 0
    while (i < 100) {
      if (expected == test)
        return
      expected = MonthDay.now(Clock.system(zone))
      test = MonthDay.now(zone)
      i += 1
    }
    assertEquals(test, expected)
  }

  @Test def now_Clock(): Unit = {
    val instant: Instant = LocalDateTime.of(2010, 12, 31, 0, 0).toInstant(ZoneOffset.UTC)
    val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
    val test: MonthDay = MonthDay.now(clock)
    assertEquals(test.getMonth, Month.DECEMBER)
    assertEquals(test.getDayOfMonth, 31)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    MonthDay.now(null.asInstanceOf[Clock])
  }

  @Test def factory_intMonth(): Unit = {
    assertEquals(TEST_07_15, MonthDay.of(Month.JULY, 15))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_intMonth_dayTooLow(): Unit = {
    MonthDay.of(Month.JANUARY, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_intMonth_dayTooHigh(): Unit = {
    MonthDay.of(Month.JANUARY, 32)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_intMonth_nullMonth(): Unit = {
    MonthDay.of(null, 15)
  }

  @Test def factory_ints(): Unit = {
    check(TEST_07_15, 7, 15)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_dayTooLow(): Unit = {
    MonthDay.of(1, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_dayTooHigh(): Unit = {
    MonthDay.of(1, 32)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_monthTooLow(): Unit = {
    MonthDay.of(0, 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_ints_monthTooHigh(): Unit = {
    MonthDay.of(13, 1)
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(MonthDay.from(LocalDate.of(2007, 7, 15)), TEST_07_15)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    MonthDay.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_CalendricalObject_null(): Unit = {
    MonthDay.from(null.asInstanceOf[TemporalAccessor])
  }

  @DataProvider(name = "goodParseData") private[temporal] def provider_goodParseData: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("--01-01", MonthDay.of(1, 1)), Array("--01-31", MonthDay.of(1, 31)), Array("--02-01", MonthDay.of(2, 1)), Array("--02-29", MonthDay.of(2, 29)), Array("--03-01", MonthDay.of(3, 1)), Array("--03-31", MonthDay.of(3, 31)), Array("--04-01", MonthDay.of(4, 1)), Array("--04-30", MonthDay.of(4, 30)), Array("--05-01", MonthDay.of(5, 1)), Array("--05-31", MonthDay.of(5, 31)), Array("--06-01", MonthDay.of(6, 1)), Array("--06-30", MonthDay.of(6, 30)), Array("--07-01", MonthDay.of(7, 1)), Array("--07-31", MonthDay.of(7, 31)), Array("--08-01", MonthDay.of(8, 1)), Array("--08-31", MonthDay.of(8, 31)), Array("--09-01", MonthDay.of(9, 1)), Array("--09-30", MonthDay.of(9, 30)), Array("--10-01", MonthDay.of(10, 1)), Array("--10-31", MonthDay.of(10, 31)), Array("--11-01", MonthDay.of(11, 1)), Array("--11-30", MonthDay.of(11, 30)), Array("--12-01", MonthDay.of(12, 1)), Array("--12-31", MonthDay.of(12, 31)))
  }

  @Test(dataProvider = "goodParseData") def factory_parse_success(text: String, expected: MonthDay): Unit = {
    val monthDay: MonthDay = MonthDay.parse(text)
    assertEquals(monthDay, expected)
  }

  @DataProvider(name = "badParseData") private[temporal] def provider_badParseData: Array[Array[Any]] = {
    Array[Array[Any]](Array("", 0), Array("-00", 0), Array("--FEB-23", 2), Array("--01-0", 5), Array("--01-3A", 5))
  }

  @Test(dataProvider = "badParseData", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_fail(text: String, pos: Int): Unit = {
    try {
      MonthDay.parse(text)
      fail(f"Parse should have failed for $text%s at position $pos%d")
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getParsedString, text)
        assertEquals(ex.getErrorIndex, pos)
        throw ex
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalValue_Day(): Unit = {
    MonthDay.parse("--06-32")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_invalidValue_Day(): Unit = {
    MonthDay.parse("--06-31")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalValue_Month(): Unit = {
    MonthDay.parse("--13-25")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    MonthDay.parse(null)
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("M d")
    val test: MonthDay = MonthDay.parse("12 3", f)
    assertEquals(test, MonthDay.of(12, 3))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("M d")
    MonthDay.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    MonthDay.parse("ANY", null)
  }

  @Test def test_get_DateTimeField(): Unit = {
    assertEquals(TEST_07_15.getLong(ChronoField.DAY_OF_MONTH), 15)
    assertEquals(TEST_07_15.getLong(ChronoField.MONTH_OF_YEAR), 7)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_get_DateTimeField_null(): Unit = {
    TEST_07_15.getLong(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_DateTimeField_invalidField(): Unit = {
    TEST_07_15.getLong(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_DateTimeField_timeField(): Unit = {
    TEST_07_15.getLong(ChronoField.AMPM_OF_DAY)
  }

  @DataProvider(name = "sampleDates") private[temporal] def provider_sampleDates: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, 1), Array(1, 31), Array(2, 1), Array(2, 28), Array(2, 29), Array(7, 4), Array(7, 5))
  }

  @Test(dataProvider = "sampleDates") def test_get(m: Int, d: Int): Unit = {
    val a: MonthDay = MonthDay.of(m, d)
    assertEquals(a.getMonth, Month.of(m))
    assertEquals(a.getDayOfMonth, d)
  }

  @Test def test_with_Month(): Unit = {
    assertEquals(MonthDay.of(6, 30).`with`(Month.JANUARY), MonthDay.of(1, 30))
  }

  @Test def test_with_Month_adjustToValid(): Unit = {
    assertEquals(MonthDay.of(7, 31).`with`(Month.JUNE), MonthDay.of(6, 30))
  }

  @Test def test_with_Month_adjustToValidFeb(): Unit = {
    assertEquals(MonthDay.of(7, 31).`with`(Month.FEBRUARY), MonthDay.of(2, 29))
  }

  @Test def test_with_Month_noChangeEqual(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    assertEquals(test.`with`(Month.JUNE), test)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_Month_null(): Unit = {
    MonthDay.of(6, 30).`with`(null.asInstanceOf[Month])
  }

  @Test def test_withMonth(): Unit = {
    assertEquals(MonthDay.of(6, 30).withMonth(1), MonthDay.of(1, 30))
  }

  @Test def test_withMonth_adjustToValid(): Unit = {
    assertEquals(MonthDay.of(7, 31).withMonth(6), MonthDay.of(6, 30))
  }

  @Test def test_withMonth_adjustToValidFeb(): Unit = {
    assertEquals(MonthDay.of(7, 31).withMonth(2), MonthDay.of(2, 29))
  }

  @Test def test_withMonth_int_noChangeEqual(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    assertEquals(test.withMonth(6), test)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMonth_tooLow(): Unit = {
    MonthDay.of(6, 30).withMonth(0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMonth_tooHigh(): Unit = {
    MonthDay.of(6, 30).withMonth(13)
  }

  @Test def test_withDayOfMonth(): Unit = {
    assertEquals(MonthDay.of(6, 30).withDayOfMonth(1), MonthDay.of(6, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_invalid(): Unit = {
    MonthDay.of(6, 30).withDayOfMonth(31)
  }

  @Test def test_withDayOfMonth_adjustToValidFeb(): Unit = {
    assertEquals(MonthDay.of(2, 1).withDayOfMonth(29), MonthDay.of(2, 29))
  }

  @Test def test_withDayOfMonth_noChangeEqual(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    assertEquals(test.withDayOfMonth(30), test)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_tooLow(): Unit = {
    MonthDay.of(6, 30).withDayOfMonth(0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_tooHigh(): Unit = {
    MonthDay.of(6, 30).withDayOfMonth(32)
  }

  @Test def test_adjustDate(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    val date: LocalDate = LocalDate.of(2007, 1, 1)
    assertEquals(test.adjustInto(date), LocalDate.of(2007, 6, 30))
  }

  @Test def test_adjustDate_resolve(): Unit = {
    val test: MonthDay = MonthDay.of(2, 29)
    val date: LocalDate = LocalDate.of(2007, 6, 30)
    assertEquals(test.adjustInto(date), LocalDate.of(2007, 2, 28))
  }

  @Test def test_adjustDate_equal(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    val date: LocalDate = LocalDate.of(2007, 6, 30)
    assertEquals(test.adjustInto(date), date)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_adjustDate_null(): Unit = {
    TEST_07_15.adjustInto(null.asInstanceOf[LocalDate])
  }

  @Test def test_isValidYear_june(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    assertEquals(test.isValidYear(2007), true)
  }

  @Test def test_isValidYear_febNonLeap(): Unit = {
    val test: MonthDay = MonthDay.of(2, 29)
    assertEquals(test.isValidYear(2007), false)
  }

  @Test def test_isValidYear_febLeap(): Unit = {
    val test: MonthDay = MonthDay.of(2, 29)
    assertEquals(test.isValidYear(2008), true)
  }

  @Test def test_atYear_int(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    assertEquals(test.atYear(2008), LocalDate.of(2008, 6, 30))
  }

  @Test def test_atYear_int_leapYearAdjust(): Unit = {
    val test: MonthDay = MonthDay.of(2, 29)
    assertEquals(test.atYear(2005), LocalDate.of(2005, 2, 28))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atYear_int_invalidYear(): Unit = {
    val test: MonthDay = MonthDay.of(6, 30)
    test.atYear(Integer.MIN_VALUE)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_07_15.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(TEST_07_15.query(TemporalQueries.localDate), null)
    assertEquals(TEST_07_15.query(TemporalQueries.localTime), null)
    assertEquals(TEST_07_15.query(TemporalQueries.offset), null)
    assertEquals(TEST_07_15.query(TemporalQueries.precision), null)
    assertEquals(TEST_07_15.query(TemporalQueries.zone), null)
    assertEquals(TEST_07_15.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_07_15.query(null)
  }

  @Test def test_comparisons(): Unit = {
    doTest_comparisons_MonthDay(MonthDay.of(1, 1), MonthDay.of(1, 31), MonthDay.of(2, 1), MonthDay.of(2, 29), MonthDay.of(3, 1), MonthDay.of(12, 31))
  }

  private[temporal] def doTest_comparisons_MonthDay(localDates: MonthDay*): Unit = {
    {
      var i: Int = 0
      while (i < localDates.length) {
        {
          val a: MonthDay = localDates(i)

          {
            var j: Int = 0
            while (j < localDates.length) {
              {
                val b: MonthDay = localDates(j)
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
              }
              {
                j += 1
                j - 1
              }
            }
          }
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_ObjectNull(): Unit = {
    TEST_07_15.compareTo(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_ObjectNull(): Unit = {
    TEST_07_15.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_ObjectNull(): Unit = {
    TEST_07_15.isAfter(null)
  }

  @Test def test_equals(): Unit = {
    val a: MonthDay = MonthDay.of(1, 1)
    val b: MonthDay = MonthDay.of(1, 1)
    val c: MonthDay = MonthDay.of(2, 1)
    val d: MonthDay = MonthDay.of(1, 2)
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
    assertEquals(TEST_07_15 == TEST_07_15, true)
  }

  @Test def test_equals_string_false(): Unit = {
    assertEquals(TEST_07_15 == "2007-07-15", false)
  }

  @Test def test_equals_null_false(): Unit = {
    assertEquals(TEST_07_15 == null, false)
  }

  @Test(dataProvider = "sampleDates") def test_hashCode(m: Int, d: Int): Unit = {
    val a: MonthDay = MonthDay.of(m, d)
    assertEquals(a.hashCode, a.hashCode)
    val b: MonthDay = MonthDay.of(m, d)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test def test_hashCode_unique(): Unit = {
    val leapYear: Int = 2008
    val uniques: java.util.Set[Integer] = new java.util.HashSet[Integer](366)
    var i: Int = 1
    while (i <= 12) {
      var j: Int = 1
      while (j <= 31) {
        if (YearMonth.of(leapYear, i).isValidDay(j)) {
          assertTrue(uniques.add(MonthDay.of(i, j).hashCode))
        }
        j += 1
      }
      i += 1
    }
  }

  @DataProvider(name = "sampleToString") private[temporal] def provider_sampleToString: Array[Array[Any]] = {
    Array[Array[Any]](Array(7, 5, "--07-05"), Array(12, 31, "--12-31"), Array(1, 2, "--01-02"))
  }

  @Test(dataProvider = "sampleToString") def test_toString(m: Int, d: Int, expected: String): Unit = {
    val test: MonthDay = MonthDay.of(m, d)
    val str: String = test.toString
    assertEquals(str, expected)
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("M d")
    val t: String = MonthDay.of(12, 3).format(f)
    assertEquals(t, "12 3")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_format_formatter_null(): Unit = {
    MonthDay.of(12, 3).format(null)
  }
}
