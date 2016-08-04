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
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import org.threeten.bp.temporal.ChronoField.AMPM_OF_DAY
import org.threeten.bp.temporal.ChronoField.CLOCK_HOUR_OF_AMPM
import org.threeten.bp.temporal.ChronoField.CLOCK_HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.HOUR_OF_AMPM
import org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.MICRO_OF_DAY
import org.threeten.bp.temporal.ChronoField.MICRO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.MILLI_OF_DAY
import org.threeten.bp.temporal.ChronoField.MILLI_OF_SECOND
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_DAY
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import org.threeten.bp.temporal.ChronoField.NANO_OF_DAY
import org.threeten.bp.temporal.ChronoField.NANO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.SECOND_OF_DAY
import org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.FOREVER
import org.threeten.bp.temporal.ChronoUnit.HALF_DAYS
import org.threeten.bp.temporal.ChronoUnit.HOURS
import org.threeten.bp.temporal.ChronoUnit.MICROS
import org.threeten.bp.temporal.ChronoUnit.MILLIS
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import org.threeten.bp.temporal.ChronoUnit.MONTHS
import org.threeten.bp.temporal.ChronoUnit.NANOS
import org.threeten.bp.temporal.ChronoUnit.SECONDS
import org.threeten.bp.temporal.ChronoUnit.WEEKS
import org.threeten.bp.temporal.ChronoUnit.YEARS
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.EnumSet
import java.util.Iterator
import java.util.List
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.JulianFields
import org.threeten.bp.temporal.MockFieldNoValue
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalAmount
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries
import org.threeten.bp.temporal.TemporalUnit
import org.threeten.bp.temporal.UnsupportedTemporalTypeException

/** Test LocalTime. */
object TestLocalTime {
  private val INVALID_UNITS: Array[TemporalUnit] =  {
    //val set: java.util.EnumSet[ChronoUnit] = EnumSet.range(WEEKS, FOREVER)
    //set.toArray(new Array[TemporalUnit](set.size)).asInstanceOf[Array[TemporalUnit]]
    // We can't use the code above, because ChronoUnit is not an enum (yet), because we can't define enums in Scala (yet).
    ChronoUnit.values.filter(unit => unit.ordinal >= WEEKS.ordinal && unit.ordinal <= FOREVER.ordinal).asInstanceOf[Array[TemporalUnit]]
  }
}

@Test class TestLocalTime extends AbstractDateTimeTest {
  private var TEST_12_30_40_987654321: LocalTime = null

  @BeforeMethod def setUp(): Unit = {
    TEST_12_30_40_987654321 = LocalTime.of(12, 30, 40, 987654321)
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_12_30_40_987654321, LocalTime.MIN, LocalTime.MAX, LocalTime.MIDNIGHT, LocalTime.NOON)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(NANO_OF_SECOND, NANO_OF_DAY, MICRO_OF_SECOND, MICRO_OF_DAY, MILLI_OF_SECOND, MILLI_OF_DAY, SECOND_OF_MINUTE, SECOND_OF_DAY, MINUTE_OF_HOUR, MINUTE_OF_DAY, CLOCK_HOUR_OF_AMPM, HOUR_OF_AMPM, CLOCK_HOUR_OF_DAY, HOUR_OF_DAY, AMPM_OF_DAY)
    Arrays.asList(array: _*)
  }

  protected def invalidFields: java.util.List[TemporalField] = {
    val list: java.util.List[TemporalField] = new java.util.ArrayList[TemporalField](Arrays.asList[TemporalField](ChronoField.values: _*))
    list.removeAll(validFields)
    list.add(JulianFields.JULIAN_DAY)
    list.add(JulianFields.MODIFIED_JULIAN_DAY)
    list.add(JulianFields.RATA_DIE)
    list
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization_format(): Unit = {
    AbstractTest.assertEqualsSerialisedForm(LocalTime.of(22, 17, 59, 460 * 1000000))
  }

  @Test
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(TEST_12_30_40_987654321)
  }

  private def check(time: LocalTime, h: Int, m: Int, s: Int, n: Int): Unit = {
    assertEquals(time.getHour, h)
    assertEquals(time.getMinute, m)
    assertEquals(time.getSecond, s)
    assertEquals(time.getNano, n)
  }

  @Test def constant_MIDNIGHT(): Unit = {
    check(LocalTime.MIDNIGHT, 0, 0, 0, 0)
  }

  @Test def constant_MIDNIGHT_equal(): Unit = {
    assertEquals(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)
    assertEquals(LocalTime.MIDNIGHT, LocalTime.of(0, 0))
  }

  @Test def constant_MIDDAY(): Unit = {
    check(LocalTime.NOON, 12, 0, 0, 0)
  }

  @Test def constant_MIDDAY_equal(): Unit = {
    assertEquals(LocalTime.NOON, LocalTime.NOON)
    assertEquals(LocalTime.NOON, LocalTime.of(12, 0))
  }

  @Test def constant_MIN_TIME(): Unit = {
    check(LocalTime.MIN, 0, 0, 0, 0)
  }

  @Test def constant_MIN_TIME_equal(): Unit = {
    assertEquals(LocalTime.MIN, LocalTime.of(0, 0))
  }

  @Test def constant_MAX_TIME(): Unit = {
    check(LocalTime.MAX, 23, 59, 59, 999999999)
  }

  @Test def constant_MAX_TIME_equal(): Unit = {
    assertEquals(LocalTime.NOON, LocalTime.NOON)
    assertEquals(LocalTime.NOON, LocalTime.of(12, 0))
  }

  @Test def now(): Unit = {
    val expected: LocalTime = LocalTime.now(Clock.systemDefaultZone)
    val test: LocalTime = LocalTime.now
    val diff: Long = Math.abs(test.toNanoOfDay - expected.toNanoOfDay)
    assertTrue(diff < 100000000)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_ZoneId_nullZoneId(): Unit = {
    LocalTime.now(null.asInstanceOf[ZoneId])
  }

  @Test def now_ZoneId(): Unit = {
    val zone: ZoneId = ZoneId.of("UTC+01:02:03")
    var expected: LocalTime = LocalTime.now(Clock.system(zone))
    var test: LocalTime = LocalTime.now(zone)

    {
      var i: Int = 0
      while (i < 100) {
        {
          if (expected == test) {
            return
          }
          expected = LocalTime.now(Clock.system(zone))
          test = LocalTime.now(zone)
        }
        {
          i += 1
          i - 1
        }
      }
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    LocalTime.now(null.asInstanceOf[Clock])
  }

  @Test def now_Clock_allSecsInDay(): Unit = {
    {
      var i: Int = 0
      while (i < (2 * 24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i, 8)
          val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
          val test: LocalTime = LocalTime.now(clock)
          assertEquals(test.getHour, (i / (60 * 60)) % 24)
          assertEquals(test.getMinute, (i / 60) % 60)
          assertEquals(test.getSecond, i % 60)
          assertEquals(test.getNano, 8)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def now_Clock_beforeEpoch(): Unit = {
    {
      var i: Int = -1
      while (i >= -(24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i, 8)
          val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
          val test: LocalTime = LocalTime.now(clock)
          assertEquals(test.getHour, ((i + 24 * 60 * 60) / (60 * 60)) % 24)
          assertEquals(test.getMinute, ((i + 24 * 60 * 60) / 60) % 60)
          assertEquals(test.getSecond, (i + 24 * 60 * 60) % 60)
          assertEquals(test.getNano, 8)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test def now_Clock_max(): Unit = {
    val clock: Clock = Clock.fixed(Instant.MAX, ZoneOffset.UTC)
    val test: LocalTime = LocalTime.now(clock)
    assertEquals(test.getHour, 23)
    assertEquals(test.getMinute, 59)
    assertEquals(test.getSecond, 59)
    assertEquals(test.getNano, 999999999)
  }

  @Test def now_Clock_min(): Unit = {
    val clock: Clock = Clock.fixed(Instant.MIN, ZoneOffset.UTC)
    val test: LocalTime = LocalTime.now(clock)
    assertEquals(test.getHour, 0)
    assertEquals(test.getMinute, 0)
    assertEquals(test.getSecond, 0)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_time_2ints(): Unit = {
    val test: LocalTime = LocalTime.of(12, 30)
    check(test, 12, 30, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_2ints_hourTooLow(): Unit = {
    LocalTime.of(-1, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_2ints_hourTooHigh(): Unit = {
    LocalTime.of(24, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_2ints_minuteTooLow(): Unit = {
    LocalTime.of(0, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_2ints_minuteTooHigh(): Unit = {
    LocalTime.of(0, 60)
  }

  @Test def factory_time_3ints(): Unit = {
    val test: LocalTime = LocalTime.of(12, 30, 40)
    check(test, 12, 30, 40, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_3ints_hourTooLow(): Unit = {
    LocalTime.of(-1, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_3ints_hourTooHigh(): Unit = {
    LocalTime.of(24, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_3ints_minuteTooLow(): Unit = {
    LocalTime.of(0, -1, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_3ints_minuteTooHigh(): Unit = {
    LocalTime.of(0, 60, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_3ints_secondTooLow(): Unit = {
    LocalTime.of(0, 0, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_3ints_secondTooHigh(): Unit = {
    LocalTime.of(0, 0, 60)
  }

  @Test def factory_time_4ints(): Unit = {
    var test: LocalTime = LocalTime.of(12, 30, 40, 987654321)
    check(test, 12, 30, 40, 987654321)
    test = LocalTime.of(12, 0, 40, 987654321)
    check(test, 12, 0, 40, 987654321)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_hourTooLow(): Unit = {
    LocalTime.of(-1, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_hourTooHigh(): Unit = {
    LocalTime.of(24, 0, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_minuteTooLow(): Unit = {
    LocalTime.of(0, -1, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_minuteTooHigh(): Unit = {
    LocalTime.of(0, 60, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_secondTooLow(): Unit = {
    LocalTime.of(0, 0, -1, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_secondTooHigh(): Unit = {
    LocalTime.of(0, 0, 60, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_nanoTooLow(): Unit = {
    LocalTime.of(0, 0, 0, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_time_4ints_nanoTooHigh(): Unit = {
    LocalTime.of(0, 0, 0, 1000000000)
  }

  @Test def factory_ofSecondOfDay(): Unit = {
    val localTime: LocalTime = LocalTime.ofSecondOfDay(2 * 60 * 60 + 17 * 60 + 23)
    check(localTime, 2, 17, 23, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofSecondOfDay_tooLow(): Unit = {
    LocalTime.ofSecondOfDay(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofSecondOfDay_tooHigh(): Unit = {
    LocalTime.ofSecondOfDay(24 * 60 * 60)
  }

  @Test def factory_ofSecondOfDay_long_int(): Unit = {
    val localTime: LocalTime = LocalTime.ofSecondOfDay(2 * 60 * 60 + 17 * 60 + 23, 987)
    check(localTime, 2, 17, 23, 987)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofSecondOfDay_long_int_tooLowSecs(): Unit = {
    LocalTime.ofSecondOfDay(-1, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofSecondOfDay_long_int_tooHighSecs(): Unit = {
    LocalTime.ofSecondOfDay(24 * 60 * 60, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofSecondOfDay_long_int_tooLowNanos(): Unit = {
    LocalTime.ofSecondOfDay(0, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofSecondOfDay_long_int_tooHighNanos(): Unit = {
    LocalTime.ofSecondOfDay(0, 1000000000)
  }

  @Test def factory_ofNanoOfDay(): Unit = {
    val localTime: LocalTime = LocalTime.ofNanoOfDay(60 * 60 * 1000000000L + 17)
    check(localTime, 1, 0, 0, 17)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofNanoOfDay_tooLow(): Unit = {
    LocalTime.ofNanoOfDay(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofNanoOfDay_tooHigh(): Unit = {
    LocalTime.ofNanoOfDay(24 * 60 * 60 * 1000000000L)
  }

  @Test def factory_from_DateTimeAccessor(): Unit = {
    assertEquals(LocalTime.from(LocalTime.of(17, 30)), LocalTime.of(17, 30))
    assertEquals(LocalTime.from(LocalDateTime.of(2012, 5, 1, 17, 30)), LocalTime.of(17, 30))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_from_DateTimeAccessor_invalid_noDerive(): Unit = {
    LocalTime.from(LocalDate.of(2007, 7, 15))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_from_DateTimeAccessor_null(): Unit = {
    LocalTime.from(null.asInstanceOf[TemporalAccessor])
  }

  @Test(dataProvider = "sampleToString") def factory_parse_validText(h: Int, m: Int, s: Int, n: Int, parsable: String): Unit = {
    val t: LocalTime = LocalTime.parse(parsable)
    assertNotNull(t, parsable)
    assertEquals(t.getHour, h)
    assertEquals(t.getMinute, m)
    assertEquals(t.getSecond, s)
    assertEquals(t.getNano, n)
  }

  @DataProvider(name = "sampleBadParse") private[bp] def provider_sampleBadParse: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("00;00"), Array("12-00"), Array("-01:00"), Array("00:00:00-09"), Array("00:00:00,09"), Array("00:00:abs"), Array("11"), Array("11:30+01:00"), Array("11:30+01:00[Europe/Paris]"))
  }

  @Test(dataProvider = "sampleBadParse", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_invalidText(unparsable: String): Unit = {
    LocalTime.parse(unparsable)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalHour(): Unit = {
    LocalTime.parse("25:00")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalMinute(): Unit = {
    LocalTime.parse("12:60")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalSecond(): Unit = {
    LocalTime.parse("12:12:60")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullTest(): Unit = {
    LocalTime.parse(null.asInstanceOf[String])
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("H m s")
    val test: LocalTime = LocalTime.parse("14 30 40", f)
    assertEquals(test, LocalTime.of(14, 30, 40))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("H m s")
    LocalTime.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    LocalTime.parse("ANY", null)
  }

  @Test def test_get_TemporalField(): Unit = {
    val test: LocalTime = TEST_12_30_40_987654321
    assertEquals(test.get(ChronoField.HOUR_OF_DAY), 12)
    assertEquals(test.get(ChronoField.MINUTE_OF_HOUR), 30)
    assertEquals(test.get(ChronoField.SECOND_OF_MINUTE), 40)
    assertEquals(test.get(ChronoField.NANO_OF_SECOND), 987654321)
    assertEquals(test.get(ChronoField.SECOND_OF_DAY), 12 * 3600 + 30 * 60 + 40)
    assertEquals(test.get(ChronoField.MINUTE_OF_DAY), 12 * 60 + 30)
    assertEquals(test.get(ChronoField.HOUR_OF_AMPM), 0)
    assertEquals(test.get(ChronoField.CLOCK_HOUR_OF_AMPM), 12)
    assertEquals(test.get(ChronoField.CLOCK_HOUR_OF_DAY), 12)
    assertEquals(test.get(ChronoField.AMPM_OF_DAY), 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_tooBig(): Unit = {
    TEST_12_30_40_987654321.get(NANO_OF_DAY)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_get_TemporalField_null(): Unit = {
    TEST_12_30_40_987654321.get(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_invalidField(): Unit = {
    TEST_12_30_40_987654321.get(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_dateField(): Unit = {
    TEST_12_30_40_987654321.get(ChronoField.DAY_OF_MONTH)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    val test: LocalTime = TEST_12_30_40_987654321
    assertEquals(test.getLong(ChronoField.HOUR_OF_DAY), 12)
    assertEquals(test.getLong(ChronoField.MINUTE_OF_HOUR), 30)
    assertEquals(test.getLong(ChronoField.SECOND_OF_MINUTE), 40)
    assertEquals(test.getLong(ChronoField.NANO_OF_SECOND), 987654321)
    assertEquals(test.getLong(ChronoField.NANO_OF_DAY), ((12 * 3600 + 30 * 60 + 40) * 1000000000L) + 987654321)
    assertEquals(test.getLong(ChronoField.SECOND_OF_DAY), 12 * 3600 + 30 * 60 + 40)
    assertEquals(test.getLong(ChronoField.MINUTE_OF_DAY), 12 * 60 + 30)
    assertEquals(test.getLong(ChronoField.HOUR_OF_AMPM), 0)
    assertEquals(test.getLong(ChronoField.CLOCK_HOUR_OF_AMPM), 12)
    assertEquals(test.getLong(ChronoField.CLOCK_HOUR_OF_DAY), 12)
    assertEquals(test.getLong(ChronoField.AMPM_OF_DAY), 1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getLong_TemporalField_null(): Unit = {
    TEST_12_30_40_987654321.getLong(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_getLong_TemporalField_invalidField(): Unit = {
    TEST_12_30_40_987654321.getLong(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_getLong_TemporalField_dateField(): Unit = {
    TEST_12_30_40_987654321.getLong(ChronoField.DAY_OF_MONTH)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.chronology), null)
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.localDate), null)
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.localTime), TEST_12_30_40_987654321)
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.offset), null)
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.precision), ChronoUnit.NANOS)
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.zone), null)
    assertEquals(TEST_12_30_40_987654321.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_12_30_40_987654321.query(null)
  }

  @DataProvider(name = "sampleTimes") private[bp] def provider_sampleTimes: Array[Array[_ <: AnyRef]] = {
    Array[Array[_ <: AnyRef]](Array[Integer](0, 0, 0, 0), Array[Integer](0, 0, 0, 1), Array[Integer](0, 0, 1, 0), Array[Integer](0, 0, 1, 1), Array[Integer](0, 1, 0, 0), Array[Integer](0, 1, 0, 1), Array[Integer](0, 1, 1, 0), Array[Integer](0, 1, 1, 1), Array[Integer](1, 0, 0, 0), Array[Integer](1, 0, 0, 1), Array[Integer](1, 0, 1, 0), Array[Integer](1, 0, 1, 1), Array[Integer](1, 1, 0, 0), Array[Integer](1, 1, 0, 1), Array[Integer](1, 1, 1, 0), Array[Integer](1, 1, 1, 1))
  }

  @Test(dataProvider = "sampleTimes") def test_get(h: Int, m: Int, s: Int, ns: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, ns)
    assertEquals(a.getHour, h)
    assertEquals(a.getMinute, m)
    assertEquals(a.getSecond, s)
    assertEquals(a.getNano, ns)
  }

  @Test def test_with_adjustment(): Unit = {
    val sample: LocalTime = LocalTime.of(23, 5)
    val adjuster: TemporalAdjuster = new TemporalAdjuster {
      override def adjustInto(temporal: Temporal): Temporal = sample
    }
    assertEquals(TEST_12_30_40_987654321.`with`(adjuster), sample)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_adjustment_null(): Unit = {
    TEST_12_30_40_987654321.`with`(null.asInstanceOf[TemporalAdjuster])
  }

  @Test def test_withHour_normal(): Unit = {
    var t: LocalTime = TEST_12_30_40_987654321

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

  @Test def test_withHour_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.withHour(12)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_withHour_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(1, 0).withHour(0)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_withHour_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(1, 0).withHour(12)
    assertEquals(t, LocalTime.NOON)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withHour_hourTooLow(): Unit = {
    TEST_12_30_40_987654321.withHour(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withHour_hourTooHigh(): Unit = {
    TEST_12_30_40_987654321.withHour(24)
  }

  @Test def test_withMinute_normal(): Unit = {
    var t: LocalTime = TEST_12_30_40_987654321

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

  @Test def test_withMinute_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.withMinute(30)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_withMinute_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(0, 1).withMinute(0)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_withMinute_toMidday_equals(): Unit = {
    val t: LocalTime = LocalTime.of(12, 1).withMinute(0)
    assertEquals(t, LocalTime.NOON)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMinute_minuteTooLow(): Unit = {
    TEST_12_30_40_987654321.withMinute(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMinute_minuteTooHigh(): Unit = {
    TEST_12_30_40_987654321.withMinute(60)
  }

  @Test def test_withSecond_normal(): Unit = {
    var t: LocalTime = TEST_12_30_40_987654321

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

  @Test def test_withSecond_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.withSecond(40)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_withSecond_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(0, 0, 1).withSecond(0)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_withSecond_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(12, 0, 1).withSecond(0)
    assertEquals(t, LocalTime.NOON)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withSecond_secondTooLow(): Unit = {
    TEST_12_30_40_987654321.withSecond(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withSecond_secondTooHigh(): Unit = {
    TEST_12_30_40_987654321.withSecond(60)
  }

  @Test def test_withNanoOfSecond_normal(): Unit = {
    var t: LocalTime = TEST_12_30_40_987654321
    t = t.withNano(1)
    assertEquals(t.getNano, 1)
    t = t.withNano(10)
    assertEquals(t.getNano, 10)
    t = t.withNano(100)
    assertEquals(t.getNano, 100)
    t = t.withNano(999999999)
    assertEquals(t.getNano, 999999999)
  }

  @Test def test_withNanoOfSecond_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.withNano(987654321)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_withNanoOfSecond_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(0, 0, 0, 1).withNano(0)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_withNanoOfSecond_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(12, 0, 0, 1).withNano(0)
    assertEquals(t, LocalTime.NOON)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withNanoOfSecond_nanoTooLow(): Unit = {
    TEST_12_30_40_987654321.withNano(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withNanoOfSecond_nanoTooHigh(): Unit = {
    TEST_12_30_40_987654321.withNano(1000000000)
  }

  private val NINETY_MINS: TemporalUnit = new TemporalUnit() {
    override def toString: String = {
      "NinetyMins"
    }

    def getDuration: Duration = {
      Duration.ofMinutes(90)
    }

    def isDurationEstimated: Boolean = {
      false
    }

    def isDateBased: Boolean = {
      false
    }

    def isTimeBased: Boolean = {
      true
    }

    def isSupportedBy(temporal: Temporal): Boolean = {
      false
    }

    def addTo[R <: Temporal](r: R, l: Long): R = {
      throw new UnsupportedOperationException
    }

    def between(r: Temporal, r2: Temporal): Long = {
      throw new UnsupportedOperationException
    }
  }
  private val NINETY_FIVE_MINS: TemporalUnit = new TemporalUnit() {
    override def toString: String = {
      "NinetyFiveMins"
    }

    def getDuration: Duration = {
      Duration.ofMinutes(95)
    }

    def isDurationEstimated: Boolean = {
      false
    }

    def isDateBased: Boolean = {
      false
    }

    def isTimeBased: Boolean = {
      true
    }

    def isSupportedBy(temporal: Temporal): Boolean = {
      false
    }

    def addTo[R <: Temporal](r: R, l: Long): R = {
      throw new UnsupportedOperationException
    }

    def between(r: Temporal, r2: Temporal): Long = {
      throw new UnsupportedOperationException
    }
  }

  @DataProvider(name = "truncatedToValid") private[bp] def data_truncatedToValid: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(LocalTime.of(1, 2, 3, 123456789), NANOS, LocalTime.of(1, 2, 3, 123456789)), Array(LocalTime.of(1, 2, 3, 123456789), MICROS, LocalTime.of(1, 2, 3, 123456000)), Array(LocalTime.of(1, 2, 3, 123456789), MILLIS, LocalTime.of(1, 2, 3, 123000000)), Array(LocalTime.of(1, 2, 3, 123456789), SECONDS, LocalTime.of(1, 2, 3)), Array(LocalTime.of(1, 2, 3, 123456789), MINUTES, LocalTime.of(1, 2)), Array(LocalTime.of(1, 2, 3, 123456789), HOURS, LocalTime.of(1, 0)), Array(LocalTime.of(1, 2, 3, 123456789), DAYS, LocalTime.MIDNIGHT), Array(LocalTime.of(1, 1, 1, 123456789), NINETY_MINS, LocalTime.of(0, 0)), Array(LocalTime.of(2, 1, 1, 123456789), NINETY_MINS, LocalTime.of(1, 30)), Array(LocalTime.of(3, 1, 1, 123456789), NINETY_MINS, LocalTime.of(3, 0)))
  }

  @Test(groups = Array("tck"), dataProvider = "truncatedToValid") def test_truncatedTo_valid(input: LocalTime, unit: TemporalUnit, expected: LocalTime): Unit = {
    assertEquals(input.truncatedTo(unit), expected)
  }

  @DataProvider(name = "truncatedToInvalid") private[bp] def data_truncatedToInvalid: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(LocalTime.of(1, 2, 3, 123456789), NINETY_FIVE_MINS), Array(LocalTime.of(1, 2, 3, 123456789), WEEKS), Array(LocalTime.of(1, 2, 3, 123456789), MONTHS), Array(LocalTime.of(1, 2, 3, 123456789), YEARS))
  }

  @Test(groups = Array("tck"), dataProvider = "truncatedToInvalid", expectedExceptions = Array(classOf[DateTimeException])) def test_truncatedTo_invalid(input: LocalTime, unit: TemporalUnit): Unit = {
    input.truncatedTo(unit)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException]), groups = Array("tck")) def test_truncatedTo_null(): Unit = {
    TEST_12_30_40_987654321.truncatedTo(null)
  }

  @Test def test_plus_Adjuster_positiveHours(): Unit = {
    val period: TemporalAmount = MockSimplePeriod.of(7, ChronoUnit.HOURS)
    val t: LocalTime = TEST_12_30_40_987654321.plus(period)
    assertEquals(t, LocalTime.of(19, 30, 40, 987654321))
  }

  @Test def test_plus_Adjuster_negativeMinutes(): Unit = {
    val period: TemporalAmount = MockSimplePeriod.of(-25, ChronoUnit.MINUTES)
    val t: LocalTime = TEST_12_30_40_987654321.plus(period)
    assertEquals(t, LocalTime.of(12, 5, 40, 987654321))
  }

  @Test def test_plus_Adjuster_zero(): Unit = {
    val period: TemporalAmount = Period.ZERO
    val t: LocalTime = TEST_12_30_40_987654321.plus(period)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plus_Adjuster_wrap(): Unit = {
    val p: TemporalAmount = Duration.ofHours(1)
    val t: LocalTime = LocalTime.of(23, 30).plus(p)
    assertEquals(t, LocalTime.of(0, 30))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_Adjuster_dateNotAllowed(): Unit = {
    val period: TemporalAmount = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    TEST_12_30_40_987654321.plus(period)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_Adjuster_null(): Unit = {
    TEST_12_30_40_987654321.plus(null.asInstanceOf[TemporalAmount])
  }

  @Test def test_plus_longPeriodUnit_positiveHours(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plus(7, ChronoUnit.HOURS)
    assertEquals(t, LocalTime.of(19, 30, 40, 987654321))
  }

  @Test def test_plus_longPeriodUnit_negativeMinutes(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plus(-25, ChronoUnit.MINUTES)
    assertEquals(t, LocalTime.of(12, 5, 40, 987654321))
  }

  @Test def test_plus_longPeriodUnit_zero(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plus(0, ChronoUnit.MINUTES)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plus_long_unit_invalidUnit(): Unit = {
    for (unit <- TestLocalTime.INVALID_UNITS) {
      try {
        TEST_12_30_40_987654321.plus(1, unit)
        fail("Unit should not be allowed " + unit)
      }
      catch {
        case ex: DateTimeException =>
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[UnsupportedTemporalTypeException])) def test_plus_long_multiples(): Unit = {
    TEST_12_30_40_987654321.plus(0, DAYS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_longPeriodUnit_null(): Unit = {
    TEST_12_30_40_987654321.plus(1, null.asInstanceOf[TemporalUnit])
  }

  @Test def test_plus_adjuster(): Unit = {
    val p: Duration = Duration.ofSeconds(62, 3)
    val t: LocalTime = TEST_12_30_40_987654321.plus(p)
    assertEquals(t, LocalTime.of(12, 31, 42, 987654324))
  }

  @Test def test_plus_adjuster_big(): Unit = {
    val p: Duration = Duration.ofNanos(Long.MaxValue)
    val t: LocalTime = TEST_12_30_40_987654321.plus(p)
    assertEquals(t, TEST_12_30_40_987654321.plusNanos(Long.MaxValue))
  }

  @Test def test_plus_adjuster_zero_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plus(Period.ZERO)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plus_adjuster_wrap(): Unit = {
    val p: Duration = Duration.ofHours(1)
    val t: LocalTime = LocalTime.of(23, 30).plus(p)
    assertEquals(t, LocalTime.of(0, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_adjuster_null(): Unit = {
    TEST_12_30_40_987654321.plus(null.asInstanceOf[TemporalAmount])
  }

  @Test def test_plusHours_one(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT

    {
      var i: Int = 0
      while (i < 50) {
        {
          t = t.plusHours(1)
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
    val base: LocalTime = LocalTime.MIDNIGHT

    {
      var i: Int = -50
      while (i < 50) {
        {
          val t: LocalTime = base.plusHours(i)
          assertEquals(t.getHour, (i + 72) % 24)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusHours_fromOne(): Unit = {
    val base: LocalTime = LocalTime.of(1, 0)

    {
      var i: Int = -50
      while (i < 50) {
        {
          val t: LocalTime = base.plusHours(i)
          assertEquals(t.getHour, (1 + i + 72) % 24)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_plusHours_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusHours(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusHours_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(23, 0).plusHours(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_plusHours_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(11, 0).plusHours(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_plusHours_big(): Unit = {
    val t: LocalTime = LocalTime.of(2, 30).plusHours(Long.MaxValue)
    val hours: Int = (Long.MaxValue % 24L).toInt
    assertEquals(t, LocalTime.of(2, 30).plusHours(hours))
  }

  @Test def test_plusMinutes_one(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT
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
    val base: LocalTime = LocalTime.MIDNIGHT
    var hour: Int = 0
    var min: Int = 0

    {
      var i: Int = -70
      while (i < 70) {
        {
          val t: LocalTime = base.plusMinutes(i)
          if (i < -60) {
            hour = 22
            min = i + 120
          }
          else if (i < 0) {
            hour = 23
            min = i + 60
          }
          else if (i >= 60) {
            hour = 1
            min = i - 60
          }
          else {
            hour = 0
            min = i
          }
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

  @Test def test_plusMinutes_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusMinutes(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusMinutes_noChange_oneDay_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusMinutes(24 * 60)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusMinutes_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(23, 59).plusMinutes(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_plusMinutes_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(11, 59).plusMinutes(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_plusMinutes_big(): Unit = {
    val t: LocalTime = LocalTime.of(2, 30).plusMinutes(Long.MaxValue)
    val mins: Int = (Long.MaxValue % (24L * 60L)).toInt
    assertEquals(t, LocalTime.of(2, 30).plusMinutes(mins))
  }

  @Test def test_plusSeconds_one(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT
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
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0

      def hasNext: Boolean = i <= 3660

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, hour, min, sec)
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
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "plusSeconds_fromZero") def test_plusSeconds_fromZero(seconds: Int, hour: Int, min: Int, sec: Int): Unit = {
    val base: LocalTime = LocalTime.MIDNIGHT
    val t: LocalTime = base.plusSeconds(seconds)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
  }

  @Test def test_plusSeconds_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusSeconds(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusSeconds_noChange_oneDay_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusSeconds(24 * 60 * 60)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusSeconds_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(23, 59, 59).plusSeconds(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_plusSeconds_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(11, 59, 59).plusSeconds(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_plusNanos_halfABillion(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0
    var nanos: Int = 0
    var i: Long = 0
    while (i < 3700 * 1000000000L) {
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
      assertEquals(t.getHour, hour)
      assertEquals(t.getMinute, min)
      assertEquals(t.getSecond, sec)
      assertEquals(t.getNano, nanos)
      i += 500000000
    }
  }

  @DataProvider(name = "plusNanos_fromZero") private[bp] def plusNanos_fromZero: java.util.Iterator[Array[Any]] = {
    new java.util.Iterator[Array[Any]]() {
      private[bp] var delta: Long = 7500000000L
      private[bp] var i: Long = -3660 * 1000000000L
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0
      private[bp] var nanos: Long = 0

      def hasNext: Boolean = i <= 3660 * 1000000000L

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, hour, min, sec, nanos.toInt).asInstanceOf[Array[Any]]
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
              }
            }
          }
        }
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "plusNanos_fromZero") def test_plusNanos_fromZero(nanoseconds: Long, hour: Int, min: Int, sec: Int, nanos: Int): Unit = {
    val base: LocalTime = LocalTime.MIDNIGHT
    val t: LocalTime = base.plusNanos(nanoseconds)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
    assertEquals(nanos, t.getNano)
  }

  @Test def test_plusNanos_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusNanos(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusNanos_noChange_oneDay_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.plusNanos(24 * 60 * 60 * 1000000000L)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_plusNanos_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(23, 59, 59, 999999999).plusNanos(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_plusNanos_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(11, 59, 59, 999999999).plusNanos(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_minus_Adjuster(): Unit = {
    val p: TemporalAmount = Duration.ofSeconds(62, 3)
    val t: LocalTime = TEST_12_30_40_987654321.minus(p)
    assertEquals(t, LocalTime.of(12, 29, 38, 987654318))
  }

  @Test def test_minus_Adjuster_positiveHours(): Unit = {
    val period: TemporalAmount = MockSimplePeriod.of(7, ChronoUnit.HOURS)
    val t: LocalTime = TEST_12_30_40_987654321.minus(period)
    assertEquals(t, LocalTime.of(5, 30, 40, 987654321))
  }

  @Test def test_minus_Adjuster_negativeMinutes(): Unit = {
    val period: TemporalAmount = MockSimplePeriod.of(-25, ChronoUnit.MINUTES)
    val t: LocalTime = TEST_12_30_40_987654321.minus(period)
    assertEquals(t, LocalTime.of(12, 55, 40, 987654321))
  }

  @Test def test_minus_Adjuster_big1(): Unit = {
    val p: TemporalAmount = Duration.ofNanos(Long.MaxValue)
    val t: LocalTime = TEST_12_30_40_987654321.minus(p)
    assertEquals(t, TEST_12_30_40_987654321.minusNanos(Long.MaxValue))
  }

  @Test def test_minus_Adjuster_zero(): Unit = {
    val p: TemporalAmount = Period.ZERO
    val t: LocalTime = TEST_12_30_40_987654321.minus(p)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minus_Adjuster_wrap(): Unit = {
    val p: TemporalAmount = Duration.ofHours(1)
    val t: LocalTime = LocalTime.of(0, 30).minus(p)
    assertEquals(t, LocalTime.of(23, 30))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_Adjuster_dateNotAllowed(): Unit = {
    val period: TemporalAmount = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    TEST_12_30_40_987654321.minus(period)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_Adjuster_null(): Unit = {
    TEST_12_30_40_987654321.minus(null.asInstanceOf[TemporalAmount])
  }

  @Test def test_minus_longPeriodUnit_positiveHours(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minus(7, ChronoUnit.HOURS)
    assertEquals(t, LocalTime.of(5, 30, 40, 987654321))
  }

  @Test def test_minus_longPeriodUnit_negativeMinutes(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minus(-25, ChronoUnit.MINUTES)
    assertEquals(t, LocalTime.of(12, 55, 40, 987654321))
  }

  @Test def test_minus_longPeriodUnit_zero(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minus(0, ChronoUnit.MINUTES)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minus_long_unit_invalidUnit(): Unit = {
    for (unit <- TestLocalTime.INVALID_UNITS) {
      try {
        TEST_12_30_40_987654321.minus(1, unit)
        fail("Unit should not be allowed " + unit)
      }
      catch {
        case ex: DateTimeException =>
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[UnsupportedTemporalTypeException])) def test_minus_long_multiples(): Unit = {
    TEST_12_30_40_987654321.minus(0, DAYS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_longPeriodUnit_null(): Unit = {
    TEST_12_30_40_987654321.minus(1, null.asInstanceOf[TemporalUnit])
  }

  @Test def test_minusHours_one(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT

    {
      var i: Int = 0
      while (i < 50) {
        {
          t = t.minusHours(1)
          assertEquals(t.getHour, (((-i + 23) % 24) + 24) % 24, String.valueOf(i))
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusHours_fromZero(): Unit = {
    val base: LocalTime = LocalTime.MIDNIGHT

    {
      var i: Int = -50
      while (i < 50) {
        {
          val t: LocalTime = base.minusHours(i)
          assertEquals(t.getHour, ((-i % 24) + 24) % 24)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusHours_fromOne(): Unit = {
    val base: LocalTime = LocalTime.of(1, 0)

    {
      var i: Int = -50
      while (i < 50) {
        {
          val t: LocalTime = base.minusHours(i)
          assertEquals(t.getHour, (1 + (-i % 24) + 24) % 24)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def test_minusHours_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusHours(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusHours_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(1, 0).minusHours(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_minusHours_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(13, 0).minusHours(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_minusHours_big(): Unit = {
    val t: LocalTime = LocalTime.of(2, 30).minusHours(Long.MaxValue)
    val hours: Int = (Long.MaxValue % 24L).toInt
    assertEquals(t, LocalTime.of(2, 30).minusHours(hours))
  }

  @Test def test_minusMinutes_one(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT
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
    val base: LocalTime = LocalTime.MIDNIGHT
    var hour: Int = 22
    var min: Int = 49

    {
      var i: Int = 70
      while (i > -70) {
        {
          val t: LocalTime = base.minusMinutes(i)
          min += 1
          if (min == 60) {
            hour += 1
            min = 0
            if (hour == 24) {
              hour = 0
            }
          }
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test def test_minusMinutes_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusMinutes(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusMinutes_noChange_oneDay_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusMinutes(24 * 60)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusMinutes_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(0, 1).minusMinutes(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_minusMinutes_toMidday_equals(): Unit = {
    val t: LocalTime = LocalTime.of(12, 1).minusMinutes(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_minusMinutes_big(): Unit = {
    val t: LocalTime = LocalTime.of(2, 30).minusMinutes(Long.MaxValue)
    val mins: Int = (Long.MaxValue % (24L * 60L)).toInt
    assertEquals(t, LocalTime.of(2, 30).minusMinutes(mins))
  }

  @Test def test_minusSeconds_one(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT
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
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0

      def hasNext: Boolean = i >= -3660

      def next: Array[Any] = {
        val ret: Array[Any] = Array[Any](i, hour, min, sec)
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
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "minusSeconds_fromZero") def test_minusSeconds_fromZero(seconds: Int, hour: Int, min: Int, sec: Int): Unit = {
    val base: LocalTime = LocalTime.MIDNIGHT
    val t: LocalTime = base.minusSeconds(seconds)
    assertEquals(t.getHour, hour)
    assertEquals(t.getMinute, min)
    assertEquals(t.getSecond, sec)
  }

  @Test def test_minusSeconds_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusSeconds(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusSeconds_noChange_oneDay_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusSeconds(24 * 60 * 60)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusSeconds_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(0, 0, 1).minusSeconds(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_minusSeconds_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(12, 0, 1).minusSeconds(1)
    assertEquals(t, LocalTime.NOON)
  }

  @Test def test_minusSeconds_big(): Unit = {
    val t: LocalTime = LocalTime.of(2, 30).minusSeconds(Long.MaxValue)
    val secs: Int = (Long.MaxValue % (24L * 60L * 60L)).toInt
    assertEquals(t, LocalTime.of(2, 30).minusSeconds(secs))
  }

  @Test def test_minusNanos_halfABillion(): Unit = {
    var t: LocalTime = LocalTime.MIDNIGHT
    var hour: Int = 0
    var min: Int = 0
    var sec: Int = 0
    var nanos: Int = 0

    {
      var i: Long = 0
      while (i < 3700 * 1000000000L) {
        {
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
          assertEquals(t.getHour, hour)
          assertEquals(t.getMinute, min)
          assertEquals(t.getSecond, sec)
          assertEquals(t.getNano, nanos)
        }
        i += 500000000
      }
    }
  }

  @DataProvider(name = "minusNanos_fromZero") private[bp] def minusNanos_fromZero: java.util.Iterator[Array[_ <: AnyRef]] = {
    new java.util.Iterator[Array[_ <: AnyRef]]() {
      private[bp] var delta: Long = 7500000000L
      private[bp] var i: Long = 3660 * 1000000000L
      private[bp] var hour: Int = 22
      private[bp] var min: Int = 59
      private[bp] var sec: Int = 0
      private[bp] var nanos: Long = 0

      def hasNext: Boolean = i >= -3660 * 1000000000L

      def next: Array[_ <: AnyRef] = {
        val ret: Array[_ <: AnyRef] = Array(i: java.lang.Long, hour: Integer, min: Integer, sec: Integer, nanos.toInt: Integer)
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
              }
            }
          }
        }
        ret
      }

      override def remove(): Unit = throw new UnsupportedOperationException
    }
  }

  @Test(dataProvider = "minusNanos_fromZero") def test_minusNanos_fromZero(nanoseconds: Long, hour: Int, min: Int, sec: Int, nanos: Int): Unit = {
    val base: LocalTime = LocalTime.MIDNIGHT
    val t: LocalTime = base.minusNanos(nanoseconds)
    assertEquals(hour, t.getHour)
    assertEquals(min, t.getMinute)
    assertEquals(sec, t.getSecond)
    assertEquals(nanos, t.getNano)
  }

  @Test def test_minusNanos_noChange_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusNanos(0)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusNanos_noChange_oneDay_equal(): Unit = {
    val t: LocalTime = TEST_12_30_40_987654321.minusNanos(24 * 60 * 60 * 1000000000L)
    assertEquals(t, TEST_12_30_40_987654321)
  }

  @Test def test_minusNanos_toMidnight_equal(): Unit = {
    val t: LocalTime = LocalTime.of(0, 0, 0, 1).minusNanos(1)
    assertEquals(t, LocalTime.MIDNIGHT)
  }

  @Test def test_minusNanos_toMidday_equal(): Unit = {
    val t: LocalTime = LocalTime.of(12, 0, 0, 1).minusNanos(1)
    assertEquals(t, LocalTime.NOON)
  }

  @DataProvider(name = "until") private[bp] def provider_until: Array[Array[Any]] = {
    Array[Array[Any]](Array("00:00", "00:00", NANOS, 0), Array("00:00", "00:00", MICROS, 0), Array("00:00", "00:00", MILLIS, 0), Array("00:00", "00:00", SECONDS, 0), Array("00:00", "00:00", MINUTES, 0), Array("00:00", "00:00", HOURS, 0), Array("00:00", "00:00", HALF_DAYS, 0), Array("00:00", "00:00:01", NANOS, 1000000000), Array("00:00", "00:00:01", MICROS, 1000000), Array("00:00", "00:00:01", MILLIS, 1000), Array("00:00", "00:00:01", SECONDS, 1), Array("00:00", "00:00:01", MINUTES, 0), Array("00:00", "00:00:01", HOURS, 0), Array("00:00", "00:00:01", HALF_DAYS, 0), Array("00:00", "00:01", NANOS, 60000000000L), Array("00:00", "00:01", MICROS, 60000000), Array("00:00", "00:01", MILLIS, 60000), Array("00:00", "00:01", SECONDS, 60), Array("00:00", "00:01", MINUTES, 1), Array("00:00", "00:01", HOURS, 0), Array("00:00", "00:01", HALF_DAYS, 0))
  }

  @Test(dataProvider = "until") def test_until(startStr: String, endStr: String, unit: TemporalUnit, expected: Long): Unit = {
    val start: LocalTime = LocalTime.parse(startStr)
    val end: LocalTime = LocalTime.parse(endStr)
    assertEquals(start.until(end, unit), expected)
    assertEquals(end.until(start, unit), -expected)
  }

  @Test def test_atDate(): Unit = {
    val t: LocalTime = LocalTime.of(11, 30)
    assertEquals(t.atDate(LocalDate.of(2012, 6, 30)), LocalDateTime.of(2012, 6, 30, 11, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atDate_nullDate(): Unit = {
    TEST_12_30_40_987654321.atDate(null.asInstanceOf[LocalDate])
  }

  @Test def test_toSecondOfDay(): Unit = {
    var t: LocalTime = LocalTime.of(0, 0)
    var i: Int = 0
    while (i < 24 * 60 * 60) {
      assertEquals(t.toSecondOfDay, i)
      t = t.plusSeconds(1)
      i += 1
    }
  }

  @Test def test_toSecondOfDay_fromNanoOfDay_symmetry(): Unit = {
    var t: LocalTime = LocalTime.of(0, 0)
    var i: Int = 0
    while (i < 24 * 60 * 60) {
      assertEquals(LocalTime.ofSecondOfDay(t.toSecondOfDay), t)
      t = t.plusSeconds(1)
      i += 1
    }
  }

  @Test def test_toNanoOfDay(): Unit = {
    var t: LocalTime = LocalTime.of(0, 0)

    {
      var i: Int = 0
      while (i < 1000000) {
        assertEquals(t.toNanoOfDay, i)
        t = t.plusNanos(1)
        i += 1
      }
    }
    t = LocalTime.of(0, 0)

    {
      var i: Int = 1
      while (i <= 1000000) {
        t = t.minusNanos(1)
        assertEquals(t.toNanoOfDay, 24 * 60 * 60 * 1000000000L - i)
        i += 1
      }
    }
  }

  @Test def test_toNanoOfDay_fromNanoOfDay_symmetry(): Unit = {
    var t: LocalTime = LocalTime.of(0, 0)

    {
      var i: Int = 0
      while (i < 1000000) {
          assertEquals(LocalTime.ofNanoOfDay(t.toNanoOfDay), t)
          t = t.plusNanos(1)
          i += 1
      }
    }
    t = LocalTime.of(0, 0)

    {
      var i: Int = 1
      while (i <= 1000000) {
          t = t.minusNanos(1)
          assertEquals(LocalTime.ofNanoOfDay(t.toNanoOfDay), t)
          i += 1
      }
    }
  }

  @Test def test_comparisons(): Unit = {
    doTest_comparisons_LocalTime(LocalTime.MIDNIGHT, LocalTime.of(0, 0, 0, 999999999), LocalTime.of(0, 0, 59, 0), LocalTime.of(0, 0, 59, 999999999), LocalTime.of(0, 59, 0, 0), LocalTime.of(0, 59, 0, 999999999), LocalTime.of(0, 59, 59, 0), LocalTime.of(0, 59, 59, 999999999), LocalTime.NOON, LocalTime.of(12, 0, 0, 999999999), LocalTime.of(12, 0, 59, 0), LocalTime.of(12, 0, 59, 999999999), LocalTime.of(12, 59, 0, 0), LocalTime.of(12, 59, 0, 999999999), LocalTime.of(12, 59, 59, 0), LocalTime.of(12, 59, 59, 999999999), LocalTime.of(23, 0, 0, 0), LocalTime.of(23, 0, 0, 999999999), LocalTime.of(23, 0, 59, 0), LocalTime.of(23, 0, 59, 999999999), LocalTime.of(23, 59, 0, 0), LocalTime.of(23, 59, 0, 999999999), LocalTime.of(23, 59, 59, 0), LocalTime.of(23, 59, 59, 999999999))
  }

  private def doTest_comparisons_LocalTime(localTimes: LocalTime*): Unit = {
    var i: Int = 0
    while (i < localTimes.length) {
      val a: LocalTime = localTimes(i)
      var j: Int = 0
      while (j < localTimes.length) {
        val b: LocalTime = localTimes(j)
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
    TEST_12_30_40_987654321.compareTo(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_ObjectNull(): Unit = {
    TEST_12_30_40_987654321.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_ObjectNull(): Unit = {
    TEST_12_30_40_987654321.isAfter(null)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_true(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m, s, n)
    assertEquals(a == b, true)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_hour_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h + 1, m, s, n)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_minute_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m + 1, s, n)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_second_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m, s + 1, n)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleTimes") def test_equals_false_nano_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m, s, n + 1)
    assertEquals(a == b, false)
  }

  @Test def test_equals_itself_true(): Unit = {
    assertEquals(TEST_12_30_40_987654321 == TEST_12_30_40_987654321, true)
  }

  @Test def test_equals_string_false(): Unit = {
    assertEquals(TEST_12_30_40_987654321 == "2007-07-15", false)
  }

  @Test def test_equals_null_false(): Unit = {
    assertEquals(TEST_12_30_40_987654321 == null, false)
  }

  @Test(dataProvider = "sampleTimes") def test_hashCode_same(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m, s, n)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test(dataProvider = "sampleTimes") def test_hashCode_hour_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h + 1, m, s, n)
    assertEquals(a.hashCode == b.hashCode, false)
  }

  @Test(dataProvider = "sampleTimes") def test_hashCode_minute_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m + 1, s, n)
    assertEquals(a.hashCode == b.hashCode, false)
  }

  @Test(dataProvider = "sampleTimes") def test_hashCode_second_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m, s + 1, n)
    assertEquals(a.hashCode == b.hashCode, false)
  }

  @Test(dataProvider = "sampleTimes") def test_hashCode_nano_differs(h: Int, m: Int, s: Int, n: Int): Unit = {
    val a: LocalTime = LocalTime.of(h, m, s, n)
    val b: LocalTime = LocalTime.of(h, m, s, n + 1)
    assertEquals(a.hashCode == b.hashCode, false)
  }

  @DataProvider(name = "sampleToString") private[bp] def provider_sampleToString: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, "00:00"), Array(1, 0, 0, 0, "01:00"), Array(23, 0, 0, 0, "23:00"), Array(0, 1, 0, 0, "00:01"), Array(12, 30, 0, 0, "12:30"), Array(23, 59, 0, 0, "23:59"), Array(0, 0, 1, 0, "00:00:01"), Array(0, 0, 59, 0, "00:00:59"), Array(0, 0, 0, 100000000, "00:00:00.100"), Array(0, 0, 0, 10000000, "00:00:00.010"), Array(0, 0, 0, 1000000, "00:00:00.001"), Array(0, 0, 0, 100000, "00:00:00.000100"), Array(0, 0, 0, 10000, "00:00:00.000010"), Array(0, 0, 0, 1000, "00:00:00.000001"), Array(0, 0, 0, 100, "00:00:00.000000100"), Array(0, 0, 0, 10, "00:00:00.000000010"), Array(0, 0, 0, 1, "00:00:00.000000001"), Array(0, 0, 0, 999999999, "00:00:00.999999999"), Array(0, 0, 0, 99999999, "00:00:00.099999999"), Array(0, 0, 0, 9999999, "00:00:00.009999999"), Array(0, 0, 0, 999999, "00:00:00.000999999"), Array(0, 0, 0, 99999, "00:00:00.000099999"), Array(0, 0, 0, 9999, "00:00:00.000009999"), Array(0, 0, 0, 999, "00:00:00.000000999"), Array(0, 0, 0, 99, "00:00:00.000000099"), Array(0, 0, 0, 9, "00:00:00.000000009"))
  }

  @Test(dataProvider = "sampleToString") def test_toString(h: Int, m: Int, s: Int, n: Int, expected: String): Unit = {
    val t: LocalTime = LocalTime.of(h, m, s, n)
    val str: String = t.toString
    assertEquals(str, expected)
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("H m s")
    val t: String = LocalTime.of(11, 30, 45).format(f)
    assertEquals(t, "11 30 45")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_format_formatter_null(): Unit = {
    LocalTime.of(11, 30, 45).format(null)
  }
}
