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
package org.threeten.bp

import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertSame
import org.testng.Assert.assertTrue
import org.threeten.bp.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH
import org.threeten.bp.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
import org.threeten.bp.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH
import org.threeten.bp.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR
import org.threeten.bp.temporal.ChronoField.AMPM_OF_DAY
import org.threeten.bp.temporal.ChronoField.CLOCK_HOUR_OF_AMPM
import org.threeten.bp.temporal.ChronoField.CLOCK_HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.DAY_OF_YEAR
import org.threeten.bp.temporal.ChronoField.EPOCH_DAY
import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.HOUR_OF_AMPM
import org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.MICRO_OF_DAY
import org.threeten.bp.temporal.ChronoField.MICRO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.MILLI_OF_DAY
import org.threeten.bp.temporal.ChronoField.MILLI_OF_SECOND
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_DAY
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import org.threeten.bp.temporal.ChronoField.NANO_OF_DAY
import org.threeten.bp.temporal.ChronoField.NANO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.PROLEPTIC_MONTH
import org.threeten.bp.temporal.ChronoField.SECOND_OF_DAY
import org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE
import org.threeten.bp.temporal.ChronoField.YEAR
import org.threeten.bp.temporal.ChronoField.YEAR_OF_ERA
import org.threeten.bp.temporal.ChronoUnit.HALF_DAYS
import org.threeten.bp.temporal.ChronoUnit.HOURS
import org.threeten.bp.temporal.ChronoUnit.MICROS
import org.threeten.bp.temporal.ChronoUnit.MILLIS
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import org.threeten.bp.temporal.ChronoUnit.NANOS
import org.threeten.bp.temporal.ChronoUnit.SECONDS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Arrays
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.JulianFields
import org.threeten.bp.temporal.MockFieldNoValue
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries
import org.threeten.bp.temporal.TemporalUnit

/** Test LocalDateTime. */
object TestLocalDateTime {
  private val OFFSET_PONE: ZoneOffset = ZoneOffset.ofHours(1)
  private val OFFSET_PTWO: ZoneOffset = ZoneOffset.ofHours(2)
  private val OFFSET_MTWO: ZoneOffset = ZoneOffset.ofHours(-2)
  private val ZONE_PARIS: ZoneId = ZoneId.of("Europe/Paris")
  private val ZONE_GAZA: ZoneId = ZoneId.of("Asia/Gaza")
}

@Test class TestLocalDateTime extends AbstractDateTimeTest {
  private val TEST_2007_07_15_12_30_40_987654321: LocalDateTime = LocalDateTime.of(2007, 7, 15, 12, 30, 40, 987654321)
  private var MAX_DATE_TIME: LocalDateTime = null
  private var MIN_DATE_TIME: LocalDateTime = null
  private var MAX_INSTANT: Instant = null
  private var MIN_INSTANT: Instant = null

  @BeforeMethod def setUp(): Unit = {
    MAX_DATE_TIME = LocalDateTime.MAX
    MIN_DATE_TIME = LocalDateTime.MIN
    MAX_INSTANT = MAX_DATE_TIME.atZone(ZoneOffset.UTC).toInstant
    MIN_INSTANT = MIN_DATE_TIME.atZone(ZoneOffset.UTC).toInstant
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_2007_07_15_12_30_40_987654321, LocalDateTime.MAX, LocalDateTime.MIN)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(NANO_OF_SECOND, NANO_OF_DAY, MICRO_OF_SECOND, MICRO_OF_DAY, MILLI_OF_SECOND, MILLI_OF_DAY, SECOND_OF_MINUTE, SECOND_OF_DAY, MINUTE_OF_HOUR, MINUTE_OF_DAY, CLOCK_HOUR_OF_AMPM, HOUR_OF_AMPM, CLOCK_HOUR_OF_DAY, HOUR_OF_DAY, AMPM_OF_DAY, DAY_OF_WEEK, ALIGNED_DAY_OF_WEEK_IN_MONTH, ALIGNED_DAY_OF_WEEK_IN_YEAR, DAY_OF_MONTH, DAY_OF_YEAR, EPOCH_DAY, ALIGNED_WEEK_OF_MONTH, ALIGNED_WEEK_OF_YEAR, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR_OF_ERA, YEAR, ERA, JulianFields.JULIAN_DAY, JulianFields.MODIFIED_JULIAN_DAY, JulianFields.RATA_DIE)
    Arrays.asList(array: _*)
  }

  protected def invalidFields: java.util.List[TemporalField] = {
    val list: java.util.List[TemporalField] = new java.util.ArrayList[TemporalField](Arrays.asList[TemporalField](ChronoField.values: _*))
    list.removeAll(validFields)
    list
  }

  private def check(dateTime: LocalDateTime, y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    assertEquals(dateTime.getYear, y)
    assertEquals(dateTime.getMonth.getValue, m)
    assertEquals(dateTime.getDayOfMonth, d)
    assertEquals(dateTime.getHour, h)
    assertEquals(dateTime.getMinute, mi)
    assertEquals(dateTime.getSecond, s)
    assertEquals(dateTime.getNano, n)
  }

  private def createDateMidnight(year: Int, month: Int, day: Int): LocalDateTime = {
    LocalDateTime.of(year, month, day, 0, 0)
  }

  @Test
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_serialization(): Unit = {
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val oos: ObjectOutputStream = new ObjectOutputStream(baos)
    oos.writeObject(TEST_2007_07_15_12_30_40_987654321)
    oos.close()
    val ois: ObjectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    assertEquals(ois.readObject, TEST_2007_07_15_12_30_40_987654321)
  }

  @Test def test_immutable(): Unit = {
    val cls: Class[LocalDateTime] = classOf[LocalDateTime]
    assertTrue(Modifier.isPublic(cls.getModifiers))
    assertTrue(Modifier.isFinal(cls.getModifiers))
    val fields: Array[Field] = cls.getDeclaredFields
    for (field <- fields) {
      if (!field.getName.contains("$")) {
        if (Modifier.isStatic(field.getModifiers)) {
          assertTrue(Modifier.isFinal(field.getModifiers), "Field:" + field.getName)
        }
        else {
          assertTrue(Modifier.isPrivate(field.getModifiers), "Field:" + field.getName)
          assertTrue(Modifier.isFinal(field.getModifiers), "Field:" + field.getName)
        }
      }
    }
  }

  @Test(timeOut = 30000) def now(): Unit = {
    var expected: LocalDateTime = LocalDateTime.now(Clock.systemDefaultZone)
    var test: LocalDateTime = LocalDateTime.now
    var diff: Long = Math.abs(test.toLocalTime.toNanoOfDay - expected.toLocalTime.toNanoOfDay)
    if (diff >= 100000000) {
      expected = LocalDateTime.now(Clock.systemDefaultZone)
      test = LocalDateTime.now
      diff = Math.abs(test.toLocalTime.toNanoOfDay - expected.toLocalTime.toNanoOfDay)
    }
    assertTrue(diff < 100000000)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_ZoneId_nullZoneId(): Unit = {
    LocalDateTime.now(null.asInstanceOf[ZoneId])
  }

  @Test def now_ZoneId(): Unit = {
    val zone: ZoneId = ZoneId.of("UTC+01:02:03")
    var expected: LocalDateTime = LocalDateTime.now(Clock.system(zone))
    var test: LocalDateTime = LocalDateTime.now(zone)
    var i: Int = 0
    while (i < 100) {
      if (expected == test)
        return
      expected = LocalDateTime.now(Clock.system(zone))
      test = LocalDateTime.now(zone)
      i += 1
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    LocalDateTime.now(null.asInstanceOf[Clock])
  }

  @Test def now_Clock_allSecsInDay_utc(): Unit = {
    var i: Int = 0
    while (i < (2 * 24 * 60 * 60)) {
      val instant: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
      val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
      val test: LocalDateTime = LocalDateTime.now(clock)
      assertEquals(test.getYear, 1970)
      assertEquals(test.getMonth, Month.JANUARY)
      assertEquals(test.getDayOfMonth, if (i < 24 * 60 * 60) 1 else 2)
      assertEquals(test.getHour, (i / (60 * 60)) % 24)
      assertEquals(test.getMinute, (i / 60) % 60)
      assertEquals(test.getSecond, i % 60)
      assertEquals(test.getNano, 123456789)
      i += 1
    }
  }

  @Test def now_Clock_allSecsInDay_offset(): Unit = {
    var i: Int = 0
    while (i < (2 * 24 * 60 * 60)) {
      val instant: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
      val clock: Clock = Clock.fixed(instant.minusSeconds(TestLocalDateTime.OFFSET_PONE.getTotalSeconds), TestLocalDateTime.OFFSET_PONE)
      val test: LocalDateTime = LocalDateTime.now(clock)
      assertEquals(test.getYear, 1970)
      assertEquals(test.getMonth, Month.JANUARY)
      assertEquals(test.getDayOfMonth, if (i < 24 * 60 * 60) 1 else 2)
      assertEquals(test.getHour, (i / (60 * 60)) % 24)
      assertEquals(test.getMinute, (i / 60) % 60)
      assertEquals(test.getSecond, i % 60)
      assertEquals(test.getNano, 123456789)
      i += 1
    }
  }

  @Test def now_Clock_allSecsInDay_beforeEpoch(): Unit = {
    var expected: LocalTime = LocalTime.MIDNIGHT.plusNanos(123456789L)
    var i: Int = -1
    while (i >= -(24 * 60 * 60)) {
      val instant: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
      val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
      val test: LocalDateTime = LocalDateTime.now(clock)
      assertEquals(test.getYear, 1969)
      assertEquals(test.getMonth, Month.DECEMBER)
      assertEquals(test.getDayOfMonth, 31)
      expected = expected.minusSeconds(1)
      assertEquals(test.toLocalTime, expected)
      i -= 1
    }
  }

  @Test def now_Clock_maxYear(): Unit = {
    val clock: Clock = Clock.fixed(MAX_INSTANT, ZoneOffset.UTC)
    val test: LocalDateTime = LocalDateTime.now(clock)
    assertEquals(test, MAX_DATE_TIME)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def now_Clock_tooBig(): Unit = {
    val clock: Clock = Clock.fixed(MAX_INSTANT.plusSeconds(24 * 60 * 60), ZoneOffset.UTC)
    LocalDateTime.now(clock)
  }

  @Test def now_Clock_minYear(): Unit = {
    val clock: Clock = Clock.fixed(MIN_INSTANT, ZoneOffset.UTC)
    val test: LocalDateTime = LocalDateTime.now(clock)
    assertEquals(test, MIN_DATE_TIME)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def now_Clock_tooLow(): Unit = {
    val clock: Clock = Clock.fixed(MIN_INSTANT.minusNanos(1), ZoneOffset.UTC)
    LocalDateTime.now(clock)
  }

  @Test def factory_of_4intsMonth(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(2007, Month.JULY, 15, 12, 30)
    check(dateTime, 2007, 7, 15, 12, 30, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_yearTooLow(): Unit = {
    LocalDateTime.of(Integer.MIN_VALUE, Month.JULY, 15, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_4intsMonth_nullMonth(): Unit = {
    LocalDateTime.of(2007, null, 15, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_dayTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, -1, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_dayTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 32, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_hourTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, -1, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_hourTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 24, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_minuteTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_4intsMonth_minuteTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 60)
  }

  @Test def factory_of_5intsMonth(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(2007, Month.JULY, 15, 12, 30, 40)
    check(dateTime, 2007, 7, 15, 12, 30, 40, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_yearTooLow(): Unit = {
    LocalDateTime.of(Integer.MIN_VALUE, Month.JULY, 15, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_5intsMonth_nullMonth(): Unit = {
    LocalDateTime.of(2007, null, 15, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_dayTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, -1, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_dayTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 32, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_hourTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, -1, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_hourTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 24, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_minuteTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, -1, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_minuteTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 60, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_secondTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 30, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5intsMonth_secondTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 30, 60)
  }

  @Test def factory_of_6intsMonth(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(2007, Month.JULY, 15, 12, 30, 40, 987654321)
    check(dateTime, 2007, 7, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_yearTooLow(): Unit = {
    LocalDateTime.of(Integer.MIN_VALUE, Month.JULY, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_6intsMonth_nullMonth(): Unit = {
    LocalDateTime.of(2007, null, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_dayTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, -1, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_dayTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 32, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_hourTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, -1, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_hourTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 24, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_minuteTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, -1, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_minuteTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 60, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_secondTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 30, -1, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_secondTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 30, 60, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_nanoTooLow(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 30, 40, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6intsMonth_nanoTooHigh(): Unit = {
    LocalDateTime.of(2007, Month.JULY, 15, 12, 30, 40, 1000000000)
  }

  @Test def factory_of_5ints(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(2007, 7, 15, 12, 30)
    check(dateTime, 2007, 7, 15, 12, 30, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_yearTooLow(): Unit = {
    LocalDateTime.of(Integer.MIN_VALUE, 7, 15, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_monthTooLow(): Unit = {
    LocalDateTime.of(2007, 0, 15, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_monthTooHigh(): Unit = {
    LocalDateTime.of(2007, 13, 15, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_dayTooLow(): Unit = {
    LocalDateTime.of(2007, 7, -1, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_dayTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 32, 12, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_hourTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, -1, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_hourTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 24, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_minuteTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_5ints_minuteTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 60)
  }

  @Test def factory_of_6ints(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(2007, 7, 15, 12, 30, 40)
    check(dateTime, 2007, 7, 15, 12, 30, 40, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_yearTooLow(): Unit = {
    LocalDateTime.of(Integer.MIN_VALUE, 7, 15, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_monthTooLow(): Unit = {
    LocalDateTime.of(2007, 0, 15, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_monthTooHigh(): Unit = {
    LocalDateTime.of(2007, 13, 15, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_dayTooLow(): Unit = {
    LocalDateTime.of(2007, 7, -1, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_dayTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 32, 12, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_hourTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, -1, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_hourTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 24, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_minuteTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, -1, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_minuteTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 60, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_secondTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 30, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_6ints_secondTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 30, 60)
  }

  @Test def factory_of_7ints(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(2007, 7, 15, 12, 30, 40, 987654321)
    check(dateTime, 2007, 7, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_yearTooLow(): Unit = {
    LocalDateTime.of(Integer.MIN_VALUE, 7, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_monthTooLow(): Unit = {
    LocalDateTime.of(2007, 0, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_monthTooHigh(): Unit = {
    LocalDateTime.of(2007, 13, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_dayTooLow(): Unit = {
    LocalDateTime.of(2007, 7, -1, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_dayTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 32, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_hourTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, -1, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_hourTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 24, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_minuteTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, -1, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_minuteTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 60, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_secondTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 30, -1, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_secondTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 30, 60, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_nanoTooLow(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 30, 40, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_7ints_nanoTooHigh(): Unit = {
    LocalDateTime.of(2007, 7, 15, 12, 30, 40, 1000000000)
  }

  @Test def factory_of_LocalDate_LocalTime(): Unit = {
    val dateTime: LocalDateTime = LocalDateTime.of(LocalDate.of(2007, 7, 15), LocalTime.of(12, 30, 40, 987654321))
    check(dateTime, 2007, 7, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDate_LocalTime_nullLocalDate(): Unit = {
    LocalDateTime.of(null, LocalTime.of(12, 30, 40, 987654321))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDate_LocalTime_nullLocalTime(): Unit = {
    LocalDateTime.of(LocalDate.of(2007, 7, 15), null)
  }

  @Test def factory_ofInstant_zone(): Unit = {
    val test: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(86400 + 3600 + 120 + 4, 500), TestLocalDateTime.ZONE_PARIS)
    assertEquals(test, LocalDateTime.of(1970, 1, 2, 2, 2, 4, 500))
  }

  @Test def factory_ofInstant_offset(): Unit = {
    val test: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(86400 + 3600 + 120 + 4, 500), TestLocalDateTime.OFFSET_MTWO)
    assertEquals(test, LocalDateTime.of(1970, 1, 1, 23, 2, 4, 500))
  }

  @Test def factory_ofInstant_offsetBeforeEpoch(): Unit = {
    val test: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(-86400 + 4, 500), TestLocalDateTime.OFFSET_PTWO)
    assertEquals(test, LocalDateTime.of(1969, 12, 31, 2, 0, 4, 500))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofInstant_instantTooBig(): Unit = {
    LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.MaxValue), TestLocalDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofInstant_instantTooSmall(): Unit = {
    LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.MinValue), TestLocalDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_ofInstant_nullInstant(): Unit = {
    LocalDateTime.ofInstant(null.asInstanceOf[Instant], TestLocalDateTime.ZONE_GAZA)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_ofInstant_nullZone(): Unit = {
    LocalDateTime.ofInstant(Instant.EPOCH, null.asInstanceOf[ZoneId])
  }

  @Test def factory_ofEpochSecond_longOffset_afterEpoch(): Unit = {
    val base: LocalDateTime = LocalDateTime.of(1970, 1, 1, 2, 0, 0, 500)

    {
      var i: Int = 0
      while (i < 100000) {
        {
          val test: LocalDateTime = LocalDateTime.ofEpochSecond(i, 500, TestLocalDateTime.OFFSET_PTWO)
          assertEquals(test, base.plusSeconds(i))
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def factory_ofEpochSecond_longOffset_beforeEpoch(): Unit = {
    val base: LocalDateTime = LocalDateTime.of(1970, 1, 1, 2, 0, 0, 500)

    {
      var i: Int = 0
      while (i < 100000) {
        {
          val test: LocalDateTime = LocalDateTime.ofEpochSecond(-i, 500, TestLocalDateTime.OFFSET_PTWO)
          assertEquals(test, base.minusSeconds(i))
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofEpochSecond_longOffset_tooBig(): Unit = {
    LocalDateTime.ofEpochSecond(Long.MaxValue, 500, TestLocalDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofEpochSecond_longOffset_tooSmall(): Unit = {
    LocalDateTime.ofEpochSecond(Long.MinValue, 500, TestLocalDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofEpochSecond_badNanos_toBig(): Unit = {
    LocalDateTime.ofEpochSecond(0, 1000000000, TestLocalDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofEpochSecond_badNanos_toSmall(): Unit = {
    LocalDateTime.ofEpochSecond(0, -1, TestLocalDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_ofEpochSecond_longOffset_nullOffset(): Unit = {
    LocalDateTime.ofEpochSecond(0L, 500, null)
  }

  @Test def test_from_Accessor(): Unit = {
    val base: LocalDateTime = LocalDateTime.of(2007, 7, 15, 17, 30)
    assertEquals(LocalDateTime.from(base), base)
    assertEquals(LocalDateTime.from(ZonedDateTime.of(base, ZoneOffset.ofHours(2))), base)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_from_Accessor_invalid_noDerive(): Unit = {
    LocalDateTime.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_from_Accessor_null(): Unit = {
    LocalDateTime.from(null.asInstanceOf[TemporalAccessor])
  }

  @Test(dataProvider = "sampleToString") def test_parse(y: Int, month: Int, d: Int, h: Int, m: Int, s: Int, n: Int, text: String): Unit = {
    val t: LocalDateTime = LocalDateTime.parse(text)
    assertEquals(t.getYear, y)
    assertEquals(t.getMonth.getValue, month)
    assertEquals(t.getDayOfMonth, d)
    assertEquals(t.getHour, h)
    assertEquals(t.getMinute, m)
    assertEquals(t.getSecond, s)
    assertEquals(t.getNano, n)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalValue(): Unit = {
    LocalDateTime.parse("2008-06-32T11:15")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_invalidValue(): Unit = {
    LocalDateTime.parse("2008-06-31T11:15")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    LocalDateTime.parse(null.asInstanceOf[String])
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M d H m s")
    val test: LocalDateTime = LocalDateTime.parse("2010 12 3 11 30 45", f)
    assertEquals(test, LocalDateTime.of(2010, 12, 3, 11, 30, 45))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M d H m s")
    LocalDateTime.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    LocalDateTime.parse("ANY", null)
  }

  @Test def test_get_DateTimeField(): Unit = {
    val test: LocalDateTime = LocalDateTime.of(2008, 6, 30, 12, 30, 40, 987654321)
    assertEquals(test.getLong(ChronoField.YEAR), 2008)
    assertEquals(test.getLong(ChronoField.MONTH_OF_YEAR), 6)
    assertEquals(test.getLong(ChronoField.DAY_OF_MONTH), 30)
    assertEquals(test.getLong(ChronoField.DAY_OF_WEEK), 1)
    assertEquals(test.getLong(ChronoField.DAY_OF_YEAR), 182)
    assertEquals(test.getLong(ChronoField.HOUR_OF_DAY), 12)
    assertEquals(test.getLong(ChronoField.MINUTE_OF_HOUR), 30)
    assertEquals(test.getLong(ChronoField.SECOND_OF_MINUTE), 40)
    assertEquals(test.getLong(ChronoField.NANO_OF_SECOND), 987654321)
    assertEquals(test.getLong(ChronoField.HOUR_OF_AMPM), 0)
    assertEquals(test.getLong(ChronoField.AMPM_OF_DAY), 1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_get_DateTimeField_null(): Unit = {
    val test: LocalDateTime = LocalDateTime.of(2008, 6, 30, 12, 30, 40, 987654321)
    test.getLong(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_DateTimeField_invalidField(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.getLong(MockFieldNoValue.INSTANCE)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.localDate), TEST_2007_07_15_12_30_40_987654321.toLocalDate)
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.localTime), TEST_2007_07_15_12_30_40_987654321.toLocalTime)
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.offset), null)
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.precision), ChronoUnit.NANOS)
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.zone), null)
    assertEquals(TEST_2007_07_15_12_30_40_987654321.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.query(null)
  }

  @DataProvider(name = "sampleDates") private[bp] def provider_sampleDates: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(2008: Integer, 7: Integer, 5: Integer), Array(2007: Integer, 7: Integer, 5: Integer), Array(2006: Integer, 7: Integer, 5: Integer), Array(2005: Integer, 7: Integer, 5: Integer), Array(2004: Integer, 1: Integer, 1: Integer), Array(-1: Integer, 1: Integer, 2: Integer))
  }

  @DataProvider(name = "sampleTimes") private[bp] def provider_sampleTimes: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(0: Integer, 0: Integer, 0: Integer, 0: Integer), Array(0: Integer, 0: Integer, 0: Integer, 1: Integer), Array(0: Integer, 0: Integer, 1: Integer, 0: Integer), Array(0: Integer, 0: Integer, 1: Integer, 1: Integer), Array(0: Integer, 1: Integer, 0: Integer, 0: Integer), Array(0: Integer, 1: Integer, 0: Integer, 1: Integer), Array(0: Integer, 1: Integer, 1: Integer, 0: Integer), Array(0: Integer, 1: Integer, 1: Integer, 1: Integer), Array(1: Integer, 0: Integer, 0: Integer, 0: Integer), Array(1: Integer, 0: Integer, 0: Integer, 1: Integer), Array(1: Integer, 0: Integer, 1: Integer, 0: Integer), Array(1: Integer, 0: Integer, 1: Integer, 1: Integer), Array(1: Integer, 1: Integer, 0: Integer, 0: Integer), Array(1: Integer, 1: Integer, 0: Integer, 1: Integer), Array(1: Integer, 1: Integer, 1: Integer, 0: Integer), Array(1: Integer, 1: Integer, 1: Integer, 1: Integer))
  }

  @Test(dataProvider = "sampleDates") def test_get_dates(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, 12, 30)
    assertEquals(a.getYear, y)
    assertEquals(a.getMonth, Month.of(m))
    assertEquals(a.getDayOfMonth, d)
  }

  @Test(dataProvider = "sampleDates") def test_getDOY(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, 12, 30)
    var total: Int = 0

    {
      var i: Int = 1
      while (i < m) {
        {
          total += Month.of(i).length(AbstractTest.isIsoLeap(y))
        }
        {
          i += 1
          i - 1
        }
      }
    }
    val doy: Int = total + d
    assertEquals(a.getDayOfYear, doy)
  }

  @Test(dataProvider = "sampleTimes") def test_get_times(h: Int, m: Int, s: Int, ns: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(TEST_2007_07_15_12_30_40_987654321.toLocalDate, LocalTime.of(h, m, s, ns))
    assertEquals(a.getHour, h)
    assertEquals(a.getMinute, m)
    assertEquals(a.getSecond, s)
    assertEquals(a.getNano, ns)
  }

  @Test def test_getDayOfWeek(): Unit = {
    var dow: DayOfWeek = DayOfWeek.MONDAY
    for (month <- Month.values) {
      val length: Int = month.length(false)

      {
        var i: Int = 1
        while (i <= length) {
          {
            val d: LocalDateTime = LocalDateTime.of(LocalDate.of(2007, month, i), TEST_2007_07_15_12_30_40_987654321.toLocalTime)
            assertSame(d.getDayOfWeek, dow)
            dow = dow.plus(1)
          }
          {
            i += 1
            i - 1
          }
        }
      }
    }
  }

  @Test def test_with_adjustment(): Unit = {
    val sample: LocalDateTime = LocalDateTime.of(2012, 3, 4, 23, 5)
    val adjuster: TemporalAdjuster = new TemporalAdjuster {
      override def adjustInto(temporal: Temporal): Temporal = sample
    }
    assertEquals(TEST_2007_07_15_12_30_40_987654321.`with`(adjuster), sample)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_adjustment_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.`with`(null.asInstanceOf[TemporalAdjuster])
  }

  @Test def test_withYear_int_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.withYear(2008)
    check(t, 2008, 7, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withYear_int_invalid(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withYear(Year.MIN_VALUE - 1)
  }

  @Test def test_withYear_int_adjustDay(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2008, 2, 29, 12, 30).withYear(2007)
    val expected: LocalDateTime = LocalDateTime.of(2007, 2, 28, 12, 30)
    assertEquals(t, expected)
  }

  @Test def test_withMonth_int_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.withMonth(1)
    check(t, 2007, 1, 15, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMonth_int_invalid(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withMonth(13)
  }

  @Test def test_withMonth_int_adjustDay(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2007, 12, 31, 12, 30).withMonth(11)
    val expected: LocalDateTime = LocalDateTime.of(2007, 11, 30, 12, 30)
    assertEquals(t, expected)
  }

  @Test def test_withDayOfMonth_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.withDayOfMonth(1)
    check(t, 2007, 7, 1, 12, 30, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_invalid(): Unit = {
    LocalDateTime.of(2007, 11, 30, 12, 30).withDayOfMonth(32)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_invalidCombination(): Unit = {
    LocalDateTime.of(2007, 11, 30, 12, 30).withDayOfMonth(31)
  }

  @Test def test_withDayOfYear_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.withDayOfYear(33)
    assertEquals(t, LocalDateTime.of(2007, 2, 2, 12, 30, 40, 987654321))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfYear_illegal(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withDayOfYear(367)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfYear_invalid(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withDayOfYear(366)
  }

  @Test def test_withHour_normal(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321

    {
      var i: Int = 0
      while (i < 24) {
        {
          t = t.withHour(i)
          assertEquals(t.getHour, i)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withHour_hourTooLow(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withHour(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withHour_hourTooHigh(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withHour(24)
  }

  @Test def test_withMinute_normal(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321

    {
      var i: Int = 0
      while (i < 60) {
        {
          t = t.withMinute(i)
          assertEquals(t.getMinute, i)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMinute_minuteTooLow(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withMinute(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMinute_minuteTooHigh(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withMinute(60)
  }

  @Test def test_withSecond_normal(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321

    {
      var i: Int = 0
      while (i < 60) {
        {
          t = t.withSecond(i)
          assertEquals(t.getSecond, i)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withSecond_secondTooLow(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withSecond(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withSecond_secondTooHigh(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withSecond(60)
  }

  @Test def test_withNanoOfSecond_normal(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321
    t = t.withNano(1)
    assertEquals(t.getNano, 1)
    t = t.withNano(10)
    assertEquals(t.getNano, 10)
    t = t.withNano(100)
    assertEquals(t.getNano, 100)
    t = t.withNano(999999999)
    assertEquals(t.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withNanoOfSecond_nanoTooLow(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withNano(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withNanoOfSecond_nanoTooHigh(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.withNano(1000000000)
  }

  @Test def test_plus_adjuster(): Unit = {
    val p: Duration = Duration.ofSeconds(62, 3)
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plus(p)
    assertEquals(t, LocalDateTime.of(2007, 7, 15, 12, 31, 42, 987654324))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_adjuster_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.plus(null)
  }

  @Test def test_plus_Period_positiveMonths(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plus(period)
    assertEquals(t, LocalDateTime.of(2008, 2, 15, 12, 30, 40, 987654321))
  }

  @Test def test_plus_Period_negativeDays(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-25, ChronoUnit.DAYS)
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plus(period)
    assertEquals(t, LocalDateTime.of(2007, 6, 20, 12, 30, 40, 987654321))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_Period_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.plus(null.asInstanceOf[MockSimplePeriod])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_Period_invalidTooLarge(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(1, ChronoUnit.YEARS)
    LocalDateTime.of(Year.MAX_VALUE, 1, 1, 0, 0).plus(period)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_Period_invalidTooSmall(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-1, ChronoUnit.YEARS)
    LocalDateTime.of(Year.MIN_VALUE, 1, 1, 0, 0).plus(period)
  }

  @Test def test_plus_longPeriodUnit_positiveMonths(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plus(7, ChronoUnit.MONTHS)
    assertEquals(t, LocalDateTime.of(2008, 2, 15, 12, 30, 40, 987654321))
  }

  @Test def test_plus_longPeriodUnit_negativeDays(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plus(-25, ChronoUnit.DAYS)
    assertEquals(t, LocalDateTime.of(2007, 6, 20, 12, 30, 40, 987654321))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_longPeriodUnit_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.plus(1, null.asInstanceOf[TemporalUnit])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_longPeriodUnit_invalidTooLarge(): Unit = {
    LocalDateTime.of(Year.MAX_VALUE, 1, 1, 0, 0).plus(1, ChronoUnit.YEARS)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_longPeriodUnit_invalidTooSmall(): Unit = {
    LocalDateTime.of(Year.MIN_VALUE, 1, 1, 0, 0).plus(-1, ChronoUnit.YEARS)
  }

  @Test def test_plusYears_int_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusYears(1)
    check(t, 2008, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusYears_int_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusYears(-1)
    check(t, 2006, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusYears_int_adjustDay(): Unit = {
    val t: LocalDateTime = createDateMidnight(2008, 2, 29).plusYears(1)
    check(t, 2009, 2, 28, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_int_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 1, 1).plusYears(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_int_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plusYears(-1)
  }

  @Test def test_plusMonths_int_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusMonths(1)
    check(t, 2007, 8, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusMonths_int_overYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusMonths(25)
    check(t, 2009, 8, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusMonths_int_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusMonths(-1)
    check(t, 2007, 6, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusMonths_int_negativeAcrossYear(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusMonths(-7)
    check(t, 2006, 12, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusMonths_int_negativeOverYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusMonths(-31)
    check(t, 2004, 12, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusMonths_int_adjustDayFromLeapYear(): Unit = {
    val t: LocalDateTime = createDateMidnight(2008, 2, 29).plusMonths(12)
    check(t, 2009, 2, 28, 0, 0, 0, 0)
  }

  @Test def test_plusMonths_int_adjustDayFromMonthLength(): Unit = {
    val t: LocalDateTime = createDateMidnight(2007, 3, 31).plusMonths(1)
    check(t, 2007, 4, 30, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_int_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 1).plusMonths(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_int_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).plusMonths(-1)
  }

  @DataProvider(name = "samplePlusWeeksSymmetry") private[bp] def provider_samplePlusWeeksSymmetry: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(createDateMidnight(-1, 1, 1)), Array(createDateMidnight(-1, 2, 28)), Array(createDateMidnight(-1, 3, 1)), Array(createDateMidnight(-1, 12, 31)), Array(createDateMidnight(0, 1, 1)), Array(createDateMidnight(0, 2, 28)), Array(createDateMidnight(0, 2, 29)), Array(createDateMidnight(0, 3, 1)), Array(createDateMidnight(0, 12, 31)), Array(createDateMidnight(2007, 1, 1)), Array(createDateMidnight(2007, 2, 28)), Array(createDateMidnight(2007, 3, 1)), Array(createDateMidnight(2007, 12, 31)), Array(createDateMidnight(2008, 1, 1)), Array(createDateMidnight(2008, 2, 28)), Array(createDateMidnight(2008, 2, 29)), Array(createDateMidnight(2008, 3, 1)), Array(createDateMidnight(2008, 12, 31)), Array(createDateMidnight(2099, 1, 1)), Array(createDateMidnight(2099, 2, 28)), Array(createDateMidnight(2099, 3, 1)), Array(createDateMidnight(2099, 12, 31)), Array(createDateMidnight(2100, 1, 1)), Array(createDateMidnight(2100, 2, 28)), Array(createDateMidnight(2100, 3, 1)), Array(createDateMidnight(2100, 12, 31)))
  }

  @Test(dataProvider = "samplePlusWeeksSymmetry") def test_plusWeeks_symmetry(reference: LocalDateTime): Unit = {
    {
      var weeks: Int = 0
      while (weeks < 365 * 8) {
        {
          var t: LocalDateTime = reference.plusWeeks(weeks).plusWeeks(-weeks)
          assertEquals(t, reference)
          t = reference.plusWeeks(-weeks).plusWeeks(weeks)
          assertEquals(t, reference)
        }
        {
          weeks += 1
          weeks - 1
        }
      }
    }
  }

  @Test def test_plusWeeks_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusWeeks(1)
    check(t, 2007, 7, 22, 12, 30, 40, 987654321)
  }

  @Test def test_plusWeeks_overMonths(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusWeeks(9)
    check(t, 2007, 9, 16, 12, 30, 40, 987654321)
  }

  @Test def test_plusWeeks_overYears(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2006, 7, 16, 12, 30, 40, 987654321).plusWeeks(52)
    assertEquals(t, TEST_2007_07_15_12_30_40_987654321)
  }

  @Test def test_plusWeeks_overLeapYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusYears(-1).plusWeeks(104)
    check(t, 2008, 7, 12, 12, 30, 40, 987654321)
  }

  @Test def test_plusWeeks_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusWeeks(-1)
    check(t, 2007, 7, 8, 12, 30, 40, 987654321)
  }

  @Test def test_plusWeeks_negativeAcrossYear(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusWeeks(-28)
    check(t, 2006, 12, 31, 12, 30, 40, 987654321)
  }

  @Test def test_plusWeeks_negativeOverYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusWeeks(-104)
    check(t, 2005, 7, 17, 12, 30, 40, 987654321)
  }

  @Test def test_plusWeeks_maximum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MAX_VALUE, 12, 24).plusWeeks(1)
    check(t, Year.MAX_VALUE, 12, 31, 0, 0, 0, 0)
  }

  @Test def test_plusWeeks_minimum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MIN_VALUE, 1, 8).plusWeeks(-1)
    check(t, Year.MIN_VALUE, 1, 1, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusWeeks_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 25).plusWeeks(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusWeeks_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 7).plusWeeks(-1)
  }

  @DataProvider(name = "samplePlusDaysSymmetry") private[bp] def provider_samplePlusDaysSymmetry: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(createDateMidnight(-1, 1, 1)), Array(createDateMidnight(-1, 2, 28)), Array(createDateMidnight(-1, 3, 1)), Array(createDateMidnight(-1, 12, 31)), Array(createDateMidnight(0, 1, 1)), Array(createDateMidnight(0, 2, 28)), Array(createDateMidnight(0, 2, 29)), Array(createDateMidnight(0, 3, 1)), Array(createDateMidnight(0, 12, 31)), Array(createDateMidnight(2007, 1, 1)), Array(createDateMidnight(2007, 2, 28)), Array(createDateMidnight(2007, 3, 1)), Array(createDateMidnight(2007, 12, 31)), Array(createDateMidnight(2008, 1, 1)), Array(createDateMidnight(2008, 2, 28)), Array(createDateMidnight(2008, 2, 29)), Array(createDateMidnight(2008, 3, 1)), Array(createDateMidnight(2008, 12, 31)), Array(createDateMidnight(2099, 1, 1)), Array(createDateMidnight(2099, 2, 28)), Array(createDateMidnight(2099, 3, 1)), Array(createDateMidnight(2099, 12, 31)), Array(createDateMidnight(2100, 1, 1)), Array(createDateMidnight(2100, 2, 28)), Array(createDateMidnight(2100, 3, 1)), Array(createDateMidnight(2100, 12, 31)))
  }

  @Test(dataProvider = "samplePlusDaysSymmetry") def test_plusDays_symmetry(reference: LocalDateTime): Unit = {
    {
      var days: Int = 0
      while (days < 365 * 8) {
        {
          var t: LocalDateTime = reference.plusDays(days).plusDays(-days)
          assertEquals(t, reference)
          t = reference.plusDays(-days).plusDays(days)
          assertEquals(t, reference)
        }
        {
          days += 1
          days - 1
        }
      }
    }
  }

  @Test def test_plusDays_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusDays(1)
    check(t, 2007, 7, 16, 12, 30, 40, 987654321)
  }

  @Test def test_plusDays_overMonths(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusDays(62)
    check(t, 2007, 9, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusDays_overYears(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2006, 7, 14, 12, 30, 40, 987654321).plusDays(366)
    assertEquals(t, TEST_2007_07_15_12_30_40_987654321)
  }

  @Test def test_plusDays_overLeapYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusYears(-1).plusDays(365 + 366)
    check(t, 2008, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusDays_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusDays(-1)
    check(t, 2007, 7, 14, 12, 30, 40, 987654321)
  }

  @Test def test_plusDays_negativeAcrossYear(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusDays(-196)
    check(t, 2006, 12, 31, 12, 30, 40, 987654321)
  }

  @Test def test_plusDays_negativeOverYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusDays(-730)
    check(t, 2005, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_plusDays_maximum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MAX_VALUE, 12, 30).plusDays(1)
    check(t, Year.MAX_VALUE, 12, 31, 0, 0, 0, 0)
  }

  @Test def test_plusDays_minimum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MIN_VALUE, 1, 2).plusDays(-1)
    check(t, Year.MIN_VALUE, 1, 1, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusDays_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 31).plusDays(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusDays_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).plusDays(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusDays_overflowTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 31).plusDays(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusDays_overflowTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).plusDays(Long.MinValue)
  }

  @Test def test_plusHours_one(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    var d: LocalDate = t.toLocalDate

    {
      var i: Int = 0
      while (i < 50) {
        {
          t = t.plusHours(1)
          if ((i + 1) % 24 == 0) {
            d = d.plusDays(1)
          }
          assertEquals(t.toLocalDate, d)
          assertEquals(t.getHour, (i + 1) % 24)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusHours_fromZero(): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    var d: LocalDate = base.toLocalDate.minusDays(3)
    var t: LocalTime = LocalTime.of(21, 0)

    {
      var i: Int = -50
      while (i < 50) {
        {
          val dt: LocalDateTime = base.plusHours(i)
          t = t.plusHours(1)
          if (t.getHour == 0) {
            d = d.plusDays(1)
          }
          assertEquals(dt.toLocalDate, d)
          assertEquals(dt.toLocalTime, t)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusHours_fromOne(): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.of(1, 0))
    var d: LocalDate = base.toLocalDate.minusDays(3)
    var t: LocalTime = LocalTime.of(22, 0)

    {
      var i: Int = -50
      while (i < 50) {
        {
          val dt: LocalDateTime = base.plusHours(i)
          t = t.plusHours(1)
          if (t.getHour == 0) {
            d = d.plusDays(1)
          }
          assertEquals(dt.toLocalDate, d)
          assertEquals(dt.toLocalTime, t)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusMinutes_one(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val d: LocalDate = t.toLocalDate
    var hour: Int = 0
    var min: Int = 0

    {
      var i: Int = 0
      while (i < 70) {
        {
          t = t.plusMinutes(1)
          min += 1
          if (min == 60) {
            hour += 1
            min = 0
          }
          assertEquals(t.toLocalDate, d)
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusMinutes_fromZero(): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    var d: LocalDate = base.toLocalDate.minusDays(1)
    var t: LocalTime = LocalTime.of(22, 49)

    {
      var i: Int = -70
      while (i < 70) {
        {
          val dt: LocalDateTime = base.plusMinutes(i)
          t = t.plusMinutes(1)
          if (t eq LocalTime.MIDNIGHT) {
            d = d.plusDays(1)
          }
          assertEquals(dt.toLocalDate, d, String.valueOf(i))
          assertEquals(dt.toLocalTime, t, String.valueOf(i))
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusMinutes_noChange_oneDay(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusMinutes(24 * 60)
    assertEquals(t.toLocalDate, TEST_2007_07_15_12_30_40_987654321.toLocalDate.plusDays(1))
  }

  @Test def test_plusSeconds_one(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val d: LocalDate = t.toLocalDate
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0

    {
      var i: Int = 0
      while (i < 3700) {
        {
          t = t.plusSeconds(1)
          sec += 1
          if (sec == 60) {
            min += 1
            sec = 0
          }
          if (min == 60) {
            hour += 1
            min = 0
          }
          assertEquals(t.toLocalDate, d)
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
          assertEquals(t.getSecond, sec)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @DataProvider(name = "plusSeconds_fromZero") private[bp] def plusSeconds_fromZero: java.util.Iterator[Array[Any]] = {
    new java.util.Iterator[Array[Any]]() {
      private[bp] var delta: Int = 30
      private[bp] var i: Int = -3660
      private[bp] var date: LocalDate = TEST_2007_07_15_12_30_40_987654321.toLocalDate.minusDays(1)
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0

      def hasNext: Boolean = i <= 3660

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, date, hour, min, sec)
        i += delta
        sec += delta
        if (sec >= 60) {
          min += 1
          sec -= 60
          if (min == 60) {
            hour += 1
            min = 0
            if (hour == 24) {
              hour = 0
            }
          }
        }
        if (i == 0)
          date = date.plusDays(1)
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "plusSeconds_fromZero") def test_plusSeconds_fromZero(seconds: Int, date: LocalDate, hour: Int, min: Int, sec: Int): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val t: LocalDateTime = base.plusSeconds(seconds)
    assertEquals(date, t.toLocalDate)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
  }

  @Test def test_plusSeconds_noChange_oneDay(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusSeconds(24 * 60 * 60)
    assertEquals(t.toLocalDate, TEST_2007_07_15_12_30_40_987654321.toLocalDate.plusDays(1))
  }

  @Test def test_plusNanos_halfABillion(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val d: LocalDate = t.toLocalDate
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0
    var nanos: Int = 0

    {
      var i: Long = 0
      while (i < 3700 * 1000000000L) {
        {
          t = t.plusNanos(500000000)
          nanos += 500000000
          if (nanos == 1000000000) {
            sec += 1
            nanos = 0
          }
          if (sec == 60) {
            min += 1
            sec = 0
          }
          if (min == 60) {
            hour += 1
            min = 0
          }
          assertEquals(t.toLocalDate, d, String.valueOf(i))
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
          assertEquals(t.getSecond, sec)
          assertEquals(t.getNano, nanos)
        }
        i += 500000000
      }
    }
  }

  @DataProvider(name = "plusNanos_fromZero") private[bp] def plusNanos_fromZero: java.util.Iterator[Array[Any]] = {
    new java.util.Iterator[Array[Any]]() {
      private[bp] var delta: Long = 7500000000L
      private[bp] var i: Long = -3660 * 1000000000L
      private[bp] var date: LocalDate = TEST_2007_07_15_12_30_40_987654321.toLocalDate.minusDays(1)
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0
      private[bp] var nanos: Long = 0

      def hasNext: Boolean = i <= 3660 * 1000000000L

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, date, hour, min, sec, nanos.toInt)
        i += delta
        nanos += delta
        if (nanos >= 1000000000L) {
          sec += (nanos / 1000000000L).toInt // !!!
          nanos %= 1000000000L
          if (sec >= 60) {
            min += 1
            sec %= 60
            if (min == 60) {
              hour += 1
              min = 0
              if (hour == 24) {
                hour = 0
                date = date.plusDays(1)
              }
            }
          }
        }
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "plusNanos_fromZero") def test_plusNanos_fromZero(nanoseconds: Long, date: LocalDate, hour: Int, min: Int, sec: Int, nanos: Int): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val t: LocalDateTime = base.plusNanos(nanoseconds)
    assertEquals(date, t.toLocalDate)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
    assertEquals(nanos, t.getNano)
  }

  @Test def test_plusNanos_noChange_oneDay(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusNanos(24 * 60 * 60 * 1000000000L)
    assertEquals(t.toLocalDate, TEST_2007_07_15_12_30_40_987654321.toLocalDate.plusDays(1))
  }

  @Test def test_minus_adjuster(): Unit = {
    val p: Duration = Duration.ofSeconds(62, 3)
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minus(p)
    assertEquals(t, LocalDateTime.of(2007, 7, 15, 12, 29, 38, 987654318))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_adjuster_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.minus(null)
  }

  @Test def test_minus_Period_positiveMonths(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minus(period)
    assertEquals(t, LocalDateTime.of(2006, 12, 15, 12, 30, 40, 987654321))
  }

  @Test def test_minus_Period_negativeDays(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-25, ChronoUnit.DAYS)
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minus(period)
    assertEquals(t, LocalDateTime.of(2007, 8, 9, 12, 30, 40, 987654321))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_Period_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.minus(null.asInstanceOf[MockSimplePeriod])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_Period_invalidTooLarge(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-1, ChronoUnit.YEARS)
    LocalDateTime.of(Year.MAX_VALUE, 1, 1, 0, 0).minus(period)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_Period_invalidTooSmall(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(1, ChronoUnit.YEARS)
    LocalDateTime.of(Year.MIN_VALUE, 1, 1, 0, 0).minus(period)
  }

  @Test def test_minus_longPeriodUnit_positiveMonths(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minus(7, ChronoUnit.MONTHS)
    assertEquals(t, LocalDateTime.of(2006, 12, 15, 12, 30, 40, 987654321))
  }

  @Test def test_minus_longPeriodUnit_negativeDays(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minus(-25, ChronoUnit.DAYS)
    assertEquals(t, LocalDateTime.of(2007, 8, 9, 12, 30, 40, 987654321))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_longPeriodUnit_null(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.minus(1, null.asInstanceOf[TemporalUnit])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_longPeriodUnit_invalidTooLarge(): Unit = {
    LocalDateTime.of(Year.MAX_VALUE, 1, 1, 0, 0).minus(-1, ChronoUnit.YEARS)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_longPeriodUnit_invalidTooSmall(): Unit = {
    LocalDateTime.of(Year.MIN_VALUE, 1, 1, 0, 0).minus(1, ChronoUnit.YEARS)
  }

  @Test def test_minusYears_int_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusYears(1)
    check(t, 2006, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusYears_int_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusYears(-1)
    check(t, 2008, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusYears_int_adjustDay(): Unit = {
    val t: LocalDateTime = createDateMidnight(2008, 2, 29).minusYears(1)
    check(t, 2007, 2, 28, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_int_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 1, 1).minusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_int_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).minusYears(1)
  }

  @Test def test_minusMonths_int_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusMonths(1)
    check(t, 2007, 6, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusMonths_int_overYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusMonths(25)
    check(t, 2005, 6, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusMonths_int_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusMonths(-1)
    check(t, 2007, 8, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusMonths_int_negativeAcrossYear(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusMonths(-7)
    check(t, 2008, 2, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusMonths_int_negativeOverYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusMonths(-31)
    check(t, 2010, 2, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusMonths_int_adjustDayFromLeapYear(): Unit = {
    val t: LocalDateTime = createDateMidnight(2008, 2, 29).minusMonths(12)
    check(t, 2007, 2, 28, 0, 0, 0, 0)
  }

  @Test def test_minusMonths_int_adjustDayFromMonthLength(): Unit = {
    val t: LocalDateTime = createDateMidnight(2007, 3, 31).minusMonths(1)
    check(t, 2007, 2, 28, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_int_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 1).minusMonths(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_int_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).minusMonths(1)
  }

  @DataProvider(name = "sampleMinusWeeksSymmetry") private[bp] def provider_sampleMinusWeeksSymmetry: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(createDateMidnight(-1, 1, 1)), Array(createDateMidnight(-1, 2, 28)), Array(createDateMidnight(-1, 3, 1)), Array(createDateMidnight(-1, 12, 31)), Array(createDateMidnight(0, 1, 1)), Array(createDateMidnight(0, 2, 28)), Array(createDateMidnight(0, 2, 29)), Array(createDateMidnight(0, 3, 1)), Array(createDateMidnight(0, 12, 31)), Array(createDateMidnight(2007, 1, 1)), Array(createDateMidnight(2007, 2, 28)), Array(createDateMidnight(2007, 3, 1)), Array(createDateMidnight(2007, 12, 31)), Array(createDateMidnight(2008, 1, 1)), Array(createDateMidnight(2008, 2, 28)), Array(createDateMidnight(2008, 2, 29)), Array(createDateMidnight(2008, 3, 1)), Array(createDateMidnight(2008, 12, 31)), Array(createDateMidnight(2099, 1, 1)), Array(createDateMidnight(2099, 2, 28)), Array(createDateMidnight(2099, 3, 1)), Array(createDateMidnight(2099, 12, 31)), Array(createDateMidnight(2100, 1, 1)), Array(createDateMidnight(2100, 2, 28)), Array(createDateMidnight(2100, 3, 1)), Array(createDateMidnight(2100, 12, 31)))
  }

  @Test(dataProvider = "sampleMinusWeeksSymmetry") def test_minusWeeks_symmetry(reference: LocalDateTime): Unit = {
    {
      var weeks: Int = 0
      while (weeks < 365 * 8) {
        {
          var t: LocalDateTime = reference.minusWeeks(weeks).minusWeeks(-weeks)
          assertEquals(t, reference)
          t = reference.minusWeeks(-weeks).minusWeeks(weeks)
          assertEquals(t, reference)
        }
        {
          weeks += 1
          weeks - 1
        }
      }
    }
  }

  @Test def test_minusWeeks_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusWeeks(1)
    check(t, 2007, 7, 8, 12, 30, 40, 987654321)
  }

  @Test def test_minusWeeks_overMonths(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusWeeks(9)
    check(t, 2007, 5, 13, 12, 30, 40, 987654321)
  }

  @Test def test_minusWeeks_overYears(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2008, 7, 13, 12, 30, 40, 987654321).minusWeeks(52)
    assertEquals(t, TEST_2007_07_15_12_30_40_987654321)
  }

  @Test def test_minusWeeks_overLeapYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusYears(-1).minusWeeks(104)
    check(t, 2006, 7, 18, 12, 30, 40, 987654321)
  }

  @Test def test_minusWeeks_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusWeeks(-1)
    check(t, 2007, 7, 22, 12, 30, 40, 987654321)
  }

  @Test def test_minusWeeks_negativeAcrossYear(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusWeeks(-28)
    check(t, 2008, 1, 27, 12, 30, 40, 987654321)
  }

  @Test def test_minusWeeks_negativeOverYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusWeeks(-104)
    check(t, 2009, 7, 12, 12, 30, 40, 987654321)
  }

  @Test def test_minusWeeks_maximum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MAX_VALUE, 12, 24).minusWeeks(-1)
    check(t, Year.MAX_VALUE, 12, 31, 0, 0, 0, 0)
  }

  @Test def test_minusWeeks_minimum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MIN_VALUE, 1, 8).minusWeeks(1)
    check(t, Year.MIN_VALUE, 1, 1, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusWeeks_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 25).minusWeeks(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusWeeks_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 7).minusWeeks(1)
  }

  @DataProvider(name = "sampleMinusDaysSymmetry") private[bp] def provider_sampleMinusDaysSymmetry: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(createDateMidnight(-1, 1, 1)), Array(createDateMidnight(-1, 2, 28)), Array(createDateMidnight(-1, 3, 1)), Array(createDateMidnight(-1, 12, 31)), Array(createDateMidnight(0, 1, 1)), Array(createDateMidnight(0, 2, 28)), Array(createDateMidnight(0, 2, 29)), Array(createDateMidnight(0, 3, 1)), Array(createDateMidnight(0, 12, 31)), Array(createDateMidnight(2007, 1, 1)), Array(createDateMidnight(2007, 2, 28)), Array(createDateMidnight(2007, 3, 1)), Array(createDateMidnight(2007, 12, 31)), Array(createDateMidnight(2008, 1, 1)), Array(createDateMidnight(2008, 2, 28)), Array(createDateMidnight(2008, 2, 29)), Array(createDateMidnight(2008, 3, 1)), Array(createDateMidnight(2008, 12, 31)), Array(createDateMidnight(2099, 1, 1)), Array(createDateMidnight(2099, 2, 28)), Array(createDateMidnight(2099, 3, 1)), Array(createDateMidnight(2099, 12, 31)), Array(createDateMidnight(2100, 1, 1)), Array(createDateMidnight(2100, 2, 28)), Array(createDateMidnight(2100, 3, 1)), Array(createDateMidnight(2100, 12, 31)))
  }

  @Test(dataProvider = "sampleMinusDaysSymmetry") def test_minusDays_symmetry(reference: LocalDateTime): Unit = {
    {
      var days: Int = 0
      while (days < 365 * 8) {
        {
          var t: LocalDateTime = reference.minusDays(days).minusDays(-days)
          assertEquals(t, reference)
          t = reference.minusDays(-days).minusDays(days)
          assertEquals(t, reference)
        }
        {
          days += 1
          days - 1
        }
      }
    }
  }

  @Test def test_minusDays_normal(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusDays(1)
    check(t, 2007, 7, 14, 12, 30, 40, 987654321)
  }

  @Test def test_minusDays_overMonths(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusDays(62)
    check(t, 2007, 5, 14, 12, 30, 40, 987654321)
  }

  @Test def test_minusDays_overYears(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2008, 7, 16, 12, 30, 40, 987654321).minusDays(367)
    assertEquals(t, TEST_2007_07_15_12_30_40_987654321)
  }

  @Test def test_minusDays_overLeapYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.plusYears(2).minusDays(365 + 366)
    assertEquals(t, TEST_2007_07_15_12_30_40_987654321)
  }

  @Test def test_minusDays_negative(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusDays(-1)
    check(t, 2007, 7, 16, 12, 30, 40, 987654321)
  }

  @Test def test_minusDays_negativeAcrossYear(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusDays(-169)
    check(t, 2007, 12, 31, 12, 30, 40, 987654321)
  }

  @Test def test_minusDays_negativeOverYears(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusDays(-731)
    check(t, 2009, 7, 15, 12, 30, 40, 987654321)
  }

  @Test def test_minusDays_maximum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MAX_VALUE, 12, 30).minusDays(-1)
    check(t, Year.MAX_VALUE, 12, 31, 0, 0, 0, 0)
  }

  @Test def test_minusDays_minimum(): Unit = {
    val t: LocalDateTime = createDateMidnight(Year.MIN_VALUE, 1, 2).minusDays(1)
    check(t, Year.MIN_VALUE, 1, 1, 0, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusDays_invalidTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 31).minusDays(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusDays_invalidTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).minusDays(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusDays_overflowTooLarge(): Unit = {
    createDateMidnight(Year.MAX_VALUE, 12, 31).minusDays(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusDays_overflowTooSmall(): Unit = {
    createDateMidnight(Year.MIN_VALUE, 1, 1).minusDays(Long.MaxValue)
  }

  @Test def test_minusHours_one(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    var d: LocalDate = t.toLocalDate

    {
      var i: Int = 0
      while (i < 50) {
        {
          t = t.minusHours(1)
          if (i % 24 == 0) {
            d = d.minusDays(1)
          }
          assertEquals(t.toLocalDate, d)
          assertEquals(t.getHour, (((-i + 23) % 24) + 24) % 24)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusHours_fromZero(): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    var d: LocalDate = base.toLocalDate.plusDays(2)
    var t: LocalTime = LocalTime.of(3, 0)

    {
      var i: Int = -50
      while (i < 50) {
        {
          val dt: LocalDateTime = base.minusHours(i)
          t = t.minusHours(1)
          if (t.getHour == 23) {
            d = d.minusDays(1)
          }
          assertEquals(dt.toLocalDate, d, String.valueOf(i))
          assertEquals(dt.toLocalTime, t)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusHours_fromOne(): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.of(1, 0))
    var d: LocalDate = base.toLocalDate.plusDays(2)
    var t: LocalTime = LocalTime.of(4, 0)

    {
      var i: Int = -50
      while (i < 50) {
        {
          val dt: LocalDateTime = base.minusHours(i)
          t = t.minusHours(1)
          if (t.getHour == 23) {
            d = d.minusDays(1)
          }
          assertEquals(dt.toLocalDate, d, String.valueOf(i))
          assertEquals(dt.toLocalTime, t)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusMinutes_one(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val d: LocalDate = t.toLocalDate.minusDays(1)
    var hour: Int = 0
    var min: Int = 0

    {
      var i: Int = 0
      while (i < 70) {
        {
          t = t.minusMinutes(1)
          min -= 1
          if (min == -1) {
            hour -= 1
            min = 59
            if (hour == -1) {
              hour = 23
            }
          }
          assertEquals(t.toLocalDate, d)
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusMinutes_fromZero(): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    var d: LocalDate = base.toLocalDate.minusDays(1)
    var t: LocalTime = LocalTime.of(22, 49)

    {
      var i: Int = 70
      while (i > -70) {
        {
          val dt: LocalDateTime = base.minusMinutes(i)
          t = t.plusMinutes(1)
          if (t eq LocalTime.MIDNIGHT) {
            d = d.plusDays(1)
          }
          assertEquals(dt.toLocalDate, d)
          assertEquals(dt.toLocalTime, t)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test def test_minusMinutes_noChange_oneDay(): Unit = {
    val t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.minusMinutes(24 * 60)
    assertEquals(t.toLocalDate, TEST_2007_07_15_12_30_40_987654321.toLocalDate.minusDays(1))
  }

  @Test def test_minusSeconds_one(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val d: LocalDate = t.toLocalDate.minusDays(1)
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0

    {
      var i: Int = 0
      while (i < 3700) {
        {
          t = t.minusSeconds(1)
          sec -= 1
          if (sec == -1) {
            min -= 1
            sec = 59
            if (min == -1) {
              hour -= 1
              min = 59
              if (hour == -1) {
                hour = 23
              }
            }
          }
          assertEquals(t.toLocalDate, d)
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
          assertEquals(t.getSecond, sec)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @DataProvider(name = "minusSeconds_fromZero") private[bp] def minusSeconds_fromZero: java.util.Iterator[Array[Any]] = {
    new java.util.Iterator[Array[Any]]() {
      private[bp] var delta: Int = 30
      private[bp] var i: Int = 3660
      private[bp] var date: LocalDate = TEST_2007_07_15_12_30_40_987654321.toLocalDate.minusDays(1)
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0

      def hasNext: Boolean = i >= -3660

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, date, hour, min, sec)
        i -= delta
        sec += delta
        if (sec >= 60) {
          min += 1
          sec -= 60
          if (min == 60) {
            hour += 1
            min = 0
            if (hour == 24) {
              hour = 0
            }
          }
        }
        if (i == 0)
          date = date.plusDays(1)
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "minusSeconds_fromZero") def test_minusSeconds_fromZero(seconds: Int, date: LocalDate, hour: Int, min: Int, sec: Int): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val t: LocalDateTime = base.minusSeconds(seconds)
    assertEquals(date, t.toLocalDate)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
  }

  @Test def test_minusNanos_halfABillion(): Unit = {
    var t: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val d: LocalDate = t.toLocalDate.minusDays(1)
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0
    var nanos: Int = 0
    var i: Long = 0
    while (i < 3700 * 1000000000L) {
      t = t.minusNanos(500000000)
      nanos -= 500000000
      if (nanos < 0) {
        sec -= 1
        nanos += 1000000000
        if (sec == -1) {
          min -= 1
          sec += 60
          if (min == -1) {
            hour -= 1
            min += 60
            if (hour == -1) {
              hour += 24
            }
          }
        }
      }
      assertEquals(t.toLocalDate, d)
      assertEquals(t.getHour, hour)
      assertEquals(t.getMinute, min)
      assertEquals(t.getSecond, sec)
      assertEquals(t.getNano, nanos)
      i += 500000000
    }
  }

  @DataProvider(name = "minusNanos_fromZero") private[bp] def minusNanos_fromZero: java.util.Iterator[Array[Any]] = {
    new java.util.Iterator[Array[Any]]() {
      private[bp] var delta: Long = 7500000000L
      private[bp] var i: Long = 3660 * 1000000000L
      private[bp] var date: LocalDate = TEST_2007_07_15_12_30_40_987654321.toLocalDate.minusDays(1)
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0
      private[bp] var nanos: Long = 0

      def hasNext: Boolean = i >= -3660 * 1000000000L

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, date, hour, min, sec, nanos.toInt)
        i -= delta
        nanos += delta
        if (nanos >= 1000000000L) {
          sec += (nanos / 1000000000L).toInt // !!!
          nanos %= 1000000000L
          if (sec >= 60) {
            min += 1
            sec %= 60
            if (min == 60) {
              hour += 1
              min = 0
              if (hour == 24) {
                hour = 0
                date = date.plusDays(1)
              }
            }
          }
        }
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "minusNanos_fromZero") def test_minusNanos_fromZero(nanoseconds: Long, date: LocalDate, hour: Int, min: Int, sec: Int, nanos: Int): Unit = {
    val base: LocalDateTime = TEST_2007_07_15_12_30_40_987654321.`with`(LocalTime.MIDNIGHT)
    val t: LocalDateTime = base.minusNanos(nanoseconds)
    assertEquals(date, t.toLocalDate)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
    assertEquals(nanos, t.getNano)
  }

  @DataProvider(name = "until") private[bp] def provider_until: Array[Array[Any]] = {
    Array[Array[Any]](Array("2012-06-15T00:00", "2012-06-15T00:00", NANOS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00", MICROS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00", MILLIS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00", SECONDS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00", MINUTES, 0), Array("2012-06-15T00:00", "2012-06-15T00:00", HOURS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00", HALF_DAYS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00:01", NANOS, 1000000000), Array("2012-06-15T00:00", "2012-06-15T00:00:01", MICROS, 1000000), Array("2012-06-15T00:00", "2012-06-15T00:00:01", MILLIS, 1000), Array("2012-06-15T00:00", "2012-06-15T00:00:01", SECONDS, 1), Array("2012-06-15T00:00", "2012-06-15T00:00:01", MINUTES, 0), Array("2012-06-15T00:00", "2012-06-15T00:00:01", HOURS, 0), Array("2012-06-15T00:00", "2012-06-15T00:00:01", HALF_DAYS, 0), Array("2012-06-15T00:00", "2012-06-15T00:01", NANOS, 60000000000L), Array("2012-06-15T00:00", "2012-06-15T00:01", MICROS, 60000000), Array("2012-06-15T00:00", "2012-06-15T00:01", MILLIS, 60000), Array("2012-06-15T00:00", "2012-06-15T00:01", SECONDS, 60), Array("2012-06-15T00:00", "2012-06-15T00:01", MINUTES, 1), Array("2012-06-15T00:00", "2012-06-15T00:01", HOURS, 0), Array("2012-06-15T00:00", "2012-06-15T00:01", HALF_DAYS, 0), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:39.499", SECONDS, -1), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:39.500", SECONDS, -1), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:39.501", SECONDS, 0), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:40.499", SECONDS, 0), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:40.500", SECONDS, 0), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:40.501", SECONDS, 0), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:41.499", SECONDS, 0), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:41.500", SECONDS, 1), Array("2012-06-15T12:30:40.500", "2012-06-15T12:30:41.501", SECONDS, 1), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:39.499", SECONDS, 86400 - 2), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:39.500", SECONDS, 86400 - 1), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:39.501", SECONDS, 86400 - 1), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:40.499", SECONDS, 86400 - 1), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:40.500", SECONDS, 86400 + 0), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:40.501", SECONDS, 86400 + 0), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:41.499", SECONDS, 86400 + 0), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:41.500", SECONDS, 86400 + 1), Array("2012-06-15T12:30:40.500", "2012-06-16T12:30:41.501", SECONDS, 86400 + 1))
  }

  @Test(dataProvider = "until") def test_until(startStr: String, endStr: String, unit: TemporalUnit, expected: Long): Unit = {
    val start: LocalDateTime = LocalDateTime.parse(startStr)
    val end: LocalDateTime = LocalDateTime.parse(endStr)
    assertEquals(start.until(end, unit), expected)
  }

  @Test(dataProvider = "until") def test_until_reveresed(startStr: String, endStr: String, unit: TemporalUnit, expected: Long): Unit = {
    val start: LocalDateTime = LocalDateTime.parse(startStr)
    val end: LocalDateTime = LocalDateTime.parse(endStr)
    assertEquals(end.until(start, unit), -expected)
  }

  @Test def test_atZone(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2008, 6, 30, 11, 30)
    assertEquals(t.atZone(TestLocalDateTime.ZONE_PARIS), ZonedDateTime.of(LocalDateTime.of(2008, 6, 30, 11, 30), TestLocalDateTime.ZONE_PARIS))
  }

  @Test def test_atZone_Offset(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2008, 6, 30, 11, 30)
    assertEquals(t.atZone(TestLocalDateTime.OFFSET_PTWO), ZonedDateTime.of(LocalDateTime.of(2008, 6, 30, 11, 30), TestLocalDateTime.OFFSET_PTWO))
  }

  @Test def test_atZone_dstGap(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2007, 4, 1, 0, 0)
    assertEquals(t.atZone(TestLocalDateTime.ZONE_GAZA), ZonedDateTime.of(LocalDateTime.of(2007, 4, 1, 1, 0), TestLocalDateTime.ZONE_GAZA))
  }

  @Test def test_atZone_dstOverlap(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2007, 10, 28, 2, 30)
    assertEquals(t.atZone(TestLocalDateTime.ZONE_PARIS), ZonedDateTime.ofStrict(LocalDateTime.of(2007, 10, 28, 2, 30), TestLocalDateTime.OFFSET_PTWO, TestLocalDateTime.ZONE_PARIS))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atZone_nullTimeZone(): Unit = {
    val t: LocalDateTime = LocalDateTime.of(2008, 6, 30, 11, 30)
    t.atZone(null.asInstanceOf[ZoneId])
  }

  @Test def test_toEpochSecond_afterEpoch(): Unit = {
    var i: Int = -5
    while (i < 5) {
      val offset: ZoneOffset = ZoneOffset.ofHours(i)
      var j: Int = 0
      while (j < 100000) {
        val a: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0).plusSeconds(j)
        assertEquals(a.toEpochSecond(offset), j - i * 3600)
        j += 1
      }
      i += 1
    }
  }

  @Test def test_toEpochSecond_beforeEpoch(): Unit = {
    var i: Int = 0
    while (i < 100000) {
      val a: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0).minusSeconds(i)
      assertEquals(a.toEpochSecond(ZoneOffset.UTC), -i)
      i += 1
    }
  }

  @Test def test_comparisons(): Unit = {
    test_comparisons_LocalDateTime(LocalDate.of(Year.MIN_VALUE, 1, 1), LocalDate.of(Year.MIN_VALUE, 12, 31), LocalDate.of(-1, 1, 1), LocalDate.of(-1, 12, 31), LocalDate.of(0, 1, 1), LocalDate.of(0, 12, 31), LocalDate.of(1, 1, 1), LocalDate.of(1, 12, 31), LocalDate.of(2008, 1, 1), LocalDate.of(2008, 2, 29), LocalDate.of(2008, 12, 31), LocalDate.of(Year.MAX_VALUE, 1, 1), LocalDate.of(Year.MAX_VALUE, 12, 31))
  }

  private def test_comparisons_LocalDateTime(localDates: LocalDate*): Unit = {
    test_comparisons_LocalDateTime(localDates.toArray, LocalTime.MIDNIGHT, LocalTime.of(0, 0, 0, 999999999), LocalTime.of(0, 0, 59, 0), LocalTime.of(0, 0, 59, 999999999), LocalTime.of(0, 59, 0, 0), LocalTime.of(0, 59, 59, 999999999), LocalTime.NOON, LocalTime.of(12, 0, 0, 999999999), LocalTime.of(12, 0, 59, 0), LocalTime.of(12, 0, 59, 999999999), LocalTime.of(12, 59, 0, 0), LocalTime.of(12, 59, 59, 999999999), LocalTime.of(23, 0, 0, 0), LocalTime.of(23, 0, 0, 999999999), LocalTime.of(23, 0, 59, 0), LocalTime.of(23, 0, 59, 999999999), LocalTime.of(23, 59, 0, 0), LocalTime.of(23, 59, 59, 999999999))
  }

  private def test_comparisons_LocalDateTime(localDates: Array[LocalDate], localTimes: LocalTime*): Unit = {
    val localDateTimes: Array[LocalDateTime] = new Array[LocalDateTime](localDates.length * localTimes.length)
    var i: Int = 0
    for (localDate <- localDates) {
      for (localTime <- localTimes) {
        localDateTimes({
          i += 1
          i - 1
        }) = LocalDateTime.of(localDate, localTime)
      }
    }
    doTest_comparisons_LocalDateTime(localDateTimes)
  }

  private def doTest_comparisons_LocalDateTime(localDateTimes: Array[LocalDateTime]): Unit = {
    var i: Int = 0
    while (i < localDateTimes.length) {
      val a: LocalDateTime = localDateTimes(i)
      var j: Int = 0
      while (j < localDateTimes.length) {
        val b: LocalDateTime = localDateTimes(j)
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
        j += 1
      }
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_ObjectNull(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.compareTo(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_ObjectNull(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_ObjectNull(): Unit = {
    TEST_2007_07_15_12_30_40_987654321.isAfter(null)
  }

  @DataProvider(name = "sampleDateTimes") private[bp] def provider_sampleDateTimes: java.util.Iterator[Array[AnyRef]] = {
    new java.util.Iterator[Array[AnyRef]]() {
      private[bp] var sampleDates: Array[Array[AnyRef]] = provider_sampleDates
      private[bp] var sampleTimes: Array[Array[AnyRef]] = provider_sampleTimes
      private[bp] var datesIndex: Int = 0
      private[bp] var timesIndex: Int = 0

      def hasNext: Boolean = datesIndex < sampleDates.length

      def next: Array[AnyRef] = {
        val sampleDate: Array[AnyRef] = sampleDates(datesIndex)
        val sampleTime: Array[AnyRef] = sampleTimes(timesIndex)
        val ret: Array[AnyRef] = new Array[AnyRef](sampleDate.length + sampleTime.length)
        System.arraycopy(sampleDate, 0, ret, 0, sampleDate.length)
        System.arraycopy(sampleTime, 0, ret, sampleDate.length, sampleTime.length)
        if ( {
          timesIndex += 1
          timesIndex
        } == sampleTimes.length) {
          datesIndex += 1
          timesIndex = 0
        }
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_true(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    assertTrue(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_year_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y + 1, m, d, h, mi, s, n)
    assertFalse(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_month_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m + 1, d, h, mi, s, n)
    assertFalse(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_day_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m, d + 1, h, mi, s, n)
    assertFalse(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_hour_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m, d, h + 1, mi, s, n)
    assertFalse(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_minute_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m, d, h, mi + 1, s, n)
    assertFalse(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_second_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s + 1, n)
    assertFalse(a == b)
  }

  @Test(dataProvider = "sampleDateTimes") def test_equals_false_nano_differs(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val b: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n + 1)
    assertFalse(a == b)
  }

  @Test def test_equals_itself_true(): Unit = {
    assertEquals(TEST_2007_07_15_12_30_40_987654321 == TEST_2007_07_15_12_30_40_987654321, true)
  }

  @Test def test_equals_string_false(): Unit = {
    assertEquals(TEST_2007_07_15_12_30_40_987654321 == "2007-07-15T12:30:40.987654321", false)
  }

  @Test def test_equals_null_false(): Unit = {
    assertEquals(TEST_2007_07_15_12_30_40_987654321 == null, false)
  }

  @Test(dataProvider = "sampleDateTimes") def test_hashCode(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int): Unit = {
    val a: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    assertEquals(a.hashCode, a.hashCode)
    val b: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    assertEquals(a.hashCode, b.hashCode)
  }

  @DataProvider(name = "sampleToString") private[bp] def provider_sampleToString: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, 7, 5, 2, 1, 0, 0, "2008-07-05T02:01"), Array(2007, 12, 31, 23, 59, 1, 0, "2007-12-31T23:59:01"), Array(999, 12, 31, 23, 59, 59, 990000000, "0999-12-31T23:59:59.990"), Array(-1, 1, 2, 23, 59, 59, 999990000, "-0001-01-02T23:59:59.999990"), Array(-2008, 1, 2, 23, 59, 59, 999999990, "-2008-01-02T23:59:59.999999990"))
  }

  @Test(dataProvider = "sampleToString") def test_toString(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, n: Int, expected: String): Unit = {
    val t: LocalDateTime = LocalDateTime.of(y, m, d, h, mi, s, n)
    val str: String = t.toString
    assertEquals(str, expected)
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("y M d H m s")
    val t: String = LocalDateTime.of(2010, 12, 3, 11, 30, 45).format(f)
    assertEquals(t, "2010 12 3 11 30 45")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_format_formatter_null(): Unit = {
    LocalDateTime.of(2010, 12, 3, 11, 30, 45).format(null)
  }
}
