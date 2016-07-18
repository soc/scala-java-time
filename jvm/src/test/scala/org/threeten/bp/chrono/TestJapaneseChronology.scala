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
import org.testng.Assert.fail
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.temporal.TemporalAdjusters

/** Test. */
@Test class TestJapaneseChronology {
  @Test def test_chrono_byName(): Unit = {
    val c: Chronology = JapaneseChronology.INSTANCE
    val test: Chronology = Chronology.of("Japanese")
    Assert.assertNotNull(test, "The Japanese calendar could not be found byName")
    Assert.assertEquals(test.getId, "Japanese", "ID mismatch")
    Assert.assertEquals(test.getCalendarType, "japanese", "Type mismatch")
    Assert.assertEquals(test, c)
  }

  @DataProvider(name = "samples") private[chrono] def data_samples: Array[Array[AnyRef]] =
    Array[Array[AnyRef]](Array(JapaneseChronology.INSTANCE.date(1890, 3, 3), LocalDate.of(1890, 3, 3)), Array(JapaneseChronology.INSTANCE.date(1890, 10, 28), LocalDate.of(1890, 10, 28)), Array(JapaneseChronology.INSTANCE.date(1890, 10, 29), LocalDate.of(1890, 10, 29)))

  @Test(dataProvider = "samples") def test_toLocalDate(jdate: ChronoLocalDate, iso: LocalDate): Unit =
    assertEquals(LocalDate.from(jdate), iso)

  @Test(dataProvider = "samples") def test_fromCalendrical(jdate: ChronoLocalDate, iso: LocalDate): Unit =
    assertEquals(JapaneseChronology.INSTANCE.date(iso), jdate)

  @DataProvider(name = "badDates") private[chrono] def data_badDates: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](1728, 0, 0), Array[Integer](1890, 0, 0), Array[Integer](1890, -1, 1), Array[Integer](1890, 0, 1), Array[Integer](1890, 14, 1), Array[Integer](1890, 15, 1), Array[Integer](1890, 1, -1), Array[Integer](1890, 1, 0), Array[Integer](1890, 1, 32), Array[Integer](1890, 12, -1), Array[Integer](1890, 12, 0), Array[Integer](1890, 12, 32))

  @Test(dataProvider = "badDates", expectedExceptions = Array(classOf[DateTimeException])) def test_badDates(year: Int, month: Int, dom: Int): Unit =
    JapaneseChronology.INSTANCE.date(year, month, dom)

  @Test def test_adjust1(): Unit = {
    val base: ChronoLocalDate = JapaneseChronology.INSTANCE.date(1890, 10, 29)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, JapaneseChronology.INSTANCE.date(1890, 10, 31))
  }

  @Test def test_adjust2(): Unit = {
    val base: ChronoLocalDate = JapaneseChronology.INSTANCE.date(1890, 12, 2)
    val test: ChronoLocalDate = base.`with`(TemporalAdjusters.lastDayOfMonth)
    assertEquals(test, JapaneseChronology.INSTANCE.date(1890, 12, 31))
  }

  @Test def test_adjust_toLocalDate(): Unit = {
    val jdate: ChronoLocalDate = JapaneseChronology.INSTANCE.date(1890, 1, 4)
    val test: ChronoLocalDate = jdate.`with`(LocalDate.of(2012, 7, 6))
    assertEquals(test, JapaneseChronology.INSTANCE.date(2012, 7, 6))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_adjust_toMonth(): Unit = {
    val jdate: ChronoLocalDate = JapaneseChronology.INSTANCE.date(1890, 1, 4)
    jdate.`with`(Month.APRIL)
  }

  @Test def test_LocalDate_adjustToJapaneseDate(): Unit = {
    val jdate: ChronoLocalDate = JapaneseChronology.INSTANCE.date(1890, 10, 29)
    val test: LocalDate = LocalDate.MIN.`with`(jdate)
    assertEquals(test, LocalDate.of(1890, 10, 29))
  }

  @Test def test_LocalDateTime_adjustToJapaneseDate(): Unit = {
    val jdate: ChronoLocalDate = JapaneseChronology.INSTANCE.date(1890, 10, 29)
    val test: LocalDateTime = LocalDateTime.MIN.`with`(jdate)
    assertEquals(test, LocalDateTime.of(1890, 10, 29, 0, 0))
  }

  @DataProvider(name = "japaneseEras") private[chrono] def data_japanseseEras: Array[Array[Any]] = {
    Array[Array[Any]](Array(JapaneseEra.MEIJI, -1, "Meiji"), Array(JapaneseEra.TAISHO, 0, "Taisho"), Array(JapaneseEra.SHOWA, 1, "Showa"), Array(JapaneseEra.HEISEI, 2, "Heisei"))
  }

  @Test(dataProvider = "japaneseEras") def test_Japanese_Eras(era: Era, eraValue: Int, name: String): Unit = {
    assertEquals(era.getValue, eraValue, "EraValue")
    assertEquals(era.toString, name, "Era Name")
    assertEquals(era, JapaneseChronology.INSTANCE.eraOf(eraValue), "JapaneseChrono.eraOf()")
    val eras: java.util.List[Era] = JapaneseChronology.INSTANCE.eras
    assertTrue(eras.contains(era), "Era is not present in JapaneseChrono.INSTANCE.eras()")
  }

  @Test def test_Japanese_badEras(): Unit = {
    val badEras: Array[Int] = Array(-1000, -998, -997, -2, 3, 4, 1000)
    for (badEra <- badEras)
      try {
        val era: Era = JapaneseChronology.INSTANCE.eraOf(badEra)
        fail(s"JapaneseChrono.eraOf returned $era + for invalid eraValue $badEra")
      } catch {
        case ex: DateTimeException =>
      }
  }

  @DataProvider(name = "toString") private[chrono] def data_toString: Array[Array[AnyRef]] =
    Array[Array[AnyRef]](Array(JapaneseChronology.INSTANCE.date(1873, 9, 8), "Japanese Meiji 6-09-08"), Array(JapaneseChronology.INSTANCE.date(1912, 7, 29), "Japanese Meiji 45-07-29"), Array(JapaneseChronology.INSTANCE.date(1912, 7, 30), "Japanese Taisho 1-07-30"), Array(JapaneseChronology.INSTANCE.date(1926, 12, 24), "Japanese Taisho 15-12-24"), Array(JapaneseChronology.INSTANCE.date(1926, 12, 25), "Japanese Showa 1-12-25"), Array(JapaneseChronology.INSTANCE.date(1989, 1, 7), "Japanese Showa 64-01-07"), Array(JapaneseChronology.INSTANCE.date(1989, 1, 8), "Japanese Heisei 1-01-08"), Array(JapaneseChronology.INSTANCE.date(2012, 12, 6), "Japanese Heisei 24-12-06"))

  @Test(dataProvider = "toString") def test_toString(jdate: ChronoLocalDate, expected: String): Unit =
    assertEquals(jdate.toString, expected)

  @Test def test_equals_true(): Unit =
    assertTrue(JapaneseChronology.INSTANCE == JapaneseChronology.INSTANCE)

  @Test def test_equals_false(): Unit =
    assertFalse(JapaneseChronology.INSTANCE == IsoChronology.INSTANCE)
}
