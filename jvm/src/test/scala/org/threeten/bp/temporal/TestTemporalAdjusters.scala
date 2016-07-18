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
import org.testng.Assert.assertFalse
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertSame
import org.testng.Assert.assertTrue
import org.threeten.bp.DayOfWeek.MONDAY
import org.threeten.bp.DayOfWeek.TUESDAY
import org.threeten.bp.Month.DECEMBER
import org.threeten.bp.Month.JANUARY
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

/** Test DateTimeAdjusters. */
class TestTemporalAdjusters {
  @Test def factory_firstDayOfMonth(): Unit = {
    assertNotNull(TemporalAdjusters.firstDayOfMonth)
  }

  @Test def test_firstDayOfMonth_nonLeap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfMonth.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2007)
        assertEquals(test.getMonth, month)
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def test_firstDayOfMonth_leap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(true)) {
        val _date: LocalDate = date(2008, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfMonth.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2008)
        assertEquals(test.getMonth, month)
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def factory_lastDayOfMonth(): Unit = {
    assertNotNull(TemporalAdjusters.lastDayOfMonth)
  }

  @Test def test_lastDayOfMonth_nonLeap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        val test: LocalDate = TemporalAdjusters.lastDayOfMonth.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2007)
        assertEquals(test.getMonth, month)
        assertEquals(test.getDayOfMonth, month.length(false))
        i += 1
      }
    }
  }

  @Test def test_lastDayOfMonth_leap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(true)) {
        val _date: LocalDate = date(2008, month, i)
        val test: LocalDate = TemporalAdjusters.lastDayOfMonth.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2008)
        assertEquals(test.getMonth, month)
        assertEquals(test.getDayOfMonth, month.length(true))
        i += 1
      }
    }
  }

  @Test def factory_firstDayOfNextMonth(): Unit = {
    assertNotNull(TemporalAdjusters.firstDayOfNextMonth)
  }

  @Test def test_firstDayOfNextMonth_nonLeap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfNextMonth.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, if (month eq DECEMBER) 2008 else 2007)
        assertEquals(test.getMonth, month.plus(1))
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def test_firstDayOfNextMonth_leap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(true)) {
        val _date: LocalDate = date(2008, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfNextMonth.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, if (month eq DECEMBER) 2009 else 2008)
        assertEquals(test.getMonth, month.plus(1))
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def factory_firstDayOfYear(): Unit = {
    assertNotNull(TemporalAdjusters.firstDayOfYear)
  }

  @Test def test_firstDayOfYear_nonLeap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfYear.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2007)
        assertEquals(test.getMonth, Month.JANUARY)
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def test_firstDayOfYear_leap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(true)) {
        val _date: LocalDate = date(2008, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfYear.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2008)
        assertEquals(test.getMonth, Month.JANUARY)
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def factory_lastDayOfYear(): Unit = {
    assertNotNull(TemporalAdjusters.lastDayOfYear)
  }

  @Test def test_lastDayOfYear_nonLeap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        val test: LocalDate = TemporalAdjusters.lastDayOfYear.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2007)
        assertEquals(test.getMonth, Month.DECEMBER)
        assertEquals(test.getDayOfMonth, 31)
        i += 1
      }
    }
  }

  @Test def test_lastDayOfYear_leap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(true)) {
        val _date: LocalDate = date(2008, month, i)
        val test: LocalDate = TemporalAdjusters.lastDayOfYear.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2008)
        assertEquals(test.getMonth, Month.DECEMBER)
        assertEquals(test.getDayOfMonth, 31)
        i += 1
      }
    }
  }

  @Test def factory_firstDayOfNextYear(): Unit = {
    assertNotNull(TemporalAdjusters.firstDayOfNextYear)
  }

  @Test def test_firstDayOfNextYear_nonLeap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfNextYear.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2008)
        assertEquals(test.getMonth, JANUARY)
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def test_firstDayOfNextYear_leap(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(true)) {
        val _date: LocalDate = date(2008, month, i)
        val test: LocalDate = TemporalAdjusters.firstDayOfNextYear.adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test.getYear, 2009)
        assertEquals(test.getMonth, JANUARY)
        assertEquals(test.getDayOfMonth, 1)
        i += 1
      }
    }
  }

  @Test def factory_dayOfWeekInMonth(): Unit = {
    assertNotNull(TemporalAdjusters.dayOfWeekInMonth(1, MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_dayOfWeekInMonth_nullDayOfWeek(): Unit = {
    TemporalAdjusters.dayOfWeekInMonth(1, null)
  }

  @DataProvider(name = "dayOfWeekInMonth_positive") private[temporal] def data_dayOfWeekInMonth_positive: Array[Array[Any]] = {
    Array[Array[Any]](Array(2011, 1, TUESDAY, date(2011, 1, 4)), Array(2011, 2, TUESDAY, date(2011, 2, 1)), Array(2011, 3, TUESDAY, date(2011, 3, 1)), Array(2011, 4, TUESDAY, date(2011, 4, 5)), Array(2011, 5, TUESDAY, date(2011, 5, 3)), Array(2011, 6, TUESDAY, date(2011, 6, 7)), Array(2011, 7, TUESDAY, date(2011, 7, 5)), Array(2011, 8, TUESDAY, date(2011, 8, 2)), Array(2011, 9, TUESDAY, date(2011, 9, 6)), Array(2011, 10, TUESDAY, date(2011, 10, 4)), Array(2011, 11, TUESDAY, date(2011, 11, 1)), Array(2011, 12, TUESDAY, date(2011, 12, 6)))
  }

  @Test(dataProvider = "dayOfWeekInMonth_positive") def test_dayOfWeekInMonth_positive(year: Int, month: Int, dow: DayOfWeek, expected: LocalDate): Unit = {
    var ordinal: Int = 1
    while (ordinal <= 5) {
      var day: Int = 1
      while (day <= Month.of(month).length(false)) {
        val _date: LocalDate = date(year, month, day)
        val test: LocalDate = TemporalAdjusters.dayOfWeekInMonth(ordinal, dow).adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test, expected.plusWeeks(ordinal - 1))
        day += 1
      }
      ordinal += 1
    }
  }

  @DataProvider(name = "dayOfWeekInMonth_zero") private[temporal] def data_dayOfWeekInMonth_zero: Array[Array[Any]] = {
    Array[Array[Any]](Array(2011, 1, TUESDAY, date(2010, 12, 28)), Array(2011, 2, TUESDAY, date(2011, 1, 25)), Array(2011, 3, TUESDAY, date(2011, 2, 22)), Array(2011, 4, TUESDAY, date(2011, 3, 29)), Array(2011, 5, TUESDAY, date(2011, 4, 26)), Array(2011, 6, TUESDAY, date(2011, 5, 31)), Array(2011, 7, TUESDAY, date(2011, 6, 28)), Array(2011, 8, TUESDAY, date(2011, 7, 26)), Array(2011, 9, TUESDAY, date(2011, 8, 30)), Array(2011, 10, TUESDAY, date(2011, 9, 27)), Array(2011, 11, TUESDAY, date(2011, 10, 25)), Array(2011, 12, TUESDAY, date(2011, 11, 29)))
  }

  @Test(dataProvider = "dayOfWeekInMonth_zero") def test_dayOfWeekInMonth_zero(year: Int, month: Int, dow: DayOfWeek, expected: LocalDate): Unit = {
    var day: Int = 1
    while (day <= Month.of(month).length(false)) {
      val _date: LocalDate = date(year, month, day)
      val test: LocalDate = TemporalAdjusters.dayOfWeekInMonth(0, dow).adjustInto(_date).asInstanceOf[LocalDate]
      assertEquals(test, expected)
      day += 1
    }
  }

  @DataProvider(name = "dayOfWeekInMonth_negative") private[temporal] def data_dayOfWeekInMonth_negative: Array[Array[Any]] = {
    Array[Array[Any]](Array(2011, 1, TUESDAY, date(2011, 1, 25)), Array(2011, 2, TUESDAY, date(2011, 2, 22)), Array(2011, 3, TUESDAY, date(2011, 3, 29)), Array(2011, 4, TUESDAY, date(2011, 4, 26)), Array(2011, 5, TUESDAY, date(2011, 5, 31)), Array(2011, 6, TUESDAY, date(2011, 6, 28)), Array(2011, 7, TUESDAY, date(2011, 7, 26)), Array(2011, 8, TUESDAY, date(2011, 8, 30)), Array(2011, 9, TUESDAY, date(2011, 9, 27)), Array(2011, 10, TUESDAY, date(2011, 10, 25)), Array(2011, 11, TUESDAY, date(2011, 11, 29)), Array(2011, 12, TUESDAY, date(2011, 12, 27)))
  }

  @Test(dataProvider = "dayOfWeekInMonth_negative") def test_dayOfWeekInMonth_negative(year: Int, month: Int, dow: DayOfWeek, expected: LocalDate): Unit = {
    var ordinal: Int = 0
    while (ordinal < 5) {
      var day: Int = 1
      while (day <= Month.of(month).length(false)) {
        val _date: LocalDate = date(year, month, day)
        val test: LocalDate = TemporalAdjusters.dayOfWeekInMonth(-1 - ordinal, dow).adjustInto(_date).asInstanceOf[LocalDate]
        assertEquals(test, expected.minusWeeks(ordinal))
        day += 1
      }
      ordinal += 1
    }
  }

  @Test def factory_firstInMonth(): Unit = {
    assertNotNull(TemporalAdjusters.firstInMonth(MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_firstInMonth_nullDayOfWeek(): Unit = {
    TemporalAdjusters.firstInMonth(null)
  }

  @Test(dataProvider = "dayOfWeekInMonth_positive") def test_firstInMonth(year: Int, month: Int, dow: DayOfWeek, expected: LocalDate): Unit = {
    var day: Int = 1
    while (day <= Month.of(month).length(false)) {
      val _date: LocalDate = date(year, month, day)
      val test: LocalDate = TemporalAdjusters.firstInMonth(dow).adjustInto(_date).asInstanceOf[LocalDate]
      assertEquals(test, expected, "day-of-month=" + day)
      day += 1
    }
  }

  @Test def factory_lastInMonth(): Unit = {
    assertNotNull(TemporalAdjusters.lastInMonth(MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_lastInMonth_nullDayOfWeek(): Unit = {
    TemporalAdjusters.lastInMonth(null)
  }

  @Test(dataProvider = "dayOfWeekInMonth_negative") def test_lastInMonth(year: Int, month: Int, dow: DayOfWeek, expected: LocalDate): Unit = {
    var day: Int = 1
    while (day <= Month.of(month).length(false)) {
      val _date: LocalDate = date(year, month, day)
      val test: LocalDate = TemporalAdjusters.lastInMonth(dow).adjustInto(_date).asInstanceOf[LocalDate]
      assertEquals(test, expected, "day-of-month=" + day)
      day += 1
    }
  }

  @Test def factory_next(): Unit = {
    assertNotNull(TemporalAdjusters.next(MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_next_nullDayOfWeek(): Unit = {
    TemporalAdjusters.next(null)
  }

  @Test def test_next(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        for (dow <- DayOfWeek.values) {
          val test: LocalDate = TemporalAdjusters.next(dow).adjustInto(_date).asInstanceOf[LocalDate]
          assertSame(test.getDayOfWeek, dow, _date + " " + test)
          if (test.getYear == 2007) {
            val dayDiff: Int = test.getDayOfYear - _date.getDayOfYear
            assertTrue(dayDiff > 0 && dayDiff < 8)
          }
          else {
            assertSame(month, Month.DECEMBER)
            assertTrue(_date.getDayOfMonth > 24)
            assertEquals(test.getYear, 2008)
            assertSame(test.getMonth, Month.JANUARY)
            assertTrue(test.getDayOfMonth < 8)
          }
        }
        i += 1
      }
    }
  }

  @Test def factory_nextOrSame(): Unit = {
    assertNotNull(TemporalAdjusters.nextOrSame(MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_nextOrSame_nullDayOfWeek(): Unit = {
    TemporalAdjusters.nextOrSame(null)
  }

  @Test def test_nextOrSame(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        for (dow <- DayOfWeek.values) {
          val test: LocalDate = TemporalAdjusters.nextOrSame(dow).adjustInto(_date).asInstanceOf[LocalDate]
          assertSame(test.getDayOfWeek, dow)
          if (test.getYear == 2007) {
            val dayDiff: Int = test.getDayOfYear - _date.getDayOfYear
            assertTrue(dayDiff < 8)
            assertEquals(_date == test, _date.getDayOfWeek eq dow)
          }
          else {
            assertFalse(_date.getDayOfWeek eq dow)
            assertSame(month, Month.DECEMBER)
            assertTrue(_date.getDayOfMonth > 24)
            assertEquals(test.getYear, 2008)
            assertSame(test.getMonth, Month.JANUARY)
            assertTrue(test.getDayOfMonth < 8)
          }
        }
        i += 1
      }
    }
  }

  @Test def factory_previous(): Unit = {
    assertNotNull(TemporalAdjusters.previous(MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_previous_nullDayOfWeek(): Unit = {
    TemporalAdjusters.previous(null)
  }

  @Test def test_previous(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        for (dow <- DayOfWeek.values) {
          val test: LocalDate = TemporalAdjusters.previous(dow).adjustInto(_date).asInstanceOf[LocalDate]
          assertSame(test.getDayOfWeek, dow, _date + " " + test)
          if (test.getYear == 2007) {
            val dayDiff: Int = test.getDayOfYear - _date.getDayOfYear
            assertTrue(dayDiff < 0 && dayDiff > -8, dayDiff + " " + test)
          }
          else {
            assertSame(month, Month.JANUARY)
            assertTrue(_date.getDayOfMonth < 8)
            assertEquals(test.getYear, 2006)
            assertSame(test.getMonth, Month.DECEMBER)
            assertTrue(test.getDayOfMonth > 24)
          }
        }
        i += 1
      }
    }
  }

  @Test def factory_previousOrSame(): Unit = {
    assertNotNull(TemporalAdjusters.previousOrSame(MONDAY))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_previousOrSame_nullDayOfWeek(): Unit = {
    TemporalAdjusters.previousOrSame(null)
  }

  @Test def test_previousOrSame(): Unit = {
    for (month <- Month.values) {
      var i: Int = 1
      while (i <= month.length(false)) {
        val _date: LocalDate = date(2007, month, i)
        for (dow <- DayOfWeek.values) {
          val test: LocalDate = TemporalAdjusters.previousOrSame(dow).adjustInto(_date).asInstanceOf[LocalDate]
          assertSame(test.getDayOfWeek, dow)
          if (test.getYear == 2007) {
            val dayDiff: Int = test.getDayOfYear - _date.getDayOfYear
            assertTrue(dayDiff <= 0 && dayDiff > -7)
            assertEquals(_date == test, _date.getDayOfWeek eq dow)
          }
          else {
            assertFalse(_date.getDayOfWeek eq dow)
            assertSame(month, Month.JANUARY)
            assertTrue(_date.getDayOfMonth < 7)
            assertEquals(test.getYear, 2006)
            assertSame(test.getMonth, Month.DECEMBER)
            assertTrue(test.getDayOfMonth > 25)
          }
        }
        i += 1
      }
    }
  }

  private def date(year: Int, month: Month, day: Int): LocalDate = LocalDate.of(year, month, day)

  private def date(year: Int, month: Int, day: Int): LocalDate = LocalDate.of(year, month, day)
}
