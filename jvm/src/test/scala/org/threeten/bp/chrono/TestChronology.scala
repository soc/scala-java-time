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
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertSame
import org.testng.Assert.assertTrue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Locale
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.temporal.ChronoField

/** Test Chrono class. */
@Test class TestChronology extends {
  @BeforeMethod def setUp(): Unit = {
    var c: Chronology = null
    c = HijrahChronology.INSTANCE
    c = IsoChronology.INSTANCE
    c = JapaneseChronology.INSTANCE
    c = MinguoChronology.INSTANCE
    c = ThaiBuddhistChronology.INSTANCE
    c.toString
  }

  @DataProvider(name = "calendars") private[chrono] def data_of_calendars: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("Hijrah-umalqura", "islamic-umalqura", "Hijrah calendar"), Array("ISO", "iso8601", "ISO calendar"), Array("Japanese", "japanese", "Japanese calendar"), Array("Minguo", "roc", "Minguo Calendar"), Array("ThaiBuddhist", "buddhist", "ThaiBuddhist calendar"))
  }

  @Test(dataProvider = "calendars") def test_getters(chronoId: String, calendarSystemType: String, description: String): Unit = {
    val chrono: Chronology = Chronology.of(chronoId)
    assertNotNull(chrono, "Required calendar not found by ID: " + chronoId)
    assertEquals(chrono.getId, chronoId)
    assertEquals(chrono.getCalendarType, calendarSystemType)
  }

  @Test(dataProvider = "calendars") def test_required_calendars(chronoId: String, calendarSystemType: String, description: String): Unit = {
    var chrono: Chronology = Chronology.of(chronoId)
    assertNotNull(chrono, "Required calendar not found by ID: " + chronoId)
    chrono = Chronology.of(calendarSystemType)
    assertNotNull(chrono, "Required calendar not found by type: " + chronoId)
    val cals: java.util.Set[Chronology] = Chronology.getAvailableChronologies
    assertTrue(cals.contains(chrono), "Required calendar not found in set of available calendars")
  }

  @Test def test_calendar_list(): Unit = {
    val chronos: java.util.Set[Chronology] = Chronology.getAvailableChronologies
    assertNotNull(chronos, "Required list of calendars must be non-null")
    import scala.collection.JavaConversions._
    for (chrono <- chronos) {
      val lookup: Chronology = Chronology.of(chrono.getId)
      assertNotNull(lookup, "Required calendar not found: " + chrono)
    }
    assertEquals(chronos.size >= data_of_calendars.length, true, "Required list of calendars too short")
  }

  /** Compute the number of days from the Epoch and compute the date from the number of days. */
  @Test(dataProvider = "calendars") def test_epoch(name: String, alias: String, description: String): Unit = {
    val chrono: Chronology = Chronology.of(name)
    val date1: ChronoLocalDate = chrono.dateNow
    val epoch1: Long = date1.getLong(ChronoField.EPOCH_DAY)
    val date2: ChronoLocalDate = date1.`with`(ChronoField.EPOCH_DAY, epoch1)
    assertEquals(date1, date2, "Date from epoch day is not same date: " + date1 + " != " + date2)
    val epoch2: Long = date1.getLong(ChronoField.EPOCH_DAY)
    assertEquals(epoch1, epoch2, "Epoch day not the same: " + epoch1 + " != " + epoch2)
  }

  @DataProvider(name = "calendarsystemtype") private[chrono] def data_CalendarType: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(HijrahChronology.INSTANCE, "islamic-umalqura"), Array(IsoChronology.INSTANCE, "iso8601"), Array(JapaneseChronology.INSTANCE, "japanese"), Array(MinguoChronology.INSTANCE, "roc"), Array(ThaiBuddhistChronology.INSTANCE, "buddhist"))
  }

  @Test(dataProvider = "calendarsystemtype") def test_getCalendarType(chrono: Chronology, calendarType: String): Unit = {
    assertEquals(chrono.getCalendarType, calendarType)
  }

  @Test def test_lookupLocale_jp_JP(): Unit = {
    val test: Chronology = Chronology.ofLocale(new Locale("ja", "JP"))
    Assert.assertEquals(test.getId, "ISO")
    Assert.assertEquals(test, IsoChronology.INSTANCE)
  }

  @Test def test_lookupLocale_jp_JP_JP(): Unit = {
    val test: Chronology = Chronology.ofLocale(new Locale("ja", "JP", "JP"))
    Assert.assertEquals(test.getId, "Japanese")
    Assert.assertEquals(test, JapaneseChronology.INSTANCE)
  }

  @Test(dataProvider = "calendarsystemtype")
  @throws(classOf[Exception])
  def test_chronoSerializationSingleton(chrono: Chronology, calendarType: String): Unit = {
    val orginal: Chronology = chrono
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val out: ObjectOutputStream = new ObjectOutputStream(baos)
    out.writeObject(orginal)
    out.close()
    val bais: ByteArrayInputStream = new ByteArrayInputStream(baos.toByteArray)
    val in: ObjectInputStream = new ObjectInputStream(bais)
    val ser: Chronology = in.readObject.asInstanceOf[Chronology]
    assertSame(ser, chrono, "Deserialized Chrono is not the singleton serialized")
  }
}
