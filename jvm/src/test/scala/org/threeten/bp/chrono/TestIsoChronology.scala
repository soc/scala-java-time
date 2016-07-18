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
package org.threeten.bp.chrono

import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertTrue
import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.YEAR
import org.threeten.bp.temporal.ChronoField.YEAR_OF_ERA
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.TemporalAdjusters

/** Test. */
@Test class TestIsoChronology {
  @Test def test_chrono_byName(): Unit = {
    val c: Chronology = IsoChronology.INSTANCE
    val test: Chronology = Chronology.of("ISO")
    Assert.assertNotNull(test, "The ISO calendar could not be found byName")
    Assert.assertEquals(test.getId, "ISO", "ID mismatch")
    Assert.assertEquals(test.getCalendarType, "iso8601", "Type mismatch")
    Assert.assertEquals(test, c)
  }

  @Test def instanceNotNull(): Unit =
    assertNotNull(IsoChronology.INSTANCE)

  @Test def test_eraOf(): Unit = {
    assertEquals(IsoChronology.INSTANCE.eraOf(0), IsoEra.BCE)
    assertEquals(IsoChronology.INSTANCE.eraOf(1), IsoEra.CE)
  }

  @DataProvider(name = "samples") private[chrono] def data_samples: Array[Array[AnyRef]] =
    Array[Array[AnyRef]](Array(IsoChronology.INSTANCE.date(1, 7, 8), LocalDate.of(1, 7, 8)), Array(IsoChronology.INSTANCE.date(1, 7, 20), LocalDate.of(1, 7, 20)), Array(IsoChronology.INSTANCE.date(1, 7, 21), LocalDate.of(1, 7, 21)), Array(IsoChronology.INSTANCE.date(2, 7, 8), LocalDate.of(2, 7, 8)), Array(IsoChronology.INSTANCE.date(3, 6, 27), LocalDate.of(3, 6, 27)), Array(IsoChronology.INSTANCE.date(3, 5, 23), LocalDate.of(3, 5, 23)), Array(IsoChronology.INSTANCE.date(4, 6, 16), LocalDate.of(4, 6, 16)), Array(IsoChronology.INSTANCE.date(4, 7, 3), LocalDate.of(4, 7, 3)), Array(IsoChronology.INSTANCE.date(4, 7, 4), LocalDate.of(4, 7, 4)), Array(IsoChronology.INSTANCE.date(5, 1, 1), LocalDate.of(5, 1, 1)), Array(IsoChronology.INSTANCE.date(1727, 3, 3), LocalDate.of(1727, 3, 3)), Array(IsoChronology.INSTANCE.date(1728, 10, 28), LocalDate.of(1728, 10, 28)), Array(IsoChronology.INSTANCE.date(2012, 10, 29), LocalDate.of(2012, 10, 29)))

  @Test(dataProvider = "samples") def test_toLocalDate(isoDate: ChronoLocalDate, iso: LocalDate): Unit =
    assertEquals(LocalDate.from(isoDate), iso)

  @Test(dataProvider = "samples") def test_fromCalendrical(isoDate: ChronoLocalDate, iso: LocalDate): Unit =
    assertEquals(IsoChronology.INSTANCE.date(iso), isoDate)

  @DataProvider(name = "badDates") private[chrono] def data_badDates: Array[Array[Any]] =
    Array[Array[Any]](Array(2012, 0, 0), Array(2012, -1, 1), Array(2012, 0, 1), Array(2012, 14, 1), Array(2012, 15, 1), Array(2012, 1, -1), Array(2012, 1, 0), Array(2012, 1, 32), Array(2012, 12, -1), Array(2012, 12, 0), Array(2012, 12, 32))

  @Test(dataProvider = "badDates", expectedExceptions = Array(classOf[DateTimeException])) def test_badDates(year: Int, month: Int, dom: Int): Unit =
    IsoChronology.INSTANCE.date(year, month, dom)

  @Test def test_date_withEra(): Unit = {
    val year: Int = 5
    val month: Int = 5
    val dayOfMonth: Int = 5
    val test: ChronoLocalDate = IsoChronology.INSTANCE.date(IsoEra.BCE, year, month, dayOfMonth)
    assertEquals(test.getEra, IsoEra.BCE)
    assertEquals(test.get(ChronoField.YEAR_OF_ERA), year)
    assertEquals(test.get(ChronoField.MONTH_OF_YEAR), month)
    assertEquals(test.get(ChronoField.DAY_OF_MONTH), dayOfMonth)
    assertEquals(test.get(YEAR), 1 + (-1 * year))
    assertEquals(test.get(ERA), 0)
    assertEquals(test.get(YEAR_OF_ERA), year)
  }

  @Test(expectedExceptions = Array(classOf[ClassCastException])) def test_date_withEra_withWrongEra(): Unit =
    IsoChronology.INSTANCE.date(HijrahEra.AH.asInstanceOf[Era], 1, 1, 1)

  @Test def test_adjust1(): Unit = {
    val base: ChronoLocalDate = IsoChronology.INSTANCE.date(1728, 10, 28)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, IsoChronology.INSTANCE.date(1728, 10, 31))
  }

  @Test def test_adjust2(): Unit = {
    val base: ChronoLocalDate = IsoChronology.INSTANCE.date(1728, 12, 2)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, IsoChronology.INSTANCE.date(1728, 12, 31))
  }

  @Test def test_adjust_toLocalDate(): Unit = {
    val isoDate: ChronoLocalDate = IsoChronology.INSTANCE.date(1726, 1, 4)
    val test: ChronoLocalDate = isoDate.`with`(LocalDate.of(2012, 7, 6))
    assertEquals(test, IsoChronology.INSTANCE.date(2012, 7, 6))
  }

  @Test def test_adjust_toMonth(): Unit = {
    val isoDate: ChronoLocalDate = IsoChronology.INSTANCE.date(1726, 1, 4)
    assertEquals(IsoChronology.INSTANCE.date(1726, 4, 4), isoDate.`with`(Month.APRIL))
  }

  @Test def test_LocalDate_adjustToISODate(): Unit = {
    val isoDate: ChronoLocalDate = IsoChronology.INSTANCE.date(1728, 10, 29)
    val test: LocalDate = LocalDate.MIN.`with`(isoDate)
    assertEquals(test, LocalDate.of(1728, 10, 29))
  }

  @Test def test_LocalDateTime_adjustToISODate(): Unit = {
    val isoDate: ChronoLocalDate = IsoChronology.INSTANCE.date(1728, 10, 29)
    val test: LocalDateTime = LocalDateTime.MIN.`with`(isoDate)
    assertEquals(test, LocalDateTime.of(1728, 10, 29, 0, 0))
  }

  @DataProvider(name = "leapYears") private[chrono] def leapYearInformation: Array[Array[Any]] =
    Array[Array[Any]](Array(2000, true), Array(1996, true), Array(1600, true), Array(1900, false), Array(2100, false), Array(-500, false), Array(-400, true), Array(-300, false), Array(-100, false), Array(-5, false), Array(-4, true), Array(-3, false), Array(-2, false), Array(-1, false), Array(0, true), Array(1, false), Array(3, false), Array(4, true), Array(5, false), Array(100, false), Array(300, false), Array(400, true), Array(500, false))

  @Test(dataProvider = "leapYears") def test_isLeapYear(year: Int, isLeapYear: Boolean): Unit =
    assertEquals(IsoChronology.INSTANCE.isLeapYear(year), isLeapYear)

  @Test def test_now(): Unit =
    assertEquals(LocalDate.from(IsoChronology.INSTANCE.dateNow), LocalDate.now)

  @DataProvider(name = "toString") private[chrono] def data_toString: Array[Array[AnyRef]] =
    Array[Array[AnyRef]](Array(IsoChronology.INSTANCE.date(1, 1, 1), "0001-01-01"), Array(IsoChronology.INSTANCE.date(1728, 10, 28), "1728-10-28"), Array(IsoChronology.INSTANCE.date(1728, 10, 29), "1728-10-29"), Array(IsoChronology.INSTANCE.date(1727, 12, 5), "1727-12-05"), Array(IsoChronology.INSTANCE.date(1727, 12, 6), "1727-12-06"))

  @Test(dataProvider = "toString") def test_toString(isoDate: ChronoLocalDate, expected: String): Unit =
    assertEquals(isoDate.toString, expected)

  @Test def test_equals_true(): Unit =
    assertTrue(IsoChronology.INSTANCE == IsoChronology.INSTANCE)

  @Test def test_equals_false(): Unit =
    assertFalse(IsoChronology.INSTANCE == HijrahChronology.INSTANCE)
}
