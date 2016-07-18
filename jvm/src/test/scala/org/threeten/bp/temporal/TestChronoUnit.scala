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
import org.threeten.bp.Month.AUGUST
import org.threeten.bp.Month.FEBRUARY
import org.threeten.bp.Month.JULY
import org.threeten.bp.Month.JUNE
import org.threeten.bp.Month.MARCH
import org.threeten.bp.Month.OCTOBER
import org.threeten.bp.Month.SEPTEMBER
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.FOREVER
import org.threeten.bp.temporal.ChronoUnit.MONTHS
import org.threeten.bp.temporal.ChronoUnit.WEEKS
import org.threeten.bp.temporal.ChronoUnit.YEARS
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset

/** Test. */
object TestChronoUnit {
  private def date(year: Int, month: Month, dom: Int): LocalDate = LocalDate.of(year, month, dom)
}

@Test class TestChronoUnit {
  @DataProvider(name = "yearsBetween") private[temporal] def data_yearsBetween: Array[Array[Any]] = {
    Array[Array[Any]](Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1939, SEPTEMBER, 1), 0), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1939, SEPTEMBER, 2), 0), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1939, SEPTEMBER, 3), 0), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1940, SEPTEMBER, 1), 0), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1940, SEPTEMBER, 2), 1), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1940, SEPTEMBER, 3), 1), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1938, SEPTEMBER, 1), -1), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1938, SEPTEMBER, 2), -1), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1938, SEPTEMBER, 3), 0), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1945, SEPTEMBER, 3), 6), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1945, OCTOBER, 3), 6), Array(TestChronoUnit.date(1939, SEPTEMBER, 2), TestChronoUnit.date(1945, AUGUST, 3), 5))
  }

  @Test(dataProvider = "yearsBetween") def test_yearsBetween(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(YEARS.between(start, end), expected)
  }

  @Test(dataProvider = "yearsBetween") def test_yearsBetweenReversed(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(YEARS.between(end, start), -expected)
  }

  @Test(dataProvider = "yearsBetween") def test_yearsBetween_LocalDateTimeSameTime(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(YEARS.between(start.atTime(12, 30), end.atTime(12, 30)), expected)
  }

  @Test(dataProvider = "yearsBetween") def test_yearsBetween_LocalDateTimeLaterTime(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    if (end.isAfter(start)) {
      assertEquals(YEARS.between(start.atTime(12, 30), end.atTime(12, 31)), expected)
    }
    else {
      assertEquals(YEARS.between(start.atTime(12, 31), end.atTime(12, 30)), expected)
    }
  }

  @Test(dataProvider = "yearsBetween") def test_yearsBetween_ZonedDateSameOffset(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(YEARS.between(start.atStartOfDay(ZoneOffset.ofHours(2)), end.atStartOfDay(ZoneOffset.ofHours(2))), expected)
  }

  @Test(dataProvider = "yearsBetween") def test_yearsBetween_ZonedDateLaterOffset(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    if (end.isAfter(start)) {
      assertEquals(YEARS.between(start.atStartOfDay(ZoneOffset.ofHours(2)), end.atStartOfDay(ZoneOffset.ofHours(1))), expected)
    }
    else {
      assertEquals(YEARS.between(start.atStartOfDay(ZoneOffset.ofHours(1)), end.atStartOfDay(ZoneOffset.ofHours(2))), expected)
    }
  }

  @DataProvider(name = "monthsBetween") private[temporal] def data_monthsBetween: Array[Array[Any]] = {
    Array[Array[Any]](Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 1), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 2), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 3), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, AUGUST, 1), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, AUGUST, 2), 1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, AUGUST, 3), 1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, SEPTEMBER, 1), 1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, SEPTEMBER, 2), 2), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, SEPTEMBER, 3), 2), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JUNE, 1), -1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JUNE, 2), -1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JUNE, 3), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 27), TestChronoUnit.date(2012, MARCH, 26), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 27), TestChronoUnit.date(2012, MARCH, 27), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 27), TestChronoUnit.date(2012, MARCH, 28), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 27), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 28), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 29), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 28), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 29), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 30), 1))
  }

  @Test(dataProvider = "monthsBetween") def test_monthsBetween(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(MONTHS.between(start, end), expected)
  }

  @Test(dataProvider = "monthsBetween") def test_monthsBetweenReversed(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(MONTHS.between(end, start), -expected)
  }

  @Test(dataProvider = "monthsBetween") def test_monthsBetween_LocalDateTimeSameTime(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(MONTHS.between(start.atTime(12, 30), end.atTime(12, 30)), expected)
  }

  @Test(dataProvider = "monthsBetween") def test_monthsBetween_LocalDateTimeLaterTime(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    if (end.isAfter(start)) {
      assertEquals(MONTHS.between(start.atTime(12, 30), end.atTime(12, 31)), expected)
    }
    else {
      assertEquals(MONTHS.between(start.atTime(12, 31), end.atTime(12, 30)), expected)
    }
  }

  @Test(dataProvider = "monthsBetween") def test_monthsBetween_ZonedDateSameOffset(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(MONTHS.between(start.atStartOfDay(ZoneOffset.ofHours(2)), end.atStartOfDay(ZoneOffset.ofHours(2))), expected)
  }

  @Test(dataProvider = "monthsBetween") def test_monthsBetween_ZonedDateLaterOffset(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    if (end.isAfter(start)) {
      assertEquals(MONTHS.between(start.atStartOfDay(ZoneOffset.ofHours(2)), end.atStartOfDay(ZoneOffset.ofHours(1))), expected)
    }
    else {
      assertEquals(MONTHS.between(start.atStartOfDay(ZoneOffset.ofHours(1)), end.atStartOfDay(ZoneOffset.ofHours(2))), expected)
    }
  }

  @DataProvider(name = "weeksBetween") private[temporal] def data_weeksBetween: Array[Array[Any]] = {
    Array[Array[Any]](Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JUNE, 25), -1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JUNE, 26), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 2), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 8), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 9), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 21), -1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 22), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 28), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 29), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 1), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 5), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 6), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 22), -1), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 23), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 28), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 29), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 1), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 6), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 7), 1))
  }

  @Test(dataProvider = "weeksBetween") def test_weeksBetween(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(WEEKS.between(start, end), expected)
  }

  @Test(dataProvider = "weeksBetween") def test_weeksBetweenReversed(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(WEEKS.between(end, start), -expected)
  }

  @DataProvider(name = "daysBetween") private[temporal] def data_daysBetween: Array[Array[Any]] = {
    Array[Array[Any]](Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 1), -1), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 2), 0), Array(TestChronoUnit.date(2012, JULY, 2), TestChronoUnit.date(2012, JULY, 3), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 27), -1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 28), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, FEBRUARY, 29), 1), Array(TestChronoUnit.date(2012, FEBRUARY, 28), TestChronoUnit.date(2012, MARCH, 1), 2), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 27), -2), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 28), -1), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, FEBRUARY, 29), 0), Array(TestChronoUnit.date(2012, FEBRUARY, 29), TestChronoUnit.date(2012, MARCH, 1), 1), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2012, FEBRUARY, 27), -3), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2012, FEBRUARY, 28), -2), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2012, FEBRUARY, 29), -1), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2012, MARCH, 1), 0), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2012, MARCH, 2), 1), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2013, FEBRUARY, 28), 364), Array(TestChronoUnit.date(2012, MARCH, 1), TestChronoUnit.date(2013, MARCH, 1), 365), Array(TestChronoUnit.date(2011, MARCH, 1), TestChronoUnit.date(2012, FEBRUARY, 28), 364), Array(TestChronoUnit.date(2011, MARCH, 1), TestChronoUnit.date(2012, FEBRUARY, 29), 365), Array(TestChronoUnit.date(2011, MARCH, 1), TestChronoUnit.date(2012, MARCH, 1), 366))
  }

  @Test(dataProvider = "daysBetween") def test_daysBetween(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(DAYS.between(start, end), expected)
  }

  @Test(dataProvider = "daysBetween") def test_daysBetweenReversed(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(DAYS.between(end, start), -expected)
  }

  @Test(dataProvider = "daysBetween") def test_daysBetween_LocalDateTimeSameTime(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(DAYS.between(start.atTime(12, 30), end.atTime(12, 30)), expected)
  }

  @Test(dataProvider = "daysBetween") def test_daysBetween_LocalDateTimeLaterTime(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    if (end.isAfter(start)) {
      assertEquals(DAYS.between(start.atTime(12, 30), end.atTime(12, 31)), expected)
    }
    else {
      assertEquals(DAYS.between(start.atTime(12, 31), end.atTime(12, 30)), expected)
    }
  }

  @Test(dataProvider = "daysBetween") def test_daysBetween_ZonedDateSameOffset(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(DAYS.between(start.atStartOfDay(ZoneOffset.ofHours(2)), end.atStartOfDay(ZoneOffset.ofHours(2))), expected)
  }

  @Test(dataProvider = "daysBetween") def test_daysBetween_ZonedDateLaterOffset(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    if (end.isAfter(start)) {
      assertEquals(DAYS.between(start.atStartOfDay(ZoneOffset.ofHours(2)), end.atStartOfDay(ZoneOffset.ofHours(1))), expected)
    }
    else {
      assertEquals(DAYS.between(start.atStartOfDay(ZoneOffset.ofHours(1)), end.atStartOfDay(ZoneOffset.ofHours(2))), expected)
    }
  }

  @Test def test_isDateBased(): Unit = {
    import scala.collection.JavaConversions._
    for (unit <- ChronoUnit.values) {
      if (unit.getDuration.getSeconds < 86400) {
        assertEquals(unit.isDateBased, false)
      }
      else if (unit eq FOREVER) {
        assertEquals(unit.isDateBased, false)
      }
      else {
        assertEquals(unit.isDateBased, true)
      }
    }
  }

  @Test def test_isTimeBased(): Unit = {
    import scala.collection.JavaConversions._
    for (unit <- ChronoUnit.values) {
      if (unit.getDuration.getSeconds < 86400) {
        assertEquals(unit.isTimeBased, true)
      }
      else if (unit eq FOREVER) {
        assertEquals(unit.isTimeBased, false)
      }
      else {
        assertEquals(unit.isTimeBased, false)
      }
    }
  }
}
