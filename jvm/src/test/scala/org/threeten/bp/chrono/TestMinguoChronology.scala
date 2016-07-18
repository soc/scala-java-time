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
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjusters

/** Test. */
@Test class TestMinguoChronology {
  @Test def test_chrono_byName(): Unit = {
    val c: Chronology = MinguoChronology.INSTANCE
    val test: Chronology = Chronology.of("Minguo")
    Assert.assertNotNull(test, "The Minguo calendar could not be found byName")
    Assert.assertEquals(test.getId, "Minguo", "ID mismatch")
    Assert.assertEquals(test.getCalendarType, "roc", "Type mismatch")
    Assert.assertEquals(test, c)
  }

  @DataProvider(name = "samples") private[chrono] def data_samples: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(MinguoChronology.INSTANCE.date(1, 1, 1), LocalDate.of(1912, 1, 1)), Array(MinguoChronology.INSTANCE.date(1, 1, 2), LocalDate.of(1912, 1, 2)), Array(MinguoChronology.INSTANCE.date(1, 1, 3), LocalDate.of(1912, 1, 3)), Array(MinguoChronology.INSTANCE.date(2, 1, 1), LocalDate.of(1913, 1, 1)), Array(MinguoChronology.INSTANCE.date(3, 1, 1), LocalDate.of(1914, 1, 1)), Array(MinguoChronology.INSTANCE.date(3, 12, 6), LocalDate.of(1914, 12, 6)), Array(MinguoChronology.INSTANCE.date(4, 1, 1), LocalDate.of(1915, 1, 1)), Array(MinguoChronology.INSTANCE.date(4, 7, 3), LocalDate.of(1915, 7, 3)), Array(MinguoChronology.INSTANCE.date(4, 7, 4), LocalDate.of(1915, 7, 4)), Array(MinguoChronology.INSTANCE.date(5, 1, 1), LocalDate.of(1916, 1, 1)), Array(MinguoChronology.INSTANCE.date(100, 3, 3), LocalDate.of(2011, 3, 3)), Array(MinguoChronology.INSTANCE.date(101, 10, 28), LocalDate.of(2012, 10, 28)), Array(MinguoChronology.INSTANCE.date(101, 10, 29), LocalDate.of(2012, 10, 29)))
  }

  @Test(dataProvider = "samples") def test_toLocalDate(minguo: ChronoLocalDate, iso: LocalDate): Unit = {
    assertEquals(LocalDate.from(minguo), iso)
  }

  @Test(dataProvider = "samples") def test_fromCalendrical(minguo: ChronoLocalDate, iso: LocalDate): Unit = {
    assertEquals(MinguoChronology.INSTANCE.date(iso), minguo)
  }

  @SuppressWarnings(Array("unused"))
  @Test(dataProvider = "samples") def test_MinguoDate(minguoDate: ChronoLocalDate, iso: LocalDate): Unit = {
    val hd: ChronoLocalDate = minguoDate
    var hdt: ChronoLocalDateTime[_ <: ChronoLocalDate] = hd.atTime(LocalTime.NOON)
    val zo: ZoneOffset = ZoneOffset.ofHours(1)
    val hzdt: ChronoZonedDateTime[_ <: ChronoLocalDate] = hdt.atZone(zo)
    hdt = hdt.plus(1, ChronoUnit.YEARS)
    hdt = hdt.plus(1, ChronoUnit.MONTHS)
    hdt = hdt.plus(1, ChronoUnit.DAYS)
    hdt = hdt.plus(1, ChronoUnit.HOURS)
    hdt = hdt.plus(1, ChronoUnit.MINUTES)
    hdt = hdt.plus(1, ChronoUnit.SECONDS)
    hdt = hdt.plus(1, ChronoUnit.NANOS)
    val a2: ChronoLocalDateTime[_ <: ChronoLocalDate] = hzdt.toLocalDateTime
    val a3: ChronoLocalDate = a2.toLocalDate
    val a5: ChronoLocalDate = hzdt.toLocalDate
  }

  @Test def test_MinguoChrono(): Unit = {
    val h1: ChronoLocalDate = MinguoChronology.INSTANCE.date(MinguoEra.ROC, 1, 2, 3)
    val h2: ChronoLocalDate = h1
    val h3: ChronoLocalDateTime[_] = h2.atTime(LocalTime.NOON)
    @SuppressWarnings(Array("unused")) val h4: ChronoZonedDateTime[_] = h3.atZone(ZoneOffset.UTC)
  }

  @DataProvider(name = "badDates") private[chrono] def data_badDates: Array[Array[Any]] = {
    Array[Array[Any]](Array(1912, 0, 0), Array(1912, -1, 1), Array(1912, 0, 1), Array(1912, 14, 1), Array(1912, 15, 1), Array(1912, 1, -1), Array(1912, 1, 0), Array(1912, 1, 32), Array(1912, 2, 29), Array(1912, 2, 30), Array(1912, 12, -1), Array(1912, 12, 0), Array(1912, 12, 32))
  }

  @Test(dataProvider = "badDates", expectedExceptions = Array(classOf[DateTimeException])) def test_badDates(year: Int, month: Int, dom: Int): Unit = {
    MinguoChronology.INSTANCE.date(year, month, dom)
  }

  @Test def test_adjust1(): Unit = {
    val base: ChronoLocalDate = MinguoChronology.INSTANCE.date(2012, 10, 29)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, MinguoChronology.INSTANCE.date(2012, 10, 31))
  }

  @Test def test_adjust2(): Unit = {
    val base: ChronoLocalDate = MinguoChronology.INSTANCE.date(1728, 12, 2)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, MinguoChronology.INSTANCE.date(1728, 12, 31))
  }

  @Test def test_adjust_toLocalDate(): Unit = {
    val minguo: ChronoLocalDate = MinguoChronology.INSTANCE.date(99, 1, 4)
    val test: ChronoLocalDate = minguo.`with`(LocalDate.of(2012, 7, 6))
    assertEquals(test, MinguoChronology.INSTANCE.date(101, 7, 6))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_adjust_toMonth(): Unit = {
    val minguo: ChronoLocalDate = MinguoChronology.INSTANCE.date(1726, 1, 4)
    minguo.`with`(Month.APRIL)
  }

  @Test def test_LocalDate_adjustToMinguoDate(): Unit = {
    val minguo: ChronoLocalDate = MinguoChronology.INSTANCE.date(101, 10, 29)
    val test: LocalDate = LocalDate.MIN.`with`(minguo)
    assertEquals(test, LocalDate.of(2012, 10, 29))
  }

  @Test def test_LocalDateTime_adjustToMinguoDate(): Unit = {
    val minguo: ChronoLocalDate = MinguoChronology.INSTANCE.date(101, 10, 29)
    val test: LocalDateTime = LocalDateTime.MIN.`with`(minguo)
    assertEquals(test, LocalDateTime.of(2012, 10, 29, 0, 0))
  }

  @DataProvider(name = "toString") private[chrono] def data_toString: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(MinguoChronology.INSTANCE.date(1, 1, 1), "Minguo ROC 1-01-01"), Array(MinguoChronology.INSTANCE.date(1728, 10, 28), "Minguo ROC 1728-10-28"), Array(MinguoChronology.INSTANCE.date(1728, 10, 29), "Minguo ROC 1728-10-29"), Array(MinguoChronology.INSTANCE.date(1727, 12, 5), "Minguo ROC 1727-12-05"), Array(MinguoChronology.INSTANCE.date(1727, 12, 6), "Minguo ROC 1727-12-06"))
  }

  @Test(dataProvider = "toString") def test_toString(minguo: ChronoLocalDate, expected: String): Unit = {
    assertEquals(minguo.toString, expected)
  }

  @Test def test_equals_true(): Unit = {
    assertTrue(MinguoChronology.INSTANCE == MinguoChronology.INSTANCE)
  }

  @Test def test_equals_false(): Unit = {
    assertFalse(MinguoChronology.INSTANCE == IsoChronology.INSTANCE)
  }
}
