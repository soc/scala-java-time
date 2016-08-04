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
import org.testng.Assert.assertTrue
import org.threeten.bp.Month.DECEMBER
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
import org.threeten.bp.temporal.ChronoField.PROLEPTIC_MONTH
import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.HOUR_OF_AMPM
import org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.INSTANT_SECONDS
import org.threeten.bp.temporal.ChronoField.MICRO_OF_DAY
import org.threeten.bp.temporal.ChronoField.MICRO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.MILLI_OF_DAY
import org.threeten.bp.temporal.ChronoField.MILLI_OF_SECOND
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_DAY
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import org.threeten.bp.temporal.ChronoField.NANO_OF_DAY
import org.threeten.bp.temporal.ChronoField.NANO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.OFFSET_SECONDS
import org.threeten.bp.temporal.ChronoField.SECOND_OF_DAY
import org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE
import org.threeten.bp.temporal.ChronoField.YEAR
import org.threeten.bp.temporal.ChronoField.YEAR_OF_ERA
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.NANOS
import org.threeten.bp.temporal.ChronoUnit.SECONDS
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.JulianFields
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries

/** Test OffsetDateTime. */
object TestOffsetDateTime {
  private val ZONE_PARIS: ZoneId = ZoneId.of("Europe/Paris")
  private val ZONE_GAZA: ZoneId = ZoneId.of("Asia/Gaza")
  private val OFFSET_PONE: ZoneOffset = ZoneOffset.ofHours(1)
  private val OFFSET_PTWO: ZoneOffset = ZoneOffset.ofHours(2)
  private val OFFSET_MONE: ZoneOffset = ZoneOffset.ofHours(-1)
  private val OFFSET_MTWO: ZoneOffset = ZoneOffset.ofHours(-2)
}

@Test class TestOffsetDateTime extends AbstractDateTimeTest {
  private var TEST_2008_6_30_11_30_59_000000500: OffsetDateTime = null

  @BeforeMethod def setUp(): Unit = {
    TEST_2008_6_30_11_30_59_000000500 = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PONE)
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_2008_6_30_11_30_59_000000500, OffsetDateTime.MIN, OffsetDateTime.MAX)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(NANO_OF_SECOND, NANO_OF_DAY, MICRO_OF_SECOND, MICRO_OF_DAY, MILLI_OF_SECOND, MILLI_OF_DAY, SECOND_OF_MINUTE, SECOND_OF_DAY, MINUTE_OF_HOUR, MINUTE_OF_DAY, CLOCK_HOUR_OF_AMPM, HOUR_OF_AMPM, CLOCK_HOUR_OF_DAY, HOUR_OF_DAY, AMPM_OF_DAY, DAY_OF_WEEK, ALIGNED_DAY_OF_WEEK_IN_MONTH, ALIGNED_DAY_OF_WEEK_IN_YEAR, DAY_OF_MONTH, DAY_OF_YEAR, EPOCH_DAY, ALIGNED_WEEK_OF_MONTH, ALIGNED_WEEK_OF_YEAR, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR_OF_ERA, YEAR, ERA, OFFSET_SECONDS, INSTANT_SECONDS, JulianFields.JULIAN_DAY, JulianFields.MODIFIED_JULIAN_DAY, JulianFields.RATA_DIE)
    Arrays.asList(array: _*)
  }

  protected def invalidFields: java.util.List[TemporalField] = {
    val list: java.util.List[TemporalField] = new java.util.ArrayList[TemporalField](Arrays.asList[TemporalField](ChronoField.values: _*))
    list.removeAll(validFields)
    list
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(TEST_2008_6_30_11_30_59_000000500)
    AbstractTest.assertSerializable(OffsetDateTime.MIN)
    AbstractTest.assertSerializable(OffsetDateTime.MAX)
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization_format(): Unit = {
    val date: LocalDate = LocalDate.of(2012, 9, 16)
    val time: LocalTime = LocalTime.of(22, 17, 59, 464 * 1000000)
    val offset: ZoneOffset = ZoneOffset.of("+01:00")
    AbstractTest.assertEqualsSerialisedForm(OffsetDateTime.of(date, time, offset))
  }

  @Test def now(): Unit = {
    var expected: OffsetDateTime = OffsetDateTime.now(Clock.systemDefaultZone)
    var test: OffsetDateTime = OffsetDateTime.now
    var diff: Long = Math.abs(test.toLocalTime.toNanoOfDay - expected.toLocalTime.toNanoOfDay)
    if (diff >= 100000000) {
      expected = OffsetDateTime.now(Clock.systemDefaultZone)
      test = OffsetDateTime.now
      diff = Math.abs(test.toLocalTime.toNanoOfDay - expected.toLocalTime.toNanoOfDay)
    }
    assertTrue(diff < 100000000)
  }

  @Test def now_Clock_allSecsInDay_utc(): Unit = {
    {
      var i: Int = 0
      while (i < (2 * 24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
          val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
          val test: OffsetDateTime = OffsetDateTime.now(clock)
          assertEquals(test.getYear, 1970)
          assertEquals(test.getMonth, Month.JANUARY)
          assertEquals(test.getDayOfMonth, if (i < 24 * 60 * 60) 1 else 2)
          assertEquals(test.getHour, (i / (60 * 60)) % 24)
          assertEquals(test.getMinute, (i / 60) % 60)
          assertEquals(test.getSecond, i % 60)
          assertEquals(test.getNano, 123456789)
          assertEquals(test.getOffset, ZoneOffset.UTC)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def now_Clock_allSecsInDay_offset(): Unit = {
    {
      var i: Int = 0
      while (i < (2 * 24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
          val clock: Clock = Clock.fixed(instant.minusSeconds(TestOffsetDateTime.OFFSET_PONE.getTotalSeconds), TestOffsetDateTime.OFFSET_PONE)
          val test: OffsetDateTime = OffsetDateTime.now(clock)
          assertEquals(test.getYear, 1970)
          assertEquals(test.getMonth, Month.JANUARY)
          assertEquals(test.getDayOfMonth, if (i < 24 * 60 * 60) 1 else 2)
          assertEquals(test.getHour, (i / (60 * 60)) % 24)
          assertEquals(test.getMinute, (i / 60) % 60)
          assertEquals(test.getSecond, i % 60)
          assertEquals(test.getNano, 123456789)
          assertEquals(test.getOffset, TestOffsetDateTime.OFFSET_PONE)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def now_Clock_allSecsInDay_beforeEpoch(): Unit = {
    var expected: LocalTime = LocalTime.MIDNIGHT.plusNanos(123456789L)

    {
      var i: Int = -1
      while (i >= -(24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
          val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
          val test: OffsetDateTime = OffsetDateTime.now(clock)
          assertEquals(test.getYear, 1969)
          assertEquals(test.getMonth, Month.DECEMBER)
          assertEquals(test.getDayOfMonth, 31)
          expected = expected.minusSeconds(1)
          assertEquals(test.toLocalTime, expected)
          assertEquals(test.getOffset, ZoneOffset.UTC)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test def now_Clock_offsets(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC)

    {
      var i: Int = -9
      while (i < 15) {
        {
          val offset: ZoneOffset = ZoneOffset.ofHours(i)
          val clock: Clock = Clock.fixed(base.toInstant, offset)
          val test: OffsetDateTime = OffsetDateTime.now(clock)
          assertEquals(test.getHour, (12 + i) % 24)
          assertEquals(test.getMinute, 0)
          assertEquals(test.getSecond, 0)
          assertEquals(test.getNano, 0)
          assertEquals(test.getOffset, offset)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullZoneId(): Unit = {
    OffsetDateTime.now(null.asInstanceOf[ZoneId])
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    OffsetDateTime.now(null.asInstanceOf[Clock])
  }

  private def check(test: OffsetDateTime, y: Int, mo: Int, d: Int, h: Int, m: Int, s: Int, n: Int, offset: ZoneOffset): Unit = {
    assertEquals(test.getYear, y)
    assertEquals(test.getMonth.getValue, mo)
    assertEquals(test.getDayOfMonth, d)
    assertEquals(test.getHour, h)
    assertEquals(test.getMinute, m)
    assertEquals(test.getSecond, s)
    assertEquals(test.getNano, n)
    assertEquals(test.getOffset, offset)
    assertEquals(test, test)
    assertEquals(test.hashCode, test.hashCode)
    assertEquals(OffsetDateTime.of(LocalDateTime.of(y, mo, d, h, m, s, n), offset), test)
  }

  @Test def factory_of_intMonthIntHM(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, Month.JUNE, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 0, 0, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test def factory_of_intMonthIntHMS(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, Month.JUNE, 30), LocalTime.of(11, 30, 10), TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 10, 0, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test def factory_of_intMonthIntHMSN(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, Month.JUNE, 30), LocalTime.of(11, 30, 10, 500), TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 10, 500, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test def factory_of_intsHM(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 0, 0, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test def factory_of_intsHMS(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 10), TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 10, 0, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test def factory_of_intsHMSN(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 10, 500), TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 10, 500, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test def factory_of_LocalDateLocalTimeZoneOffset(): Unit = {
    val date: LocalDate = LocalDate.of(2008, 6, 30)
    val time: LocalTime = LocalTime.of(11, 30, 10, 500)
    val test: OffsetDateTime = OffsetDateTime.of(date, time, TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 10, 500, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDateLocalTimeZoneOffset_nullLocalDate(): Unit = {
    val time: LocalTime = LocalTime.of(11, 30, 10, 500)
    OffsetDateTime.of(null.asInstanceOf[LocalDate], time, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDateLocalTimeZoneOffset_nullLocalTime(): Unit = {
    val date: LocalDate = LocalDate.of(2008, 6, 30)
    OffsetDateTime.of(date, null.asInstanceOf[LocalTime], TestOffsetDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDateLocalTimeZoneOffset_nullOffset(): Unit = {
    val date: LocalDate = LocalDate.of(2008, 6, 30)
    val time: LocalTime = LocalTime.of(11, 30, 10, 500)
    OffsetDateTime.of(date, time, null.asInstanceOf[ZoneOffset])
  }

  @Test def factory_of_LocalDateTimeZoneOffset(): Unit = {
    val dt: LocalDateTime = LocalDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 10, 500))
    val test: OffsetDateTime = OffsetDateTime.of(dt, TestOffsetDateTime.OFFSET_PONE)
    check(test, 2008, 6, 30, 11, 30, 10, 500, TestOffsetDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDateTimeZoneOffset_nullProvider(): Unit = {
    OffsetDateTime.of(null.asInstanceOf[LocalDateTime], TestOffsetDateTime.OFFSET_PONE)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_LocalDateTimeZoneOffset_nullOffset(): Unit = {
    val dt: LocalDateTime = LocalDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 10, 500))
    OffsetDateTime.of(dt, null.asInstanceOf[ZoneOffset])
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(OffsetDateTime.from(OffsetDateTime.of(LocalDate.of(2007, 7, 15), LocalTime.of(17, 30), TestOffsetDateTime.OFFSET_PONE)), OffsetDateTime.of(LocalDate.of(2007, 7, 15), LocalTime.of(17, 30), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    OffsetDateTime.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_Calendricals_null(): Unit = {
    OffsetDateTime.from(null.asInstanceOf[TemporalAccessor])
  }

  @Test(dataProvider = "sampleToString") def test_parse(y: Int, month: Int, d: Int, h: Int, m: Int, s: Int, n: Int, offsetId: String, text: String): Unit = {
    val t: OffsetDateTime = OffsetDateTime.parse(text)
    assertEquals(t.getYear, y)
    assertEquals(t.getMonth.getValue, month)
    assertEquals(t.getDayOfMonth, d)
    assertEquals(t.getHour, h)
    assertEquals(t.getMinute, m)
    assertEquals(t.getSecond, s)
    assertEquals(t.getNano, n)
    assertEquals(t.getOffset.getId, offsetId)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalValue(): Unit = {
    OffsetDateTime.parse("2008-06-32T11:15+01:00")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_invalidValue(): Unit = {
    OffsetDateTime.parse("2008-06-31T11:15+01:00")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    OffsetDateTime.parse(null.asInstanceOf[String])
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M d H m s XXX")
    val test: OffsetDateTime = OffsetDateTime.parse("2010 12 3 11 30 0 +01:00", f)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2010, 12, 3), LocalTime.of(11, 30), ZoneOffset.ofHours(1)))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M d H m s")
    OffsetDateTime.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    OffsetDateTime.parse("ANY", null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Throwable])
  def constructor_nullTime(): Unit = {
    val con: Constructor[OffsetDateTime] = classOf[OffsetDateTime].getDeclaredConstructor(classOf[LocalDateTime], classOf[ZoneOffset])
    con.setAccessible(true)
    try con.newInstance(null, TestOffsetDateTime.OFFSET_PONE)
    catch {
      case ex: InvocationTargetException =>
        throw ex.getCause
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]))
  @throws(classOf[Throwable])
  def constructor_nullOffset(): Unit = {
    val con: Constructor[OffsetDateTime] = classOf[OffsetDateTime].getDeclaredConstructor(classOf[LocalDateTime], classOf[ZoneOffset])
    con.setAccessible(true)
    try con.newInstance(LocalDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30)), null)
    catch {
      case ex: InvocationTargetException =>
        throw ex.getCause
    }
  }

  @DataProvider(name = "sampleTimes") private[bp] def provider_sampleTimes: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, 6, 30, 11, 30, 20, 500, TestOffsetDateTime.OFFSET_PONE), Array(2008, 6, 30, 11, 0, 0, 0, TestOffsetDateTime.OFFSET_PONE), Array(2008, 6, 30, 23, 59, 59, 999999999, TestOffsetDateTime.OFFSET_PONE), Array(-1, 1, 1, 0, 0, 0, 0, TestOffsetDateTime.OFFSET_PONE))
  }

  @Test(dataProvider = "sampleTimes") def test_get(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, offset: ZoneOffset): Unit = {
    val localDate: LocalDate = LocalDate.of(y, o, d)
    val localTime: LocalTime = LocalTime.of(h, m, s, n)
    val localDateTime: LocalDateTime = LocalDateTime.of(localDate, localTime)
    val a: OffsetDateTime = OffsetDateTime.of(localDateTime, offset)
    assertEquals(a.getYear, localDate.getYear)
    assertEquals(a.getMonth, localDate.getMonth)
    assertEquals(a.getDayOfMonth, localDate.getDayOfMonth)
    assertEquals(a.getDayOfYear, localDate.getDayOfYear)
    assertEquals(a.getDayOfWeek, localDate.getDayOfWeek)
    assertEquals(a.getHour, localDateTime.getHour)
    assertEquals(a.getMinute, localDateTime.getMinute)
    assertEquals(a.getSecond, localDateTime.getSecond)
    assertEquals(a.getNano, localDateTime.getNano)
    assertEquals(a.toOffsetTime, OffsetTime.of(localTime, offset))
    assertEquals(a.toString, localDateTime.toString + offset.toString)
  }

  @Test def test_get_TemporalField(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(12, 30, 40, 987654321), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(test.get(ChronoField.YEAR), 2008)
    assertEquals(test.get(ChronoField.MONTH_OF_YEAR), 6)
    assertEquals(test.get(ChronoField.DAY_OF_MONTH), 30)
    assertEquals(test.get(ChronoField.DAY_OF_WEEK), 1)
    assertEquals(test.get(ChronoField.DAY_OF_YEAR), 182)
    assertEquals(test.get(ChronoField.HOUR_OF_DAY), 12)
    assertEquals(test.get(ChronoField.MINUTE_OF_HOUR), 30)
    assertEquals(test.get(ChronoField.SECOND_OF_MINUTE), 40)
    assertEquals(test.get(ChronoField.NANO_OF_SECOND), 987654321)
    assertEquals(test.get(ChronoField.HOUR_OF_AMPM), 0)
    assertEquals(test.get(ChronoField.AMPM_OF_DAY), 1)
    assertEquals(test.get(ChronoField.OFFSET_SECONDS), 3600)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    val test: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(12, 30, 40, 987654321), TestOffsetDateTime.OFFSET_PONE)
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
    assertEquals(test.getLong(ChronoField.INSTANT_SECONDS), test.toEpochSecond)
    assertEquals(test.getLong(ChronoField.OFFSET_SECONDS), 3600)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.localDate), TEST_2008_6_30_11_30_59_000000500.toLocalDate)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.localTime), TEST_2008_6_30_11_30_59_000000500.toLocalTime)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.offset), TEST_2008_6_30_11_30_59_000000500.getOffset)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.precision), ChronoUnit.NANOS)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.zone), TEST_2008_6_30_11_30_59_000000500.getOffset)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_2008_6_30_11_30_59_000000500.query(null)
  }

  @Test def test_with_adjustment(): Unit = {
    val sample: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2012, 3, 4), LocalTime.of(23, 5), TestOffsetDateTime.OFFSET_PONE)
    val adjuster: TemporalAdjuster = new TemporalAdjuster {
      override def adjustInto(temporal: Temporal): Temporal = sample
    }
    assertEquals(TEST_2008_6_30_11_30_59_000000500.`with`(adjuster), sample)
  }

  @Test def test_with_adjustment_LocalDate(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(LocalDate.of(2012, 9, 3))
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2012, 9, 3), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_with_adjustment_LocalTime(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(LocalTime.of(19, 15))
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(19, 15), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_with_adjustment_LocalDateTime(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(LocalDateTime.of(LocalDate.of(2012, 9, 3), LocalTime.of(19, 15)))
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2012, 9, 3), LocalTime.of(19, 15), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_with_adjustment_OffsetTime(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(OffsetTime.of(LocalTime.of(19, 15), TestOffsetDateTime.OFFSET_PTWO))
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(19, 15), TestOffsetDateTime.OFFSET_PTWO))
  }

  @Test def test_with_adjustment_OffsetDateTime(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(OffsetDateTime.of(LocalDate.of(2012, 9, 3), LocalTime.of(19, 15), TestOffsetDateTime.OFFSET_PTWO))
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2012, 9, 3), LocalTime.of(19, 15), TestOffsetDateTime.OFFSET_PTWO))
  }

  @Test def test_with_adjustment_Month(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(DECEMBER)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 12, 30), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_with_adjustment_ZoneOffset(): Unit = {
    val test: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.`with`(TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PTWO))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_adjustment_null(): Unit = {
    TEST_2008_6_30_11_30_59_000000500.`with`(null.asInstanceOf[TemporalAdjuster])
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_withOffsetSameLocal_null(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    base.withOffsetSameLocal(null)
  }

  @Test def test_withOffsetSameInstant(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withOffsetSameInstant(TestOffsetDateTime.OFFSET_PTWO)
    val expected: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(12, 30, 59), TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_withOffsetSameInstant_null(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    base.withOffsetSameInstant(null)
  }

  @Test def test_withYear_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withYear(2007)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2007, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_withMonth_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withMonth(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 1, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_withDayOfMonth_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withDayOfMonth(15)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 15), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_withDayOfYear_normal(): Unit = {
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.withDayOfYear(33)
    assertEquals(t, OffsetDateTime.of(LocalDate.of(2008, 2, 2), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfYear_illegal(): Unit = {
    TEST_2008_6_30_11_30_59_000000500.withDayOfYear(367)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfYear_invalid(): Unit = {
    OffsetDateTime.of(LocalDate.of(2007, 2, 2), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PONE).withDayOfYear(366)
  }

  @Test def test_withHour_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withHour(15)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(15, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_withMinute_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withMinute(15)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 15, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_withSecond_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withSecond(15)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 15), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_withNanoOfSecond_normal(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 1), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.withNano(15)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 15), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_truncatedTo_normal(): Unit = {
    assertEquals(TEST_2008_6_30_11_30_59_000000500.truncatedTo(NANOS), TEST_2008_6_30_11_30_59_000000500)
    assertEquals(TEST_2008_6_30_11_30_59_000000500.truncatedTo(SECONDS), TEST_2008_6_30_11_30_59_000000500.withNano(0))
    assertEquals(TEST_2008_6_30_11_30_59_000000500.truncatedTo(DAYS), TEST_2008_6_30_11_30_59_000000500.`with`(LocalTime.MIDNIGHT))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_truncatedTo_null(): Unit = {
    TEST_2008_6_30_11_30_59_000000500.truncatedTo(null)
  }

  @Test def test_plus_Period(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.plus(period)
    assertEquals(t, OffsetDateTime.of(LocalDate.of(2009, 1, 30), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plus_Duration(): Unit = {
    val dur: Duration = Duration.ofSeconds(62, 3)
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.plus(dur)
    assertEquals(t, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 32, 1, 503), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plus_Duration_zero(): Unit = {
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.plus(Duration.ZERO)
    assertEquals(t, TEST_2008_6_30_11_30_59_000000500)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_Duration_null(): Unit = {
    TEST_2008_6_30_11_30_59_000000500.plus(null.asInstanceOf[Duration])
  }

  @Test def test_plusYears(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusYears(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2009, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusMonths(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusMonths(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 7, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusWeeks(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusWeeks(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 7, 7), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusDays(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusDays(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 7, 1), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusHours(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusHours(13)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 7, 1), LocalTime.of(0, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusMinutes(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusMinutes(30)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(12, 0, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusSeconds(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusSeconds(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 31, 0), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_plusNanos(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 0), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.plusNanos(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 1), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minus_Period(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.minus(period)
    assertEquals(t, OffsetDateTime.of(LocalDate.of(2007, 11, 30), LocalTime.of(11, 30, 59, 500), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minus_Duration(): Unit = {
    val dur: Duration = Duration.ofSeconds(62, 3)
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.minus(dur)
    assertEquals(t, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 29, 57, 497), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minus_Duration_zero(): Unit = {
    val t: OffsetDateTime = TEST_2008_6_30_11_30_59_000000500.minus(Duration.ZERO)
    assertEquals(t, TEST_2008_6_30_11_30_59_000000500)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_Duration_null(): Unit = {
    TEST_2008_6_30_11_30_59_000000500.minus(null.asInstanceOf[Duration])
  }

  @Test def test_minusYears(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusYears(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2007, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusMonths(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusMonths(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 5, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusWeeks(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusWeeks(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 23), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusDays(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusDays(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 29), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusHours(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusHours(13)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 29), LocalTime.of(22, 30, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusMinutes(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusMinutes(30)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 0, 59), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusSeconds(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusSeconds(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 58), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_minusNanos(): Unit = {
    val base: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 0), TestOffsetDateTime.OFFSET_PONE)
    val test: OffsetDateTime = base.minusNanos(1)
    assertEquals(test, OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 58, 999999999), TestOffsetDateTime.OFFSET_PONE))
  }

  @Test def test_atZone(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_MTWO)
    assertEquals(t.atZoneSameInstant(TestOffsetDateTime.ZONE_PARIS), ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(15, 30)), TestOffsetDateTime.ZONE_PARIS))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atZone_nullTimeZone(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PTWO)
    t.atZoneSameInstant(null.asInstanceOf[ZoneId])
  }

  @Test def test_atZoneSimilarLocal(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_MTWO)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS), ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30)), TestOffsetDateTime.ZONE_PARIS))
  }

  @Test def test_atZoneSimilarLocal_dstGap(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2007, 4, 1), LocalTime.of(0, 0), TestOffsetDateTime.OFFSET_MTWO)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_GAZA), ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2007, 4, 1), LocalTime.of(1, 0)), TestOffsetDateTime.ZONE_GAZA))
  }

  @Test def test_atZone_dstOverlapSummer(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2007, 10, 28), LocalTime.of(2, 30), TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS).toLocalDateTime, t.toLocalDateTime)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS).getOffset, TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS).getZone, TestOffsetDateTime.ZONE_PARIS)
  }

  @Test def test_atZone_dstOverlapWinter(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2007, 10, 28), LocalTime.of(2, 30), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS).toLocalDateTime, t.toLocalDateTime)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS).getOffset, TestOffsetDateTime.OFFSET_PONE)
    assertEquals(t.atZoneSimilarLocal(TestOffsetDateTime.ZONE_PARIS).getZone, TestOffsetDateTime.ZONE_PARIS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atZoneSimilarLocal_nullTimeZone(): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PTWO)
    t.atZoneSimilarLocal(null.asInstanceOf[ZoneId])
  }

  @Test def test_toEpochSecond_afterEpoch(): Unit = {
    {
      var i: Int = 0
      while (i < 100000) {
        {
          val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0), ZoneOffset.UTC).plusSeconds(i)
          assertEquals(a.toEpochSecond, i)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_toEpochSecond_beforeEpoch(): Unit = {
    {
      var i: Int = 0
      while (i < 100000) {
        {
          val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0), ZoneOffset.UTC).minusSeconds(i)
          assertEquals(a.toEpochSecond, -i)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_compareTo_timeMins(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 29, 3), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 2), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_timeSecs(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 29, 2), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 29, 3), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_timeNanos(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 29, 40, 4), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 29, 40, 5), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_offset(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PTWO)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_offsetNanos(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 40, 6), TestOffsetDateTime.OFFSET_PTWO)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 40, 5), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_both(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 50), TestOffsetDateTime.OFFSET_PTWO)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 20), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_bothNanos(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 20, 40, 4), TestOffsetDateTime.OFFSET_PTWO)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(10, 20, 40, 5), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) < 0, true)
  }

  @Test def test_compareTo_hourDifference(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(10, 0), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 0), TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
    assertEquals(a.toInstant.compareTo(b.toInstant) == 0, true)
  }

  @Test def test_compareTo_max(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(Year.MAX_VALUE, 12, 31), LocalTime.of(23, 59), TestOffsetDateTime.OFFSET_MONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(Year.MAX_VALUE, 12, 31), LocalTime.of(23, 59), TestOffsetDateTime.OFFSET_MTWO)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
  }

  @Test def test_compareTo_min(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(Year.MIN_VALUE, 1, 1), LocalTime.of(0, 0), TestOffsetDateTime.OFFSET_PTWO)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(Year.MIN_VALUE, 1, 1), LocalTime.of(0, 0), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.compareTo(b) < 0, true)
    assertEquals(b.compareTo(a) > 0, true)
    assertEquals(a.compareTo(a) == 0, true)
    assertEquals(b.compareTo(b) == 0, true)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_null(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    a.compareTo(null)
  }

  @Test def test_isBeforeIsAfterIsEqual1(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 58, 3), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 2), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.isBefore(b), true)
    assertEquals(a.isEqual(b), false)
    assertEquals(a.isAfter(b), false)
    assertEquals(b.isBefore(a), false)
    assertEquals(b.isEqual(a), false)
    assertEquals(b.isAfter(a), true)
    assertEquals(a.isBefore(a), false)
    assertEquals(b.isBefore(b), false)
    assertEquals(a.isEqual(a), true)
    assertEquals(b.isEqual(b), true)
    assertEquals(a.isAfter(a), false)
    assertEquals(b.isAfter(b), false)
  }

  @Test def test_isBeforeIsAfterIsEqual2(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 2), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59, 3), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a.isBefore(b), true)
    assertEquals(a.isEqual(b), false)
    assertEquals(a.isAfter(b), false)
    assertEquals(b.isBefore(a), false)
    assertEquals(b.isEqual(a), false)
    assertEquals(b.isAfter(a), true)
    assertEquals(a.isBefore(a), false)
    assertEquals(b.isBefore(b), false)
    assertEquals(a.isEqual(a), true)
    assertEquals(b.isEqual(b), true)
    assertEquals(a.isAfter(a), false)
    assertEquals(b.isAfter(b), false)
  }

  @Test def test_isBeforeIsAfterIsEqual_instantComparison(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(10, 0), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 0), TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(a.isBefore(b), false)
    assertEquals(a.isEqual(b), true)
    assertEquals(a.isAfter(b), false)
    assertEquals(b.isBefore(a), false)
    assertEquals(b.isEqual(a), true)
    assertEquals(b.isAfter(a), false)
    assertEquals(a.isBefore(a), false)
    assertEquals(b.isBefore(b), false)
    assertEquals(a.isEqual(a), true)
    assertEquals(b.isEqual(b), true)
    assertEquals(a.isAfter(a), false)
    assertEquals(b.isAfter(b), false)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_null(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    a.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isEqual_null(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    a.isEqual(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_null(): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(2008, 6, 30), LocalTime.of(11, 30, 59), TestOffsetDateTime.OFFSET_PONE)
    a.isAfter(null)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_true(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a == b, true)
    assertEquals(a.hashCode == b.hashCode, true)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_year_differs(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y + 1, o, d), LocalTime.of(h, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_hour_differs(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    var _h = h
    _h = if (_h == 23) 22 else _h
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(_h, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(_h + 1, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_minute_differs(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    var _m = m
    _m = if (_m == 59) 58 else _m
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, _m, s, n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, _m + 1, s, n), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_second_differs(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    var _s = s
    _s = if (_s == 59) 58 else _s
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, _s, n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, _s + 1, n), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_nano_differs(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    var _n = n
    _n = if (_n == 999999999) 999999998 else _n
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, _n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, _n + 1), TestOffsetDateTime.OFFSET_PONE)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_offset_differs(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, ignored: ZoneOffset): Unit = {
    val a: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, n), TestOffsetDateTime.OFFSET_PONE)
    val b: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, n), TestOffsetDateTime.OFFSET_PTWO)
    assertEquals(a == b, false)
  }

  @Test def test_equals_itself_true(): Unit = {
    assertEquals(TEST_2008_6_30_11_30_59_000000500 == TEST_2008_6_30_11_30_59_000000500, true)
  }

  @Test def test_equals_string_false(): Unit = {
    assertEquals(TEST_2008_6_30_11_30_59_000000500 == "2007-07-15", false)
  }

  @Test def test_equals_null_false(): Unit = {
    assertEquals(TEST_2008_6_30_11_30_59_000000500 == null, false)
  }

  @DataProvider(name = "sampleToString") private[bp] def provider_sampleToString: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, 6, 30, 11, 30, 59, 0, "Z", "2008-06-30T11:30:59Z"), Array(2008, 6, 30, 11, 30, 59, 0, "+01:00", "2008-06-30T11:30:59+01:00"), Array(2008, 6, 30, 11, 30, 59, 999000000, "Z", "2008-06-30T11:30:59.999Z"), Array(2008, 6, 30, 11, 30, 59, 999000000, "+01:00", "2008-06-30T11:30:59.999+01:00"), Array(2008, 6, 30, 11, 30, 59, 999000, "Z", "2008-06-30T11:30:59.000999Z"), Array(2008, 6, 30, 11, 30, 59, 999000, "+01:00", "2008-06-30T11:30:59.000999+01:00"), Array(2008, 6, 30, 11, 30, 59, 999, "Z", "2008-06-30T11:30:59.000000999Z"), Array(2008, 6, 30, 11, 30, 59, 999, "+01:00", "2008-06-30T11:30:59.000000999+01:00"))
  }

  @Test(dataProvider = "sampleToString") def test_toString(y: Int, o: Int, d: Int, h: Int, m: Int, s: Int, n: Int, offsetId: String, expected: String): Unit = {
    val t: OffsetDateTime = OffsetDateTime.of(LocalDate.of(y, o, d), LocalTime.of(h, m, s, n), ZoneOffset.of(offsetId))
    val str: String = t.toString
    assertEquals(str, expected)
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("y M d H m s")
    val t: String = OffsetDateTime.of(LocalDate.of(2010, 12, 3), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PONE).format(f)
    assertEquals(t, "2010 12 3 11 30 0")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_format_formatter_null(): Unit = {
    OffsetDateTime.of(LocalDate.of(2010, 12, 3), LocalTime.of(11, 30), TestOffsetDateTime.OFFSET_PONE).format(null)
  }
}
