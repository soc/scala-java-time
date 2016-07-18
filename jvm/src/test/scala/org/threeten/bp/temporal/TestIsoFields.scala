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
import org.threeten.bp.DayOfWeek.FRIDAY
import org.threeten.bp.DayOfWeek.MONDAY
import org.threeten.bp.DayOfWeek.SATURDAY
import org.threeten.bp.DayOfWeek.SUNDAY
import org.threeten.bp.DayOfWeek.THURSDAY
import org.threeten.bp.DayOfWeek.TUESDAY
import org.threeten.bp.DayOfWeek.WEDNESDAY
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder

/** Test. */
@Test class TestIsoFields {
  def test_enum(): Unit = {
    assertTrue(IsoFields.WEEK_OF_WEEK_BASED_YEAR.isInstanceOf[Enum[_]])
    assertTrue(IsoFields.WEEK_BASED_YEAR.isInstanceOf[Enum[_]])
    assertTrue(IsoFields.WEEK_BASED_YEARS.isInstanceOf[Enum[_]])
  }

  @DataProvider(name = "week") private[temporal] def data_week: Array[Array[Any]] = {
    Array[Array[Any]](Array(LocalDate.of(1969, 12, 29), MONDAY, 1, 1970), Array(LocalDate.of(2012, 12, 23), SUNDAY, 51, 2012), Array(LocalDate.of(2012, 12, 24), MONDAY, 52, 2012), Array(LocalDate.of(2012, 12, 27), THURSDAY, 52, 2012), Array(LocalDate.of(2012, 12, 28), FRIDAY, 52, 2012), Array(LocalDate.of(2012, 12, 29), SATURDAY, 52, 2012), Array(LocalDate.of(2012, 12, 30), SUNDAY, 52, 2012), Array(LocalDate.of(2012, 12, 31), MONDAY, 1, 2013), Array(LocalDate.of(2013, 1, 1), TUESDAY, 1, 2013), Array(LocalDate.of(2013, 1, 2), WEDNESDAY, 1, 2013), Array(LocalDate.of(2013, 1, 6), SUNDAY, 1, 2013), Array(LocalDate.of(2013, 1, 7), MONDAY, 2, 2013))
  }

  @Test(dataProvider = "week") def test_WOWBY(date: LocalDate, dow: DayOfWeek, week: Int, wby: Int): Unit = {
    assertEquals(date.getDayOfWeek, dow)
    assertEquals(IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(date), week)
    assertEquals(date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), week)
  }

  @Test(dataProvider = "week") def test_WBY(date: LocalDate, dow: DayOfWeek, week: Int, wby: Int): Unit = {
    assertEquals(date.getDayOfWeek, dow)
    assertEquals(IsoFields.WEEK_BASED_YEAR.getFrom(date), wby)
    assertEquals(date.get(IsoFields.WEEK_BASED_YEAR), wby)
  }

  @Test(dataProvider = "week") def test_parse_weeks(date: LocalDate, dow: DayOfWeek, week: Int, wby: Int): Unit = {
    val f: DateTimeFormatter = new DateTimeFormatterBuilder().appendValue(IsoFields.WEEK_BASED_YEAR).appendLiteral('-').appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR).appendLiteral('-').appendValue(DAY_OF_WEEK).toFormatter
    val parsed: LocalDate = LocalDate.parse(wby + "-" + week + "-" + dow.getValue, f)
    assertEquals(parsed, date)
  }

  def test_loop(): Unit = {
    var date: LocalDate = LocalDate.of(1960, 1, 5)
    var year: Int = 1960
    var wby: Int = 1960
    var weekLen: Int = 52
    var week: Int = 1
    while (date.getYear < 2400) {
      val loopDow: DayOfWeek = date.getDayOfWeek
      if (date.getYear != year) {
        year = date.getYear
      }
      if (loopDow eq MONDAY) {
        week += 1
        if ((week == 53 && weekLen == 52) || week == 54) {
          week = 1
          val firstDayOfWeekBasedYear: LocalDate = date.plusDays(14).withDayOfYear(1)
          val firstDay: DayOfWeek = firstDayOfWeekBasedYear.getDayOfWeek
          weekLen = if ((firstDay eq THURSDAY) || ((firstDay eq WEDNESDAY) && firstDayOfWeekBasedYear.isLeapYear)) 53 else 52
          wby += 1
        }
      }
      assertEquals(IsoFields.WEEK_OF_WEEK_BASED_YEAR.rangeRefinedBy(date), ValueRange.of(1, weekLen), "Failed on " + date + " " + date.getDayOfWeek)
      assertEquals(IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(date), week, "Failed on " + date + " " + date.getDayOfWeek)
      assertEquals(date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), week, "Failed on " + date + " " + date.getDayOfWeek)
      assertEquals(IsoFields.WEEK_BASED_YEAR.getFrom(date), wby, "Failed on " + date + " " + date.getDayOfWeek)
      assertEquals(date.get(IsoFields.WEEK_BASED_YEAR), wby, "Failed on " + date + " " + date.getDayOfWeek)
      date = date.plusDays(1)
    }
  }

  @DataProvider(name = "quartersBetween") private[temporal] def data_quartersBetween: Array[Array[Any]] = {
    Array[Array[Any]](Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 1), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 2, 1), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 3, 1), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 3, 31), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 4, 1), 1), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 4, 2), 1), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 6, 30), 1), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 7, 1), 2), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 10, 1), 3), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 12, 31), 3), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2001, 1, 1), 4), Array(LocalDate.of(2000, 1, 1), LocalDate.of(2002, 1, 1), 8), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 12, 31), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 10, 2), 0), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 10, 1), -1), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 7, 2), -1), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 7, 1), -2), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 4, 2), -2), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 4, 1), -3), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 1, 2), -3), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 1, 1), -4), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1998, 12, 31), -4), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1998, 10, 2), -4), Array(LocalDate.of(2000, 1, 1), LocalDate.of(1998, 10, 1), -5))
  }

  @Test(dataProvider = "quartersBetween") def test_quarters_between(start: LocalDate, end: LocalDate, expected: Long): Unit = {
    assertEquals(IsoFields.QUARTER_YEARS.between(start, end), expected)
  }
}
