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
import org.testng.Assert.assertTrue
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.temporal.TemporalAdjusters

/** Test. */
@Test class TestHijrahChronology {
  @Test def test_chrono_byName(): Unit = {
    val c: Chronology = HijrahChronology.INSTANCE
    val test: Chronology = Chronology.of("Hijrah")
    Assert.assertNotNull(test, "The Hijrah calendar could not be found byName")
    Assert.assertEquals(test.getId, "Hijrah-umalqura", "ID mismatch")
    Assert.assertEquals(test.getCalendarType, "islamic-umalqura", "Type mismatch")
    Assert.assertEquals(test, c)
  }

  @DataProvider(name = "samples") private[chrono] def data_samples: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(HijrahChronology.INSTANCE.date(1, 1, 1), LocalDate.of(622, 7, 19)), Array(HijrahChronology.INSTANCE.date(1, 1, 2), LocalDate.of(622, 7, 20)), Array(HijrahChronology.INSTANCE.date(1, 1, 3), LocalDate.of(622, 7, 21)), Array(HijrahChronology.INSTANCE.date(2, 1, 1), LocalDate.of(623, 7, 8)), Array(HijrahChronology.INSTANCE.date(3, 1, 1), LocalDate.of(624, 6, 27)), Array(HijrahChronology.INSTANCE.date(3, 12, 6), LocalDate.of(625, 5, 23)), Array(HijrahChronology.INSTANCE.date(4, 1, 1), LocalDate.of(625, 6, 16)), Array(HijrahChronology.INSTANCE.date(4, 7, 3), LocalDate.of(625, 12, 12)), Array(HijrahChronology.INSTANCE.date(4, 7, 4), LocalDate.of(625, 12, 13)), Array(HijrahChronology.INSTANCE.date(5, 1, 1), LocalDate.of(626, 6, 5)), Array(HijrahChronology.INSTANCE.date(1662, 3, 3), LocalDate.of(2234, 4, 3)), Array(HijrahChronology.INSTANCE.date(1728, 10, 28), LocalDate.of(2298, 12, 3)), Array(HijrahChronology.INSTANCE.date(1728, 10, 29), LocalDate.of(2298, 12, 4)))
  }

  @Test(dataProvider = "samples") def test_toLocalDate(hijrahDate: ChronoLocalDate, iso: LocalDate): Unit = {
    assertEquals(LocalDate.from(hijrahDate), iso)
  }

  @Test(dataProvider = "samples") def test_fromCalendrical(hijrahDate: ChronoLocalDate, iso: LocalDate): Unit = {
    assertEquals(HijrahChronology.INSTANCE.date(iso), hijrahDate)
  }

  @DataProvider(name = "badDates") private[chrono] def data_badDates: Array[Array[Any]] = {
    Array[Array[Any]](Array(1728, 0, 0), Array(1728, -1, 1), Array(1728, 0, 1), Array(1728, 14, 1), Array(1728, 15, 1), Array(1728, 1, -1), Array(1728, 1, 0), Array(1728, 1, 32), Array(1728, 12, -1), Array(1728, 12, 0), Array(1728, 12, 32))
  }

  @Test(dataProvider = "badDates", expectedExceptions = Array(classOf[DateTimeException])) def test_badDates(year: Int, month: Int, dom: Int): Unit = {
    HijrahChronology.INSTANCE.date(year, month, dom)
  }

  @Test def test_adjust1(): Unit = {
    val base: ChronoLocalDate = HijrahChronology.INSTANCE.date(1728, 10, 28)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, HijrahChronology.INSTANCE.date(1728, 10, 29))
  }

  @Test def test_adjust2(): Unit = {
    val base: ChronoLocalDate = HijrahChronology.INSTANCE.date(1728, 12, 2)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, HijrahChronology.INSTANCE.date(1728, 12, 30))
  }

  @Test def test_adjust_toLocalDate(): Unit = {
    val hijrahDate: ChronoLocalDate = HijrahChronology.INSTANCE.date(1726, 1, 4)
    val test: ChronoLocalDate = hijrahDate.`with`(LocalDate.of(2012, 7, 6))
    assertEquals(test, HijrahChronology.INSTANCE.date(1433, 8, 16))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_adjust_toMonth(): Unit = {
    val hijrahDate: ChronoLocalDate = HijrahChronology.INSTANCE.date(1726, 1, 4)
    hijrahDate.`with`(Month.APRIL)
  }

  @Test def test_LocalDate_adjustToHijrahDate(): Unit = {
    val hijrahDate: ChronoLocalDate = HijrahChronology.INSTANCE.date(1728, 10, 29)
    val test: LocalDate = LocalDate.MIN.`with`(hijrahDate)
    assertEquals(test, LocalDate.of(2298, 12, 4))
  }

  @Test def test_LocalDateTime_adjustToHijrahDate(): Unit = {
    val hijrahDate: ChronoLocalDate = HijrahChronology.INSTANCE.date(1728, 10, 29)
    val test: LocalDateTime = LocalDateTime.MIN.`with`(hijrahDate)
    assertEquals(test, LocalDateTime.of(2298, 12, 4, 0, 0))
  }

  @DataProvider(name = "toString") private[chrono] def data_toString: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(HijrahChronology.INSTANCE.date(1, 1, 1), "Hijrah-umalqura AH 1-01-01"), Array(HijrahChronology.INSTANCE.date(1728, 10, 28), "Hijrah-umalqura AH 1728-10-28"), Array(HijrahChronology.INSTANCE.date(1728, 10, 29), "Hijrah-umalqura AH 1728-10-29"), Array(HijrahChronology.INSTANCE.date(1727, 12, 5), "Hijrah-umalqura AH 1727-12-05"), Array(HijrahChronology.INSTANCE.date(1727, 12, 6), "Hijrah-umalqura AH 1727-12-06"))
  }

  @Test(dataProvider = "toString") def test_toString(hijrahDate: ChronoLocalDate, expected: String): Unit = {
    assertEquals(hijrahDate.toString, expected)
  }

  @Test def test_equals_true(): Unit = {
    assertTrue(HijrahChronology.INSTANCE == HijrahChronology.INSTANCE)
  }

  @Test def test_equals_false(): Unit = {
    assertFalse(HijrahChronology.INSTANCE == IsoChronology.INSTANCE)
  }
}
