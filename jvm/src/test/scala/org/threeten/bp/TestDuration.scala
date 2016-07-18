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
import org.testng.Assert.fail
import org.testng.SkipException
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.HALF_DAYS
import org.threeten.bp.temporal.ChronoUnit.HOURS
import org.threeten.bp.temporal.ChronoUnit.MICROS
import org.threeten.bp.temporal.ChronoUnit.MILLIS
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import org.threeten.bp.temporal.ChronoUnit.NANOS
import org.threeten.bp.temporal.ChronoUnit.SECONDS
import org.threeten.bp.temporal.ChronoUnit.WEEKS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Locale
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.TemporalUnit

/** Test Duration. */
@Test class TestDuration {
  @Test def test_immutable(): Unit = {
    throw new SkipException("private constructor shows up public due to companion object")
    AbstractTest.assertImmutable(classOf[Duration])
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(Duration.ofHours(5))
    AbstractTest.assertSerializable(Duration.ofHours(-5))
    AbstractTest.assertSerializableAndSame(Duration.ZERO)
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization_format(): Unit = {
    AbstractTest.assertEqualsSerialisedForm(Duration.ofSeconds(654321, 123456789))
  }

  @Test def test_zero(): Unit = {
    assertEquals(Duration.ZERO.getSeconds, 0L)
    assertEquals(Duration.ZERO.getNano, 0)
  }

  @Test def factory_seconds_long(): Unit = {
    {
      var i: Long = -2
      while (i <= 2) {
        {
          val t: Duration = Duration.ofSeconds(i)
          assertEquals(t.getSeconds, i)
          assertEquals(t.getNano, 0)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def factory_seconds_long_long(): Unit = {
    {
      var i: Long = -2
      while (i <= 2) {
        {
          {
            var j: Int = 0
            while (j < 10) {
              {
                val t: Duration = Duration.ofSeconds(i, j)
                assertEquals(t.getSeconds, i)
                assertEquals(t.getNano, j)
              }
              {
                j += 1
                j - 1
              }
            }
          }
          {
            var j: Int = -10
            while (j < 0) {
              {
                val t: Duration = Duration.ofSeconds(i, j)
                assertEquals(t.getSeconds, i - 1)
                assertEquals(t.getNano, j + 1000000000)
              }
              {
                j += 1
                j - 1
              }
            }
          }
          {
            var j: Int = 999999990
            while (j < 1000000000) {
              {
                val t: Duration = Duration.ofSeconds(i, j)
                assertEquals(t.getSeconds, i)
                assertEquals(t.getNano, j)
              }
              {
                j += 1
                j - 1
              }
            }
          }
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def factory_seconds_long_long_nanosNegativeAdjusted(): Unit = {
    val test: Duration = Duration.ofSeconds(2L, -1)
    assertEquals(test.getSeconds, 1)
    assertEquals(test.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_seconds_long_long_tooBig(): Unit = {
    Duration.ofSeconds(Long.MaxValue, 1000000000)
  }

  @DataProvider(name = "MillisDurationNoNanos") private[bp] def provider_factory_millis_long: Array[Array[_ <: AnyRef]] = {
    Array[Array[_ <: AnyRef]](Array[Integer](0, 0, 0), Array[Integer](1, 0, 1000000), Array[Integer](2, 0, 2000000), Array[Integer](999, 0, 999000000), Array[Integer](1000, 1, 0), Array[Integer](1001, 1, 1000000), Array[Integer](-1, -1, 999000000), Array[Integer](-2, -1, 998000000), Array[Integer](-999, -1, 1000000), Array[Integer](-1000, -1, 0), Array[Integer](-1001, -2, 999000000))
  }

  @Test(dataProvider = "MillisDurationNoNanos") def factory_millis_long(millis: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val test: Duration = Duration.ofMillis(millis)
    assertEquals(test.getSeconds, expectedSeconds)
    assertEquals(test.getNano, expectedNanoOfSecond)
  }

  @Test def factory_nanos_nanos(): Unit = {
    val test: Duration = Duration.ofNanos(1)
    assertEquals(test.getSeconds, 0)
    assertEquals(test.getNano, 1)
  }

  @Test def factory_nanos_nanosSecs(): Unit = {
    val test: Duration = Duration.ofNanos(1000000002)
    assertEquals(test.getSeconds, 1)
    assertEquals(test.getNano, 2)
  }

  @Test def factory_nanos_negative(): Unit = {
    val test: Duration = Duration.ofNanos(-2000000001)
    assertEquals(test.getSeconds, -3)
    assertEquals(test.getNano, 999999999)
  }

  @Test def factory_nanos_max(): Unit = {
    val test: Duration = Duration.ofNanos(Long.MaxValue)
    assertEquals(test.getSeconds, Long.MaxValue / 1000000000)
    assertEquals(test.getNano, Long.MaxValue % 1000000000)
  }

  @Test def factory_nanos_min(): Unit = {
    val test: Duration = Duration.ofNanos(Long.MinValue)
    assertEquals(test.getSeconds, Long.MinValue / 1000000000 - 1)
    assertEquals(test.getNano, Long.MinValue % 1000000000 + 1000000000)
  }

  @Test def factory_minutes(): Unit = {
    val test: Duration = Duration.ofMinutes(2)
    assertEquals(test.getSeconds, 120)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_minutes_max(): Unit = {
    val test: Duration = Duration.ofMinutes(Long.MaxValue / 60)
    assertEquals(test.getSeconds, (Long.MaxValue / 60) * 60)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_minutes_min(): Unit = {
    val test: Duration = Duration.ofMinutes(Long.MinValue / 60)
    assertEquals(test.getSeconds, (Long.MinValue / 60) * 60)
    assertEquals(test.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_minutes_tooBig(): Unit = {
    Duration.ofMinutes(Long.MaxValue / 60 + 1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_minutes_tooSmall(): Unit = {
    Duration.ofMinutes(Long.MinValue / 60 - 1)
  }

  @Test def factory_hours(): Unit = {
    val test: Duration = Duration.ofHours(2)
    assertEquals(test.getSeconds, 2 * 3600)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_hours_max(): Unit = {
    val test: Duration = Duration.ofHours(Long.MaxValue / 3600)
    assertEquals(test.getSeconds, (Long.MaxValue / 3600) * 3600)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_hours_min(): Unit = {
    val test: Duration = Duration.ofHours(Long.MinValue / 3600)
    assertEquals(test.getSeconds, (Long.MinValue / 3600) * 3600)
    assertEquals(test.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_hours_tooBig(): Unit = {
    Duration.ofHours(Long.MaxValue / 3600 + 1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_hours_tooSmall(): Unit = {
    Duration.ofHours(Long.MinValue / 3600 - 1)
  }

  @Test def factory_days(): Unit = {
    val test: Duration = Duration.ofDays(2)
    assertEquals(test.getSeconds, 2 * 86400)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_days_max(): Unit = {
    val test: Duration = Duration.ofDays(Long.MaxValue / 86400)
    assertEquals(test.getSeconds, (Long.MaxValue / 86400) * 86400)
    assertEquals(test.getNano, 0)
  }

  @Test def factory_days_min(): Unit = {
    val test: Duration = Duration.ofDays(Long.MinValue / 86400)
    assertEquals(test.getSeconds, (Long.MinValue / 86400) * 86400)
    assertEquals(test.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_days_tooBig(): Unit = {
    Duration.ofDays(Long.MaxValue / 86400 + 1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_days_tooSmall(): Unit = {
    Duration.ofDays(Long.MinValue / 86400 - 1)
  }

  @DataProvider(name = "OfTemporalUnit") private[bp] def provider_factory_of_longTemporalUnit: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, NANOS, 0, 0), Array(0, MICROS, 0, 0), Array(0, MILLIS, 0, 0), Array(0, SECONDS, 0, 0), Array(0, MINUTES, 0, 0), Array(0, HOURS, 0, 0), Array(0, HALF_DAYS, 0, 0), Array(0, DAYS, 0, 0), Array(1, NANOS, 0, 1), Array(1, MICROS, 0, 1000), Array(1, MILLIS, 0, 1000000), Array(1, SECONDS, 1, 0), Array(1, MINUTES, 60, 0), Array(1, HOURS, 3600, 0), Array(1, HALF_DAYS, 43200, 0), Array(1, DAYS, 86400, 0), Array(3, NANOS, 0, 3), Array(3, MICROS, 0, 3000), Array(3, MILLIS, 0, 3000000), Array(3, SECONDS, 3, 0), Array(3, MINUTES, 3 * 60, 0), Array(3, HOURS, 3 * 3600, 0), Array(3, HALF_DAYS, 3 * 43200, 0), Array(3, DAYS, 3 * 86400, 0), Array(-1, NANOS, -1, 999999999), Array(-1, MICROS, -1, 999999000), Array(-1, MILLIS, -1, 999000000), Array(-1, SECONDS, -1, 0), Array(-1, MINUTES, -60, 0), Array(-1, HOURS, -3600, 0), Array(-1, HALF_DAYS, -43200, 0), Array(-1, DAYS, -86400, 0), Array(-3, NANOS, -1, 999999997), Array(-3, MICROS, -1, 999997000), Array(-3, MILLIS, -1, 997000000), Array(-3, SECONDS, -3, 0), Array(-3, MINUTES, -3 * 60, 0), Array(-3, HOURS, -3 * 3600, 0), Array(-3, HALF_DAYS, -3 * 43200, 0), Array(-3, DAYS, -3 * 86400, 0), Array(Long.MaxValue, NANOS, Long.MaxValue / 1000000000, (Long.MaxValue % 1000000000).toInt), Array(Long.MinValue, NANOS, Long.MinValue / 1000000000 - 1, (Long.MinValue % 1000000000 + 1000000000).toInt), Array(Long.MaxValue, MICROS, Long.MaxValue / 1000000, ((Long.MaxValue % 1000000) * 1000).toInt), Array(Long.MinValue, MICROS, Long.MinValue / 1000000 - 1, ((Long.MinValue % 1000000 + 1000000) * 1000).toInt), Array(Long.MaxValue, MILLIS, Long.MaxValue / 1000, ((Long.MaxValue % 1000) * 1000000).toInt), Array(Long.MinValue, MILLIS, Long.MinValue / 1000 - 1, ((Long.MinValue % 1000 + 1000) * 1000000).toInt), Array(Long.MaxValue, SECONDS, Long.MaxValue, 0), Array(Long.MinValue, SECONDS, Long.MinValue, 0), Array(Long.MaxValue / 60, MINUTES, (Long.MaxValue / 60) * 60, 0), Array(Long.MinValue / 60, MINUTES, (Long.MinValue / 60) * 60, 0), Array(Long.MaxValue / 3600, HOURS, (Long.MaxValue / 3600) * 3600, 0), Array(Long.MinValue / 3600, HOURS, (Long.MinValue / 3600) * 3600, 0), Array(Long.MaxValue / 43200, HALF_DAYS, (Long.MaxValue / 43200) * 43200, 0), Array(Long.MinValue / 43200, HALF_DAYS, (Long.MinValue / 43200) * 43200, 0))
  }

  @Test(dataProvider = "OfTemporalUnit") def factory_of_longTemporalUnit(amount: Long, unit: TemporalUnit, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Duration = Duration.of(amount, unit)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @DataProvider(name = "OfTemporalUnitOutOfRange") private[bp] def provider_factory_of_longTemporalUnit_outOfRange: Array[Array[Any]] = {
    Array[Array[Any]](Array(Long.MaxValue / 60 + 1, MINUTES), Array(Long.MinValue / 60 - 1, MINUTES), Array(Long.MaxValue / 3600 + 1, HOURS), Array(Long.MinValue / 3600 - 1, HOURS), Array(Long.MaxValue / 43200 + 1, HALF_DAYS), Array(Long.MinValue / 43200 - 1, HALF_DAYS))
  }

  @Test(dataProvider = "OfTemporalUnitOutOfRange", expectedExceptions = Array(classOf[ArithmeticException])) def factory_of_longTemporalUnit_outOfRange(amount: Long, unit: TemporalUnit): Unit = {
    Duration.of(amount, unit)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_longTemporalUnit_estimatedUnit(): Unit = {
    Duration.of(2, WEEKS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_longTemporalUnit_null(): Unit = {
    Duration.of(1, null.asInstanceOf[TemporalUnit])
  }

  @DataProvider(name = "DurationBetween") private[bp] def provider_factory_between_Instant_Instant: Array[Array[_ <: AnyRef]] = {
    Array[Array[_ <: AnyRef]](Array[Integer](0, 0, 0, 0, 0, 0), Array[Integer](3, 0, 7, 0, 4, 0), Array[Integer](3, 20, 7, 50, 4, 30), Array[Integer](3, 80, 7, 50, 3, 999999970), Array[Integer](7, 0, 3, 0, -4, 0))
  }

  @Test(dataProvider = "DurationBetween") def factory_between_Instant_Instant(secs1: Long, nanos1: Int, secs2: Long, nanos2: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val start: Instant = Instant.ofEpochSecond(secs1, nanos1)
    val end: Instant = Instant.ofEpochSecond(secs2, nanos2)
    val t: Duration = Duration.between(start, end)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_between_Instant_Instant_startNull(): Unit = {
    val end: Instant = Instant.ofEpochSecond(1)
    Duration.between(null, end)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_between_Instant_Instant_endNull(): Unit = {
    val start: Instant = Instant.ofEpochSecond(1)
    Duration.between(start, null)
  }

  @DataProvider(name = "Parse") private[bp] def provider_factory_parse: Array[Array[Any]] = {
    Array[Array[Any]](Array("PT0S", 0, 0), Array("PT1S", 1, 0), Array("PT12S", 12, 0), Array("PT123456789S", 123456789, 0), Array("PT" + Long.MaxValue + "S", Long.MaxValue, 0), Array("PT+1S", 1, 0), Array("PT+12S", 12, 0), Array("PT-1S", -1, 0), Array("PT-12S", -12, 0), Array("PT-123456789S", -123456789, 0), Array("PT" + Long.MinValue + "S", Long.MinValue, 0), Array("PT0.1S", 0, 100000000), Array("PT1.1S", 1, 100000000), Array("PT1.12S", 1, 120000000), Array("PT1.123S", 1, 123000000), Array("PT1.1234S", 1, 123400000), Array("PT1.12345S", 1, 123450000), Array("PT1.123456S", 1, 123456000), Array("PT1.1234567S", 1, 123456700), Array("PT1.12345678S", 1, 123456780), Array("PT1.123456789S", 1, 123456789), Array("PT-0.1S", -1, 1000000000 - 100000000), Array("PT-1.1S", -2, 1000000000 - 100000000), Array("PT-1.12S", -2, 1000000000 - 120000000), Array("PT-1.123S", -2, 1000000000 - 123000000), Array("PT-1.1234S", -2, 1000000000 - 123400000), Array("PT-1.12345S", -2, 1000000000 - 123450000), Array("PT-1.123456S", -2, 1000000000 - 123456000), Array("PT-1.1234567S", -2, 1000000000 - 123456700), Array("PT-1.12345678S", -2, 1000000000 - 123456780), Array("PT-1.123456789S", -2, 1000000000 - 123456789), Array("PT" + Long.MaxValue + ".123456789S", Long.MaxValue, 123456789), Array("PT" + Long.MinValue + ".000000000S", Long.MinValue, 0), Array("PT12M", 12 * 60, 0), Array("PT12M0.35S", 12 * 60, 350000000), Array("PT12M1.35S", 12 * 60 + 1, 350000000), Array("PT12M-0.35S", 12 * 60 - 1, 1000000000 - 350000000), Array("PT12M-1.35S", 12 * 60 - 2, 1000000000 - 350000000), Array("PT12H", 12 * 3600, 0), Array("PT12H0.35S", 12 * 3600, 350000000), Array("PT12H1.35S", 12 * 3600 + 1, 350000000), Array("PT12H-0.35S", 12 * 3600 - 1, 1000000000 - 350000000), Array("PT12H-1.35S", 12 * 3600 - 2, 1000000000 - 350000000), Array("P12D", 12 * 24 * 3600, 0), Array("P12DT0.35S", 12 * 24 * 3600, 350000000), Array("P12DT1.35S", 12 * 24 * 3600 + 1, 350000000), Array("P12DT-0.35S", 12 * 24 * 3600 - 1, 1000000000 - 350000000), Array("P12DT-1.35S", 12 * 24 * 3600 - 2, 1000000000 - 350000000))
  }

  @Test(dataProvider = "Parse") def factory_parse(text: String, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Duration = Duration.parse(text)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "Parse") def factory_parse_ignoreCase(text: String, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Duration = Duration.parse(text.toLowerCase(Locale.ENGLISH))
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "Parse") def factory_parse_comma(text: String, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var _text = text
    _text = _text.replace('.', ',')
    val t: Duration = Duration.parse(_text)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @DataProvider(name = "ParseFailures") private[bp] def provider_factory_parseFailures: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(""), Array("PTS"), Array("AT0S"), Array("PA0S"), Array("PT0A"), Array("PT+S"), Array("PT-S"), Array("PT.S"), Array("PTAS"), Array("PT-.S"), Array("PT+.S"), Array("PT1ABC2S"), Array("PT1.1ABC2S"), Array("PT123456789123456789123456789S"), Array("PT0.1234567891S"), Array("PT.1S"), Array("PT2.-3"), Array("PT-2.-3"), Array("PT2.+3"), Array("PT-2.+3"))
  }

  @Test(dataProvider = "ParseFailures", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parseFailures(text: String): Unit = {
    Duration.parse(text)
  }

  @Test(dataProvider = "ParseFailures", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parseFailures_comma(text: String): Unit = {
    var _text = text
    _text = _text.replace('.', ',')
    Duration.parse(_text)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_tooBig(): Unit = {
    Duration.parse("PT" + Long.MaxValue + "1S")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_tooBig_decimal(): Unit = {
    Duration.parse("PT" + Long.MaxValue + "1.1S")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_tooSmall(): Unit = {
    Duration.parse("PT" + Long.MinValue + "1S")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_tooSmall_decimal(): Unit = {
    Duration.parse("PT" + Long.MinValue + ".1S")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    Duration.parse(null.asInstanceOf[String])
  }

  @Test
  @throws(classOf[Exception])
  def test_deserialization(): Unit = {
    val orginal: Duration = Duration.ofSeconds(2)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val out: ObjectOutputStream = new ObjectOutputStream(baos)
    out.writeObject(orginal)
    out.close()
    val bais: ByteArrayInputStream = new ByteArrayInputStream(baos.toByteArray)
    val in: ObjectInputStream = new ObjectInputStream(bais)
    val ser: Duration = in.readObject.asInstanceOf[Duration]
    assertEquals(Duration.ofSeconds(2), ser)
  }

  @Test def test_isZero(): Unit = {
    assertEquals(Duration.ofNanos(0).isZero, true)
    assertEquals(Duration.ofSeconds(0).isZero, true)
    assertEquals(Duration.ofNanos(1).isZero, false)
    assertEquals(Duration.ofSeconds(1).isZero, false)
    assertEquals(Duration.ofSeconds(1, 1).isZero, false)
    assertEquals(Duration.ofNanos(-1).isZero, false)
    assertEquals(Duration.ofSeconds(-1).isZero, false)
    assertEquals(Duration.ofSeconds(-1, -1).isZero, false)
  }

  @Test def test_isNegative(): Unit = {
    assertEquals(Duration.ofNanos(0).isNegative, false)
    assertEquals(Duration.ofSeconds(0).isNegative, false)
    assertEquals(Duration.ofNanos(1).isNegative, false)
    assertEquals(Duration.ofSeconds(1).isNegative, false)
    assertEquals(Duration.ofSeconds(1, 1).isNegative, false)
    assertEquals(Duration.ofNanos(-1).isNegative, true)
    assertEquals(Duration.ofSeconds(-1).isNegative, true)
    assertEquals(Duration.ofSeconds(-1, -1).isNegative, true)
  }

  @DataProvider(name = "Plus") private[bp] def provider_plus: Array[Array[Any]] = {
    Array[Array[Any]](Array(Long.MinValue, 0, Long.MaxValue, 0, -1, 0), Array(-4, 666666667, -4, 666666667, -7, 333333334), Array(-4, 666666667, -3, 0, -7, 666666667), Array(-4, 666666667, -2, 0, -6, 666666667), Array(-4, 666666667, -1, 0, -5, 666666667), Array(-4, 666666667, -1, 333333334, -4, 1), Array(-4, 666666667, -1, 666666667, -4, 333333334), Array(-4, 666666667, -1, 999999999, -4, 666666666), Array(-4, 666666667, 0, 0, -4, 666666667), Array(-4, 666666667, 0, 1, -4, 666666668), Array(-4, 666666667, 0, 333333333, -3, 0), Array(-4, 666666667, 0, 666666666, -3, 333333333), Array(-4, 666666667, 1, 0, -3, 666666667), Array(-4, 666666667, 2, 0, -2, 666666667), Array(-4, 666666667, 3, 0, -1, 666666667), Array(-4, 666666667, 3, 333333333, 0, 0), Array(-3, 0, -4, 666666667, -7, 666666667), Array(-3, 0, -3, 0, -6, 0), Array(-3, 0, -2, 0, -5, 0), Array(-3, 0, -1, 0, -4, 0), Array(-3, 0, -1, 333333334, -4, 333333334), Array(-3, 0, -1, 666666667, -4, 666666667), Array(-3, 0, -1, 999999999, -4, 999999999), Array(-3, 0, 0, 0, -3, 0), Array(-3, 0, 0, 1, -3, 1), Array(-3, 0, 0, 333333333, -3, 333333333), Array(-3, 0, 0, 666666666, -3, 666666666), Array(-3, 0, 1, 0, -2, 0), Array(-3, 0, 2, 0, -1, 0), Array(-3, 0, 3, 0, 0, 0), Array(-3, 0, 3, 333333333, 0, 333333333), Array(-2, 0, -4, 666666667, -6, 666666667), Array(-2, 0, -3, 0, -5, 0), Array(-2, 0, -2, 0, -4, 0), Array(-2, 0, -1, 0, -3, 0), Array(-2, 0, -1, 333333334, -3, 333333334), Array(-2, 0, -1, 666666667, -3, 666666667), Array(-2, 0, -1, 999999999, -3, 999999999), Array(-2, 0, 0, 0, -2, 0), Array(-2, 0, 0, 1, -2, 1), Array(-2, 0, 0, 333333333, -2, 333333333), Array(-2, 0, 0, 666666666, -2, 666666666), Array(-2, 0, 1, 0, -1, 0), Array(-2, 0, 2, 0, 0, 0), Array(-2, 0, 3, 0, 1, 0), Array(-2, 0, 3, 333333333, 1, 333333333), Array(-1, 0, -4, 666666667, -5, 666666667), Array(-1, 0, -3, 0, -4, 0), Array(-1, 0, -2, 0, -3, 0), Array(-1, 0, -1, 0, -2, 0), Array(-1, 0, -1, 333333334, -2, 333333334), Array(-1, 0, -1, 666666667, -2, 666666667), Array(-1, 0, -1, 999999999, -2, 999999999), Array(-1, 0, 0, 0, -1, 0), Array(-1, 0, 0, 1, -1, 1), Array(-1, 0, 0, 333333333, -1, 333333333), Array(-1, 0, 0, 666666666, -1, 666666666), Array(-1, 0, 1, 0, 0, 0), Array(-1, 0, 2, 0, 1, 0), Array(-1, 0, 3, 0, 2, 0), Array(-1, 0, 3, 333333333, 2, 333333333), Array(-1, 666666667, -4, 666666667, -4, 333333334), Array(-1, 666666667, -3, 0, -4, 666666667), Array(-1, 666666667, -2, 0, -3, 666666667), Array(-1, 666666667, -1, 0, -2, 666666667), Array(-1, 666666667, -1, 333333334, -1, 1), Array(-1, 666666667, -1, 666666667, -1, 333333334), Array(-1, 666666667, -1, 999999999, -1, 666666666), Array(-1, 666666667, 0, 0, -1, 666666667), Array(-1, 666666667, 0, 1, -1, 666666668), Array(-1, 666666667, 0, 333333333, 0, 0), Array(-1, 666666667, 0, 666666666, 0, 333333333), Array(-1, 666666667, 1, 0, 0, 666666667), Array(-1, 666666667, 2, 0, 1, 666666667), Array(-1, 666666667, 3, 0, 2, 666666667), Array(-1, 666666667, 3, 333333333, 3, 0), Array(0, 0, -4, 666666667, -4, 666666667), Array(0, 0, -3, 0, -3, 0), Array(0, 0, -2, 0, -2, 0), Array(0, 0, -1, 0, -1, 0), Array(0, 0, -1, 333333334, -1, 333333334), Array(0, 0, -1, 666666667, -1, 666666667), Array(0, 0, -1, 999999999, -1, 999999999), Array(0, 0, 0, 0, 0, 0), Array(0, 0, 0, 1, 0, 1), Array(0, 0, 0, 333333333, 0, 333333333), Array(0, 0, 0, 666666666, 0, 666666666), Array(0, 0, 1, 0, 1, 0), Array(0, 0, 2, 0, 2, 0), Array(0, 0, 3, 0, 3, 0), Array(0, 0, 3, 333333333, 3, 333333333), Array(0, 333333333, -4, 666666667, -3, 0), Array(0, 333333333, -3, 0, -3, 333333333), Array(0, 333333333, -2, 0, -2, 333333333), Array(0, 333333333, -1, 0, -1, 333333333), Array(0, 333333333, -1, 333333334, -1, 666666667), Array(0, 333333333, -1, 666666667, 0, 0), Array(0, 333333333, -1, 999999999, 0, 333333332), Array(0, 333333333, 0, 0, 0, 333333333), Array(0, 333333333, 0, 1, 0, 333333334), Array(0, 333333333, 0, 333333333, 0, 666666666), Array(0, 333333333, 0, 666666666, 0, 999999999), Array(0, 333333333, 1, 0, 1, 333333333), Array(0, 333333333, 2, 0, 2, 333333333), Array(0, 333333333, 3, 0, 3, 333333333), Array(0, 333333333, 3, 333333333, 3, 666666666), Array(1, 0, -4, 666666667, -3, 666666667), Array(1, 0, -3, 0, -2, 0), Array(1, 0, -2, 0, -1, 0), Array(1, 0, -1, 0, 0, 0), Array(1, 0, -1, 333333334, 0, 333333334), Array(1, 0, -1, 666666667, 0, 666666667), Array(1, 0, -1, 999999999, 0, 999999999), Array(1, 0, 0, 0, 1, 0), Array(1, 0, 0, 1, 1, 1), Array(1, 0, 0, 333333333, 1, 333333333), Array(1, 0, 0, 666666666, 1, 666666666), Array(1, 0, 1, 0, 2, 0), Array(1, 0, 2, 0, 3, 0), Array(1, 0, 3, 0, 4, 0), Array(1, 0, 3, 333333333, 4, 333333333), Array(2, 0, -4, 666666667, -2, 666666667), Array(2, 0, -3, 0, -1, 0), Array(2, 0, -2, 0, 0, 0), Array(2, 0, -1, 0, 1, 0), Array(2, 0, -1, 333333334, 1, 333333334), Array(2, 0, -1, 666666667, 1, 666666667), Array(2, 0, -1, 999999999, 1, 999999999), Array(2, 0, 0, 0, 2, 0), Array(2, 0, 0, 1, 2, 1), Array(2, 0, 0, 333333333, 2, 333333333), Array(2, 0, 0, 666666666, 2, 666666666), Array(2, 0, 1, 0, 3, 0), Array(2, 0, 2, 0, 4, 0), Array(2, 0, 3, 0, 5, 0), Array(2, 0, 3, 333333333, 5, 333333333), Array(3, 0, -4, 666666667, -1, 666666667), Array(3, 0, -3, 0, 0, 0), Array(3, 0, -2, 0, 1, 0), Array(3, 0, -1, 0, 2, 0), Array(3, 0, -1, 333333334, 2, 333333334), Array(3, 0, -1, 666666667, 2, 666666667), Array(3, 0, -1, 999999999, 2, 999999999), Array(3, 0, 0, 0, 3, 0), Array(3, 0, 0, 1, 3, 1), Array(3, 0, 0, 333333333, 3, 333333333), Array(3, 0, 0, 666666666, 3, 666666666), Array(3, 0, 1, 0, 4, 0), Array(3, 0, 2, 0, 5, 0), Array(3, 0, 3, 0, 6, 0), Array(3, 0, 3, 333333333, 6, 333333333), Array(3, 333333333, -4, 666666667, 0, 0), Array(3, 333333333, -3, 0, 0, 333333333), Array(3, 333333333, -2, 0, 1, 333333333), Array(3, 333333333, -1, 0, 2, 333333333), Array(3, 333333333, -1, 333333334, 2, 666666667), Array(3, 333333333, -1, 666666667, 3, 0), Array(3, 333333333, -1, 999999999, 3, 333333332), Array(3, 333333333, 0, 0, 3, 333333333), Array(3, 333333333, 0, 1, 3, 333333334), Array(3, 333333333, 0, 333333333, 3, 666666666), Array(3, 333333333, 0, 666666666, 3, 999999999), Array(3, 333333333, 1, 0, 4, 333333333), Array(3, 333333333, 2, 0, 5, 333333333), Array(3, 333333333, 3, 0, 6, 333333333), Array(3, 333333333, 3, 333333333, 6, 666666666), Array(Long.MaxValue, 0, Long.MinValue, 0, -1, 0))
  }

  @Test(dataProvider = "Plus") def plus(seconds: Long, nanos: Int, otherSeconds: Long, otherNanos: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Duration = Duration.ofSeconds(seconds, nanos).plus(Duration.ofSeconds(otherSeconds, otherNanos))
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusOverflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MaxValue, 999999999)
    t.plus(Duration.ofSeconds(0, 1))
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusOverflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MinValue)
    t.plus(Duration.ofSeconds(-1, 999999999))
  }

  @Test def plus_longTemporalUnit_seconds(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.plus(1, SECONDS)
    assertEquals(2, t.getSeconds)
    assertEquals(0, t.getNano)
  }

  @Test def plus_longTemporalUnit_millis(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.plus(1, MILLIS)
    assertEquals(1, t.getSeconds)
    assertEquals(1000000, t.getNano)
  }

  @Test def plus_longTemporalUnit_micros(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.plus(1, MICROS)
    assertEquals(1, t.getSeconds)
    assertEquals(1000, t.getNano)
  }

  @Test def plus_longTemporalUnit_nanos(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.plus(1, NANOS)
    assertEquals(1, t.getSeconds)
    assertEquals(1, t.getNano)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def plus_longTemporalUnit_null(): Unit = {
    val t: Duration = Duration.ofSeconds(1)
    t.plus(1, null.asInstanceOf[TemporalUnit])
  }

  @DataProvider(name = "PlusSeconds") private[bp] def provider_plusSeconds_long: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, 1, 0), Array(0, 0, -1, -1, 0), Array(0, 0, Long.MaxValue, Long.MaxValue, 0), Array(0, 0, Long.MinValue, Long.MinValue, 0), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 2, 0), Array(1, 0, -1, 0, 0), Array(1, 0, Long.MaxValue - 1, Long.MaxValue, 0), Array(1, 0, Long.MinValue, Long.MinValue + 1, 0), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 2, 1), Array(1, 1, -1, 0, 1), Array(1, 1, Long.MaxValue - 1, Long.MaxValue, 1), Array(1, 1, Long.MinValue, Long.MinValue + 1, 1), Array(-1, 1, 0, -1, 1), Array(-1, 1, 1, 0, 1), Array(-1, 1, -1, -2, 1), Array(-1, 1, Long.MaxValue, Long.MaxValue - 1, 1), Array(-1, 1, Long.MinValue + 1, Long.MinValue, 1))
  }

  @Test(dataProvider = "PlusSeconds") def plusSeconds_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.plusSeconds(amount)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusSeconds_long_overflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(1, 0)
    t.plusSeconds(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusSeconds_long_overflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(-1, 0)
    t.plusSeconds(Long.MinValue)
  }

  @DataProvider(name = "PlusMillis") private[bp] def provider_plusMillis_long: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, 0, 1000000), Array(0, 0, 999, 0, 999000000), Array(0, 0, 1000, 1, 0), Array(0, 0, 1001, 1, 1000000), Array(0, 0, 1999, 1, 999000000), Array(0, 0, 2000, 2, 0), Array(0, 0, -1, -1, 999000000), Array(0, 0, -999, -1, 1000000), Array(0, 0, -1000, -1, 0), Array(0, 0, -1001, -2, 999000000), Array(0, 0, -1999, -2, 1000000), Array(0, 1, 0, 0, 1), Array(0, 1, 1, 0, 1000001), Array(0, 1, 998, 0, 998000001), Array(0, 1, 999, 0, 999000001), Array(0, 1, 1000, 1, 1), Array(0, 1, 1998, 1, 998000001), Array(0, 1, 1999, 1, 999000001), Array(0, 1, 2000, 2, 1), Array(0, 1, -1, -1, 999000001), Array(0, 1, -2, -1, 998000001), Array(0, 1, -1000, -1, 1), Array(0, 1, -1001, -2, 999000001), Array(0, 1000000, 0, 0, 1000000), Array(0, 1000000, 1, 0, 2000000), Array(0, 1000000, 998, 0, 999000000), Array(0, 1000000, 999, 1, 0), Array(0, 1000000, 1000, 1, 1000000), Array(0, 1000000, 1998, 1, 999000000), Array(0, 1000000, 1999, 2, 0), Array(0, 1000000, 2000, 2, 1000000), Array(0, 1000000, -1, 0, 0), Array(0, 1000000, -2, -1, 999000000), Array(0, 1000000, -999, -1, 2000000), Array(0, 1000000, -1000, -1, 1000000), Array(0, 1000000, -1001, -1, 0), Array(0, 1000000, -1002, -2, 999000000), Array(0, 999999999, 0, 0, 999999999), Array(0, 999999999, 1, 1, 999999), Array(0, 999999999, 999, 1, 998999999), Array(0, 999999999, 1000, 1, 999999999), Array(0, 999999999, 1001, 2, 999999), Array(0, 999999999, -1, 0, 998999999), Array(0, 999999999, -1000, -1, 999999999), Array(0, 999999999, -1001, -1, 998999999))
  }

  @Test(dataProvider = "PlusMillis") def plusMillis_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.plusMillis(amount)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "PlusMillis") def plusMillis_long_oneMore(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds + 1, nanos)
    t = t.plusMillis(amount)
    assertEquals(t.getSeconds, expectedSeconds + 1)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "PlusMillis") def plusMillis_long_minusOneLess(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds - 1, nanos)
    t = t.plusMillis(amount)
    assertEquals(t.getSeconds, expectedSeconds - 1)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test def plusMillis_long_max(): Unit = {
    var t: Duration = Duration.ofSeconds(Long.MaxValue, 998999999)
    t = t.plusMillis(1)
    assertEquals(t.getSeconds, Long.MaxValue)
    assertEquals(t.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusMillis_long_overflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MaxValue, 999000000)
    t.plusMillis(1)
  }

  @Test def plusMillis_long_min(): Unit = {
    var t: Duration = Duration.ofSeconds(Long.MinValue, 1000000)
    t = t.plusMillis(-1)
    assertEquals(t.getSeconds, Long.MinValue)
    assertEquals(t.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusMillis_long_overflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MinValue, 0)
    t.plusMillis(-1)
  }

  @DataProvider(name = "PlusNanos") private[bp] def provider_plusNanos_long: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, 0, 1), Array(0, 0, 999999999, 0, 999999999), Array(0, 0, 1000000000, 1, 0), Array(0, 0, 1000000001, 1, 1), Array(0, 0, 1999999999, 1, 999999999), Array(0, 0, 2000000000, 2, 0), Array(0, 0, -1, -1, 999999999), Array(0, 0, -999999999, -1, 1), Array(0, 0, -1000000000, -1, 0), Array(0, 0, -1000000001, -2, 999999999), Array(0, 0, -1999999999, -2, 1), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 1, 1), Array(1, 0, 999999999, 1, 999999999), Array(1, 0, 1000000000, 2, 0), Array(1, 0, 1000000001, 2, 1), Array(1, 0, 1999999999, 2, 999999999), Array(1, 0, 2000000000, 3, 0), Array(1, 0, -1, 0, 999999999), Array(1, 0, -999999999, 0, 1), Array(1, 0, -1000000000, 0, 0), Array(1, 0, -1000000001, -1, 999999999), Array(1, 0, -1999999999, -1, 1), Array(-1, 0, 0, -1, 0), Array(-1, 0, 1, -1, 1), Array(-1, 0, 999999999, -1, 999999999), Array(-1, 0, 1000000000, 0, 0), Array(-1, 0, 1000000001, 0, 1), Array(-1, 0, 1999999999, 0, 999999999), Array(-1, 0, 2000000000, 1, 0), Array(-1, 0, -1, -2, 999999999), Array(-1, 0, -999999999, -2, 1), Array(-1, 0, -1000000000, -2, 0), Array(-1, 0, -1000000001, -3, 999999999), Array(-1, 0, -1999999999, -3, 1), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 1, 2), Array(1, 1, 999999998, 1, 999999999), Array(1, 1, 999999999, 2, 0), Array(1, 1, 1000000000, 2, 1), Array(1, 1, 1999999998, 2, 999999999), Array(1, 1, 1999999999, 3, 0), Array(1, 1, 2000000000, 3, 1), Array(1, 1, -1, 1, 0), Array(1, 1, -2, 0, 999999999), Array(1, 1, -1000000000, 0, 1), Array(1, 1, -1000000001, 0, 0), Array(1, 1, -1000000002, -1, 999999999), Array(1, 1, -2000000000, -1, 1), Array(1, 999999999, 0, 1, 999999999), Array(1, 999999999, 1, 2, 0), Array(1, 999999999, 999999999, 2, 999999998), Array(1, 999999999, 1000000000, 2, 999999999), Array(1, 999999999, 1000000001, 3, 0), Array(1, 999999999, -1, 1, 999999998), Array(1, 999999999, -1000000000, 0, 999999999), Array(1, 999999999, -1000000001, 0, 999999998), Array(1, 999999999, -1999999999, 0, 0), Array(1, 999999999, -2000000000, -1, 999999999), Array(Long.MaxValue, 0, 999999999, Long.MaxValue, 999999999), Array(Long.MaxValue - 1, 0, 1999999999, Long.MaxValue, 999999999), Array(Long.MinValue, 1, -1, Long.MinValue, 0), Array(Long.MinValue + 1, 1, -1000000001, Long.MinValue, 0))
  }

  @Test(dataProvider = "PlusNanos") def plusNanos_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.plusNanos(amount)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusNanos_long_overflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MaxValue, 999999999)
    t.plusNanos(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusNanos_long_overflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MinValue, 0)
    t.plusNanos(-1)
  }

  @DataProvider(name = "Minus") private[bp] def provider_minus: Array[Array[Any]] = {
    Array[Array[Any]](Array(Long.MinValue, 0, Long.MinValue + 1, 0, -1, 0), Array(-4, 666666667, -4, 666666667, 0, 0), Array(-4, 666666667, -3, 0, -1, 666666667), Array(-4, 666666667, -2, 0, -2, 666666667), Array(-4, 666666667, -1, 0, -3, 666666667), Array(-4, 666666667, -1, 333333334, -3, 333333333), Array(-4, 666666667, -1, 666666667, -3, 0), Array(-4, 666666667, -1, 999999999, -4, 666666668), Array(-4, 666666667, 0, 0, -4, 666666667), Array(-4, 666666667, 0, 1, -4, 666666666), Array(-4, 666666667, 0, 333333333, -4, 333333334), Array(-4, 666666667, 0, 666666666, -4, 1), Array(-4, 666666667, 1, 0, -5, 666666667), Array(-4, 666666667, 2, 0, -6, 666666667), Array(-4, 666666667, 3, 0, -7, 666666667), Array(-4, 666666667, 3, 333333333, -7, 333333334), Array(-3, 0, -4, 666666667, 0, 333333333), Array(-3, 0, -3, 0, 0, 0), Array(-3, 0, -2, 0, -1, 0), Array(-3, 0, -1, 0, -2, 0), Array(-3, 0, -1, 333333334, -3, 666666666), Array(-3, 0, -1, 666666667, -3, 333333333), Array(-3, 0, -1, 999999999, -3, 1), Array(-3, 0, 0, 0, -3, 0), Array(-3, 0, 0, 1, -4, 999999999), Array(-3, 0, 0, 333333333, -4, 666666667), Array(-3, 0, 0, 666666666, -4, 333333334), Array(-3, 0, 1, 0, -4, 0), Array(-3, 0, 2, 0, -5, 0), Array(-3, 0, 3, 0, -6, 0), Array(-3, 0, 3, 333333333, -7, 666666667), Array(-2, 0, -4, 666666667, 1, 333333333), Array(-2, 0, -3, 0, 1, 0), Array(-2, 0, -2, 0, 0, 0), Array(-2, 0, -1, 0, -1, 0), Array(-2, 0, -1, 333333334, -2, 666666666), Array(-2, 0, -1, 666666667, -2, 333333333), Array(-2, 0, -1, 999999999, -2, 1), Array(-2, 0, 0, 0, -2, 0), Array(-2, 0, 0, 1, -3, 999999999), Array(-2, 0, 0, 333333333, -3, 666666667), Array(-2, 0, 0, 666666666, -3, 333333334), Array(-2, 0, 1, 0, -3, 0), Array(-2, 0, 2, 0, -4, 0), Array(-2, 0, 3, 0, -5, 0), Array(-2, 0, 3, 333333333, -6, 666666667), Array(-1, 0, -4, 666666667, 2, 333333333), Array(-1, 0, -3, 0, 2, 0), Array(-1, 0, -2, 0, 1, 0), Array(-1, 0, -1, 0, 0, 0), Array(-1, 0, -1, 333333334, -1, 666666666), Array(-1, 0, -1, 666666667, -1, 333333333), Array(-1, 0, -1, 999999999, -1, 1), Array(-1, 0, 0, 0, -1, 0), Array(-1, 0, 0, 1, -2, 999999999), Array(-1, 0, 0, 333333333, -2, 666666667), Array(-1, 0, 0, 666666666, -2, 333333334), Array(-1, 0, 1, 0, -2, 0), Array(-1, 0, 2, 0, -3, 0), Array(-1, 0, 3, 0, -4, 0), Array(-1, 0, 3, 333333333, -5, 666666667), Array(-1, 666666667, -4, 666666667, 3, 0), Array(-1, 666666667, -3, 0, 2, 666666667), Array(-1, 666666667, -2, 0, 1, 666666667), Array(-1, 666666667, -1, 0, 0, 666666667), Array(-1, 666666667, -1, 333333334, 0, 333333333), Array(-1, 666666667, -1, 666666667, 0, 0), Array(-1, 666666667, -1, 999999999, -1, 666666668), Array(-1, 666666667, 0, 0, -1, 666666667), Array(-1, 666666667, 0, 1, -1, 666666666), Array(-1, 666666667, 0, 333333333, -1, 333333334), Array(-1, 666666667, 0, 666666666, -1, 1), Array(-1, 666666667, 1, 0, -2, 666666667), Array(-1, 666666667, 2, 0, -3, 666666667), Array(-1, 666666667, 3, 0, -4, 666666667), Array(-1, 666666667, 3, 333333333, -4, 333333334), Array(0, 0, -4, 666666667, 3, 333333333), Array(0, 0, -3, 0, 3, 0), Array(0, 0, -2, 0, 2, 0), Array(0, 0, -1, 0, 1, 0), Array(0, 0, -1, 333333334, 0, 666666666), Array(0, 0, -1, 666666667, 0, 333333333), Array(0, 0, -1, 999999999, 0, 1), Array(0, 0, 0, 0, 0, 0), Array(0, 0, 0, 1, -1, 999999999), Array(0, 0, 0, 333333333, -1, 666666667), Array(0, 0, 0, 666666666, -1, 333333334), Array(0, 0, 1, 0, -1, 0), Array(0, 0, 2, 0, -2, 0), Array(0, 0, 3, 0, -3, 0), Array(0, 0, 3, 333333333, -4, 666666667), Array(0, 333333333, -4, 666666667, 3, 666666666), Array(0, 333333333, -3, 0, 3, 333333333), Array(0, 333333333, -2, 0, 2, 333333333), Array(0, 333333333, -1, 0, 1, 333333333), Array(0, 333333333, -1, 333333334, 0, 999999999), Array(0, 333333333, -1, 666666667, 0, 666666666), Array(0, 333333333, -1, 999999999, 0, 333333334), Array(0, 333333333, 0, 0, 0, 333333333), Array(0, 333333333, 0, 1, 0, 333333332), Array(0, 333333333, 0, 333333333, 0, 0), Array(0, 333333333, 0, 666666666, -1, 666666667), Array(0, 333333333, 1, 0, -1, 333333333), Array(0, 333333333, 2, 0, -2, 333333333), Array(0, 333333333, 3, 0, -3, 333333333), Array(0, 333333333, 3, 333333333, -3, 0), Array(1, 0, -4, 666666667, 4, 333333333), Array(1, 0, -3, 0, 4, 0), Array(1, 0, -2, 0, 3, 0), Array(1, 0, -1, 0, 2, 0), Array(1, 0, -1, 333333334, 1, 666666666), Array(1, 0, -1, 666666667, 1, 333333333), Array(1, 0, -1, 999999999, 1, 1), Array(1, 0, 0, 0, 1, 0), Array(1, 0, 0, 1, 0, 999999999), Array(1, 0, 0, 333333333, 0, 666666667), Array(1, 0, 0, 666666666, 0, 333333334), Array(1, 0, 1, 0, 0, 0), Array(1, 0, 2, 0, -1, 0), Array(1, 0, 3, 0, -2, 0), Array(1, 0, 3, 333333333, -3, 666666667), Array(2, 0, -4, 666666667, 5, 333333333), Array(2, 0, -3, 0, 5, 0), Array(2, 0, -2, 0, 4, 0), Array(2, 0, -1, 0, 3, 0), Array(2, 0, -1, 333333334, 2, 666666666), Array(2, 0, -1, 666666667, 2, 333333333), Array(2, 0, -1, 999999999, 2, 1), Array(2, 0, 0, 0, 2, 0), Array(2, 0, 0, 1, 1, 999999999), Array(2, 0, 0, 333333333, 1, 666666667), Array(2, 0, 0, 666666666, 1, 333333334), Array(2, 0, 1, 0, 1, 0), Array(2, 0, 2, 0, 0, 0), Array(2, 0, 3, 0, -1, 0), Array(2, 0, 3, 333333333, -2, 666666667), Array(3, 0, -4, 666666667, 6, 333333333), Array(3, 0, -3, 0, 6, 0), Array(3, 0, -2, 0, 5, 0), Array(3, 0, -1, 0, 4, 0), Array(3, 0, -1, 333333334, 3, 666666666), Array(3, 0, -1, 666666667, 3, 333333333), Array(3, 0, -1, 999999999, 3, 1), Array(3, 0, 0, 0, 3, 0), Array(3, 0, 0, 1, 2, 999999999), Array(3, 0, 0, 333333333, 2, 666666667), Array(3, 0, 0, 666666666, 2, 333333334), Array(3, 0, 1, 0, 2, 0), Array(3, 0, 2, 0, 1, 0), Array(3, 0, 3, 0, 0, 0), Array(3, 0, 3, 333333333, -1, 666666667), Array(3, 333333333, -4, 666666667, 6, 666666666), Array(3, 333333333, -3, 0, 6, 333333333), Array(3, 333333333, -2, 0, 5, 333333333), Array(3, 333333333, -1, 0, 4, 333333333), Array(3, 333333333, -1, 333333334, 3, 999999999), Array(3, 333333333, -1, 666666667, 3, 666666666), Array(3, 333333333, -1, 999999999, 3, 333333334), Array(3, 333333333, 0, 0, 3, 333333333), Array(3, 333333333, 0, 1, 3, 333333332), Array(3, 333333333, 0, 333333333, 3, 0), Array(3, 333333333, 0, 666666666, 2, 666666667), Array(3, 333333333, 1, 0, 2, 333333333), Array(3, 333333333, 2, 0, 1, 333333333), Array(3, 333333333, 3, 0, 0, 333333333), Array(3, 333333333, 3, 333333333, 0, 0), Array(Long.MaxValue, 0, Long.MaxValue, 0, 0, 0))
  }

  @Test(dataProvider = "Minus") def minus(seconds: Long, nanos: Int, otherSeconds: Long, otherNanos: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Duration = Duration.ofSeconds(seconds, nanos).minus(Duration.ofSeconds(otherSeconds, otherNanos))
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusOverflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MinValue)
    t.minus(Duration.ofSeconds(0, 1))
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusOverflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MaxValue, 999999999)
    t.minus(Duration.ofSeconds(-1, 999999999))
  }

  @Test def minus_longTemporalUnit_seconds(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.minus(1, SECONDS)
    assertEquals(0, t.getSeconds)
    assertEquals(0, t.getNano)
  }

  @Test def minus_longTemporalUnit_millis(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.minus(1, MILLIS)
    assertEquals(0, t.getSeconds)
    assertEquals(999000000, t.getNano)
  }

  @Test def minus_longTemporalUnit_micros(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.minus(1, MICROS)
    assertEquals(0, t.getSeconds)
    assertEquals(999999000, t.getNano)
  }

  @Test def minus_longTemporalUnit_nanos(): Unit = {
    var t: Duration = Duration.ofSeconds(1)
    t = t.minus(1, NANOS)
    assertEquals(0, t.getSeconds)
    assertEquals(999999999, t.getNano)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def minus_longTemporalUnit_null(): Unit = {
    val t: Duration = Duration.ofSeconds(1)
    t.minus(1, null.asInstanceOf[TemporalUnit])
  }

  @DataProvider(name = "MinusSeconds") private[bp] def provider_minusSeconds_long: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, -1, 0), Array(0, 0, -1, 1, 0), Array(0, 0, Long.MaxValue, -Long.MaxValue, 0), Array(0, 0, Long.MinValue + 1, Long.MaxValue, 0), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 0, 0), Array(1, 0, -1, 2, 0), Array(1, 0, Long.MaxValue - 1, -Long.MaxValue + 2, 0), Array(1, 0, Long.MinValue + 2, Long.MaxValue, 0), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 0, 1), Array(1, 1, -1, 2, 1), Array(1, 1, Long.MaxValue, -Long.MaxValue + 1, 1), Array(1, 1, Long.MinValue + 2, Long.MaxValue, 1), Array(-1, 1, 0, -1, 1), Array(-1, 1, 1, -2, 1), Array(-1, 1, -1, 0, 1), Array(-1, 1, Long.MaxValue, Long.MinValue, 1), Array(-1, 1, Long.MinValue + 1, Long.MaxValue - 1, 1))
  }

  @Test(dataProvider = "MinusSeconds") def minusSeconds_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.minusSeconds(amount)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusSeconds_long_overflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(1, 0)
    t.minusSeconds(Long.MinValue + 1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusSeconds_long_overflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(-2, 0)
    t.minusSeconds(Long.MaxValue)
  }

  @DataProvider(name = "MinusMillis") private[bp] def provider_minusMillis_long: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, -1, 999000000), Array(0, 0, 999, -1, 1000000), Array(0, 0, 1000, -1, 0), Array(0, 0, 1001, -2, 999000000), Array(0, 0, 1999, -2, 1000000), Array(0, 0, 2000, -2, 0), Array(0, 0, -1, 0, 1000000), Array(0, 0, -999, 0, 999000000), Array(0, 0, -1000, 1, 0), Array(0, 0, -1001, 1, 1000000), Array(0, 0, -1999, 1, 999000000), Array(0, 1, 0, 0, 1), Array(0, 1, 1, -1, 999000001), Array(0, 1, 998, -1, 2000001), Array(0, 1, 999, -1, 1000001), Array(0, 1, 1000, -1, 1), Array(0, 1, 1998, -2, 2000001), Array(0, 1, 1999, -2, 1000001), Array(0, 1, 2000, -2, 1), Array(0, 1, -1, 0, 1000001), Array(0, 1, -2, 0, 2000001), Array(0, 1, -1000, 1, 1), Array(0, 1, -1001, 1, 1000001), Array(0, 1000000, 0, 0, 1000000), Array(0, 1000000, 1, 0, 0), Array(0, 1000000, 998, -1, 3000000), Array(0, 1000000, 999, -1, 2000000), Array(0, 1000000, 1000, -1, 1000000), Array(0, 1000000, 1998, -2, 3000000), Array(0, 1000000, 1999, -2, 2000000), Array(0, 1000000, 2000, -2, 1000000), Array(0, 1000000, -1, 0, 2000000), Array(0, 1000000, -2, 0, 3000000), Array(0, 1000000, -999, 1, 0), Array(0, 1000000, -1000, 1, 1000000), Array(0, 1000000, -1001, 1, 2000000), Array(0, 1000000, -1002, 1, 3000000), Array(0, 999999999, 0, 0, 999999999), Array(0, 999999999, 1, 0, 998999999), Array(0, 999999999, 999, 0, 999999), Array(0, 999999999, 1000, -1, 999999999), Array(0, 999999999, 1001, -1, 998999999), Array(0, 999999999, -1, 1, 999999), Array(0, 999999999, -1000, 1, 999999999), Array(0, 999999999, -1001, 2, 999999))
  }

  @Test(dataProvider = "MinusMillis") def minusMillis_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.minusMillis(amount)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "MinusMillis") def minusMillis_long_oneMore(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds + 1, nanos)
    t = t.minusMillis(amount)
    assertEquals(t.getSeconds, expectedSeconds + 1)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "MinusMillis") def minusMillis_long_minusOneLess(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds - 1, nanos)
    t = t.minusMillis(amount)
    assertEquals(t.getSeconds, expectedSeconds - 1)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test def minusMillis_long_max(): Unit = {
    var t: Duration = Duration.ofSeconds(Long.MaxValue, 998999999)
    t = t.minusMillis(-1)
    assertEquals(t.getSeconds, Long.MaxValue)
    assertEquals(t.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusMillis_long_overflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MaxValue, 999000000)
    t.minusMillis(-1)
  }

  @Test def minusMillis_long_min(): Unit = {
    var t: Duration = Duration.ofSeconds(Long.MinValue, 1000000)
    t = t.minusMillis(1)
    assertEquals(t.getSeconds, Long.MinValue)
    assertEquals(t.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusMillis_long_overflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MinValue, 0)
    t.minusMillis(1)
  }

  @DataProvider(name = "MinusNanos") private[bp] def provider_minusNanos_long: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, -1, 999999999), Array(0, 0, 999999999, -1, 1), Array(0, 0, 1000000000, -1, 0), Array(0, 0, 1000000001, -2, 999999999), Array(0, 0, 1999999999, -2, 1), Array(0, 0, 2000000000, -2, 0), Array(0, 0, -1, 0, 1), Array(0, 0, -999999999, 0, 999999999), Array(0, 0, -1000000000, 1, 0), Array(0, 0, -1000000001, 1, 1), Array(0, 0, -1999999999, 1, 999999999), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 0, 999999999), Array(1, 0, 999999999, 0, 1), Array(1, 0, 1000000000, 0, 0), Array(1, 0, 1000000001, -1, 999999999), Array(1, 0, 1999999999, -1, 1), Array(1, 0, 2000000000, -1, 0), Array(1, 0, -1, 1, 1), Array(1, 0, -999999999, 1, 999999999), Array(1, 0, -1000000000, 2, 0), Array(1, 0, -1000000001, 2, 1), Array(1, 0, -1999999999, 2, 999999999), Array(-1, 0, 0, -1, 0), Array(-1, 0, 1, -2, 999999999), Array(-1, 0, 999999999, -2, 1), Array(-1, 0, 1000000000, -2, 0), Array(-1, 0, 1000000001, -3, 999999999), Array(-1, 0, 1999999999, -3, 1), Array(-1, 0, 2000000000, -3, 0), Array(-1, 0, -1, -1, 1), Array(-1, 0, -999999999, -1, 999999999), Array(-1, 0, -1000000000, 0, 0), Array(-1, 0, -1000000001, 0, 1), Array(-1, 0, -1999999999, 0, 999999999), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 1, 0), Array(1, 1, 999999998, 0, 3), Array(1, 1, 999999999, 0, 2), Array(1, 1, 1000000000, 0, 1), Array(1, 1, 1999999998, -1, 3), Array(1, 1, 1999999999, -1, 2), Array(1, 1, 2000000000, -1, 1), Array(1, 1, -1, 1, 2), Array(1, 1, -2, 1, 3), Array(1, 1, -1000000000, 2, 1), Array(1, 1, -1000000001, 2, 2), Array(1, 1, -1000000002, 2, 3), Array(1, 1, -2000000000, 3, 1), Array(1, 999999999, 0, 1, 999999999), Array(1, 999999999, 1, 1, 999999998), Array(1, 999999999, 999999999, 1, 0), Array(1, 999999999, 1000000000, 0, 999999999), Array(1, 999999999, 1000000001, 0, 999999998), Array(1, 999999999, -1, 2, 0), Array(1, 999999999, -1000000000, 2, 999999999), Array(1, 999999999, -1000000001, 3, 0), Array(1, 999999999, -1999999999, 3, 999999998), Array(1, 999999999, -2000000000, 3, 999999999), Array(Long.MaxValue, 0, -999999999, Long.MaxValue, 999999999), Array(Long.MaxValue - 1, 0, -1999999999, Long.MaxValue, 999999999), Array(Long.MinValue, 1, 1, Long.MinValue, 0), Array(Long.MinValue + 1, 1, 1000000001, Long.MinValue, 0))
  }

  @Test(dataProvider = "MinusNanos") def minusNanos_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.minusNanos(amount)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusNanos_long_overflowTooBig(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MaxValue, 999999999)
    t.minusNanos(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusNanos_long_overflowTooSmall(): Unit = {
    val t: Duration = Duration.ofSeconds(Long.MinValue, 0)
    t.minusNanos(1)
  }

  @DataProvider(name = "MultipliedBy") private[bp] def provider_multipliedBy: Array[Array[Any]] = {
    Array[Array[Any]](Array(-4, 666666667, -3, 9, 999999999), Array(-4, 666666667, -2, 6, 666666666), Array(-4, 666666667, -1, 3, 333333333), Array(-4, 666666667, 0, 0, 0), Array(-4, 666666667, 1, -4, 666666667), Array(-4, 666666667, 2, -7, 333333334), Array(-4, 666666667, 3, -10, 1), Array(-3, 0, -3, 9, 0), Array(-3, 0, -2, 6, 0), Array(-3, 0, -1, 3, 0), Array(-3, 0, 0, 0, 0), Array(-3, 0, 1, -3, 0), Array(-3, 0, 2, -6, 0), Array(-3, 0, 3, -9, 0), Array(-2, 0, -3, 6, 0), Array(-2, 0, -2, 4, 0), Array(-2, 0, -1, 2, 0), Array(-2, 0, 0, 0, 0), Array(-2, 0, 1, -2, 0), Array(-2, 0, 2, -4, 0), Array(-2, 0, 3, -6, 0), Array(-1, 0, -3, 3, 0), Array(-1, 0, -2, 2, 0), Array(-1, 0, -1, 1, 0), Array(-1, 0, 0, 0, 0), Array(-1, 0, 1, -1, 0), Array(-1, 0, 2, -2, 0), Array(-1, 0, 3, -3, 0), Array(-1, 500000000, -3, 1, 500000000), Array(-1, 500000000, -2, 1, 0), Array(-1, 500000000, -1, 0, 500000000), Array(-1, 500000000, 0, 0, 0), Array(-1, 500000000, 1, -1, 500000000), Array(-1, 500000000, 2, -1, 0), Array(-1, 500000000, 3, -2, 500000000), Array(0, 0, -3, 0, 0), Array(0, 0, -2, 0, 0), Array(0, 0, -1, 0, 0), Array(0, 0, 0, 0, 0), Array(0, 0, 1, 0, 0), Array(0, 0, 2, 0, 0), Array(0, 0, 3, 0, 0), Array(0, 500000000, -3, -2, 500000000), Array(0, 500000000, -2, -1, 0), Array(0, 500000000, -1, -1, 500000000), Array(0, 500000000, 0, 0, 0), Array(0, 500000000, 1, 0, 500000000), Array(0, 500000000, 2, 1, 0), Array(0, 500000000, 3, 1, 500000000), Array(1, 0, -3, -3, 0), Array(1, 0, -2, -2, 0), Array(1, 0, -1, -1, 0), Array(1, 0, 0, 0, 0), Array(1, 0, 1, 1, 0), Array(1, 0, 2, 2, 0), Array(1, 0, 3, 3, 0), Array(2, 0, -3, -6, 0), Array(2, 0, -2, -4, 0), Array(2, 0, -1, -2, 0), Array(2, 0, 0, 0, 0), Array(2, 0, 1, 2, 0), Array(2, 0, 2, 4, 0), Array(2, 0, 3, 6, 0), Array(3, 0, -3, -9, 0), Array(3, 0, -2, -6, 0), Array(3, 0, -1, -3, 0), Array(3, 0, 0, 0, 0), Array(3, 0, 1, 3, 0), Array(3, 0, 2, 6, 0), Array(3, 0, 3, 9, 0), Array(3, 333333333, -3, -10, 1), Array(3, 333333333, -2, -7, 333333334), Array(3, 333333333, -1, -4, 666666667), Array(3, 333333333, 0, 0, 0), Array(3, 333333333, 1, 3, 333333333), Array(3, 333333333, 2, 6, 666666666), Array(3, 333333333, 3, 9, 999999999))
  }

  @Test(dataProvider = "MultipliedBy") def multipliedBy(seconds: Long, nanos: Int, multiplicand: Int, expectedSeconds: Long, expectedNanos: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.multipliedBy(multiplicand)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanos)
  }

  @Test def multipliedBy_max(): Unit = {
    val test: Duration = Duration.ofSeconds(1)
    assertEquals(test.multipliedBy(Long.MaxValue), Duration.ofSeconds(Long.MaxValue))
  }

  @Test def multipliedBy_min(): Unit = {
    val test: Duration = Duration.ofSeconds(1)
    assertEquals(test.multipliedBy(Long.MinValue), Duration.ofSeconds(Long.MinValue))
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def multipliedBy_tooBig(): Unit = {
    val test: Duration = Duration.ofSeconds(1, 1)
    test.multipliedBy(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def multipliedBy_tooBig_negative(): Unit = {
    val test: Duration = Duration.ofSeconds(1, 1)
    test.multipliedBy(Long.MinValue)
  }

  @DataProvider(name = "DividedBy") private[bp] def provider_dividedBy: Array[Array[Any]] = {
    Array[Array[Any]](Array(-4, 666666667, -3, 1, 111111111), Array(-4, 666666667, -2, 1, 666666666), Array(-4, 666666667, -1, 3, 333333333), Array(-4, 666666667, 1, -4, 666666667), Array(-4, 666666667, 2, -2, 333333334), Array(-4, 666666667, 3, -2, 888888889), Array(-3, 0, -3, 1, 0), Array(-3, 0, -2, 1, 500000000), Array(-3, 0, -1, 3, 0), Array(-3, 0, 1, -3, 0), Array(-3, 0, 2, -2, 500000000), Array(-3, 0, 3, -1, 0), Array(-2, 0, -3, 0, 666666666), Array(-2, 0, -2, 1, 0), Array(-2, 0, -1, 2, 0), Array(-2, 0, 1, -2, 0), Array(-2, 0, 2, -1, 0), Array(-2, 0, 3, -1, 333333334), Array(-1, 0, -3, 0, 333333333), Array(-1, 0, -2, 0, 500000000), Array(-1, 0, -1, 1, 0), Array(-1, 0, 1, -1, 0), Array(-1, 0, 2, -1, 500000000), Array(-1, 0, 3, -1, 666666667), Array(-1, 500000000, -3, 0, 166666666), Array(-1, 500000000, -2, 0, 250000000), Array(-1, 500000000, -1, 0, 500000000), Array(-1, 500000000, 1, -1, 500000000), Array(-1, 500000000, 2, -1, 750000000), Array(-1, 500000000, 3, -1, 833333334), Array(0, 0, -3, 0, 0), Array(0, 0, -2, 0, 0), Array(0, 0, -1, 0, 0), Array(0, 0, 1, 0, 0), Array(0, 0, 2, 0, 0), Array(0, 0, 3, 0, 0), Array(0, 500000000, -3, -1, 833333334), Array(0, 500000000, -2, -1, 750000000), Array(0, 500000000, -1, -1, 500000000), Array(0, 500000000, 1, 0, 500000000), Array(0, 500000000, 2, 0, 250000000), Array(0, 500000000, 3, 0, 166666666), Array(1, 0, -3, -1, 666666667), Array(1, 0, -2, -1, 500000000), Array(1, 0, -1, -1, 0), Array(1, 0, 1, 1, 0), Array(1, 0, 2, 0, 500000000), Array(1, 0, 3, 0, 333333333), Array(2, 0, -3, -1, 333333334), Array(2, 0, -2, -1, 0), Array(2, 0, -1, -2, 0), Array(2, 0, 1, 2, 0), Array(2, 0, 2, 1, 0), Array(2, 0, 3, 0, 666666666), Array(3, 0, -3, -1, 0), Array(3, 0, -2, -2, 500000000), Array(3, 0, -1, -3, 0), Array(3, 0, 1, 3, 0), Array(3, 0, 2, 1, 500000000), Array(3, 0, 3, 1, 0), Array(3, 333333333, -3, -2, 888888889), Array(3, 333333333, -2, -2, 333333334), Array(3, 333333333, -1, -4, 666666667), Array(3, 333333333, 1, 3, 333333333), Array(3, 333333333, 2, 1, 666666666), Array(3, 333333333, 3, 1, 111111111))
  }

  @Test(dataProvider = "DividedBy") def dividedBy(seconds: Long, nanos: Int, divisor: Int, expectedSeconds: Long, expectedNanos: Int): Unit = {
    var t: Duration = Duration.ofSeconds(seconds, nanos)
    t = t.dividedBy(divisor)
    assertEquals(t.getSeconds, expectedSeconds)
    assertEquals(t.getNano, expectedNanos)
  }

  @Test(dataProvider = "DividedBy", expectedExceptions = Array(classOf[ArithmeticException])) def dividedByZero(seconds: Long, nanos: Int, divisor: Int, expectedSeconds: Long, expectedNanos: Int): Unit = {
    val t: Duration = Duration.ofSeconds(seconds, nanos)
    t.dividedBy(0)
    fail(t + " divided by zero did not throw ArithmeticException")
  }

  @Test def dividedBy_max(): Unit = {
    val test: Duration = Duration.ofSeconds(Long.MaxValue)
    assertEquals(test.dividedBy(Long.MaxValue), Duration.ofSeconds(1))
  }

  @Test def test_negated(): Unit = {
    assertEquals(Duration.ofSeconds(0).negated, Duration.ofSeconds(0))
    assertEquals(Duration.ofSeconds(12).negated, Duration.ofSeconds(-12))
    assertEquals(Duration.ofSeconds(-12).negated, Duration.ofSeconds(12))
    assertEquals(Duration.ofSeconds(12, 20).negated, Duration.ofSeconds(-12, -20))
    assertEquals(Duration.ofSeconds(12, -20).negated, Duration.ofSeconds(-12, 20))
    assertEquals(Duration.ofSeconds(-12, -20).negated, Duration.ofSeconds(12, 20))
    assertEquals(Duration.ofSeconds(-12, 20).negated, Duration.ofSeconds(12, -20))
    assertEquals(Duration.ofSeconds(Long.MaxValue).negated, Duration.ofSeconds(-Long.MaxValue))
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_negated_overflow(): Unit = {
    Duration.ofSeconds(Long.MinValue).negated
  }

  @Test def test_abs(): Unit = {
    assertEquals(Duration.ofSeconds(0).abs, Duration.ofSeconds(0))
    assertEquals(Duration.ofSeconds(12).abs, Duration.ofSeconds(12))
    assertEquals(Duration.ofSeconds(-12).abs, Duration.ofSeconds(12))
    assertEquals(Duration.ofSeconds(12, 20).abs, Duration.ofSeconds(12, 20))
    assertEquals(Duration.ofSeconds(12, -20).abs, Duration.ofSeconds(12, -20))
    assertEquals(Duration.ofSeconds(-12, -20).abs, Duration.ofSeconds(12, 20))
    assertEquals(Duration.ofSeconds(-12, 20).abs, Duration.ofSeconds(12, -20))
    assertEquals(Duration.ofSeconds(Long.MaxValue).abs, Duration.ofSeconds(Long.MaxValue))
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_abs_overflow(): Unit = {
    Duration.ofSeconds(Long.MinValue).abs
  }

  @Test def test_toNanos(): Unit = {
    val test: Duration = Duration.ofSeconds(321, 123456789)
    assertEquals(test.toNanos, 321123456789L)
  }

  @Test def test_toNanos_max(): Unit = {
    val test: Duration = Duration.ofSeconds(0, Long.MaxValue)
    assertEquals(test.toNanos, Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_toNanos_tooBig(): Unit = {
    val test: Duration = Duration.ofSeconds(0, Long.MaxValue).plusNanos(1)
    test.toNanos
  }

  @Test def test_toMillis(): Unit = {
    val test: Duration = Duration.ofSeconds(321, 123456789)
    assertEquals(test.toMillis, 321000 + 123)
  }

  @Test def test_toMillis_max(): Unit = {
    val test: Duration = Duration.ofSeconds(Long.MaxValue / 1000, (Long.MaxValue % 1000) * 1000000)
    assertEquals(test.toMillis, Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_toMillis_tooBig(): Unit = {
    val test: Duration = Duration.ofSeconds(Long.MaxValue / 1000, ((Long.MaxValue % 1000) + 1) * 1000000)
    test.toMillis
  }

  @Test def test_comparisons(): Unit = {
    doTest_comparisons_Duration(Duration.ofSeconds(-2L, 0), Duration.ofSeconds(-2L, 999999998), Duration.ofSeconds(-2L, 999999999), Duration.ofSeconds(-1L, 0), Duration.ofSeconds(-1L, 1), Duration.ofSeconds(-1L, 999999998), Duration.ofSeconds(-1L, 999999999), Duration.ofSeconds(0L, 0), Duration.ofSeconds(0L, 1), Duration.ofSeconds(0L, 2), Duration.ofSeconds(0L, 999999999), Duration.ofSeconds(1L, 0), Duration.ofSeconds(2L, 0))
  }

  private def doTest_comparisons_Duration(durations: Duration*): Unit = {
    var i: Int = 0
    while (i < durations.length) {
      val a: Duration = durations(i)
      var j: Int = 0
      while (j < durations.length) {
        val b: Duration = durations(j)
        if (i < j) {
          assertEquals(a.compareTo(b) < 0, true, a + " <=> " + b)
          assertEquals(a == b, false, a + " <=> " + b)
        } else if (i > j) {
          assertEquals(a.compareTo(b) > 0, true, a + " <=> " + b)
          assertEquals(a == b, false, a + " <=> " + b)
        } else {
          assertEquals(a.compareTo(b), 0, a + " <=> " + b)
          assertEquals(a == b, true, a + " <=> " + b)
        }
        j += 1
      }
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_ObjectNull(): Unit = {
    val a: Duration = Duration.ofSeconds(0L, 0)
    a.compareTo(null)
  }

  @Test def test_equals(): Unit = {
    val test5a: Duration = Duration.ofSeconds(5L, 20)
    val test5b: Duration = Duration.ofSeconds(5L, 20)
    val test5n: Duration = Duration.ofSeconds(5L, 30)
    val test6: Duration = Duration.ofSeconds(6L, 20)
    assertEquals(test5a == test5a, true)
    assertEquals(test5a == test5b, true)
    assertEquals(test5a == test5n, false)
    assertEquals(test5a == test6, false)
    assertEquals(test5b == test5a, true)
    assertEquals(test5b == test5b, true)
    assertEquals(test5b == test5n, false)
    assertEquals(test5b == test6, false)
    assertEquals(test5n == test5a, false)
    assertEquals(test5n == test5b, false)
    assertEquals(test5n == test5n, true)
    assertEquals(test5n == test6, false)
    assertEquals(test6 == test5a, false)
    assertEquals(test6 == test5b, false)
    assertEquals(test6 == test5n, false)
    assertEquals(test6 == test6, true)
  }

  @Test def test_equals_null(): Unit = {
    val test5: Duration = Duration.ofSeconds(5L, 20)
    assertEquals(test5 == null, false)
  }

  @Test def test_equals_otherClass(): Unit = {
    val test5: Duration = Duration.ofSeconds(5L, 20)
    assertEquals(test5 == "", false)
  }

  @Test def test_hashCode(): Unit = {
    val test5a: Duration = Duration.ofSeconds(5L, 20)
    val test5b: Duration = Duration.ofSeconds(5L, 20)
    val test5n: Duration = Duration.ofSeconds(5L, 30)
    val test6: Duration = Duration.ofSeconds(6L, 20)
    assertEquals(test5a.hashCode == test5a.hashCode, true)
    assertEquals(test5a.hashCode == test5b.hashCode, true)
    assertEquals(test5b.hashCode == test5b.hashCode, true)
    assertEquals(test5a.hashCode == test5n.hashCode, false)
    assertEquals(test5a.hashCode == test6.hashCode, false)
  }

  @DataProvider(name = "ToString") private[bp] def provider_toString: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, "PT0S"), Array(0, 1, "PT0.000000001S"), Array(0, 10, "PT0.00000001S"), Array(0, 100, "PT0.0000001S"), Array(0, 1000, "PT0.000001S"), Array(0, 10000, "PT0.00001S"), Array(0, 100000, "PT0.0001S"), Array(0, 1000000, "PT0.001S"), Array(0, 10000000, "PT0.01S"), Array(0, 100000000, "PT0.1S"), Array(0, 120000000, "PT0.12S"), Array(0, 123000000, "PT0.123S"), Array(0, 123400000, "PT0.1234S"), Array(0, 123450000, "PT0.12345S"), Array(0, 123456000, "PT0.123456S"), Array(0, 123456700, "PT0.1234567S"), Array(0, 123456780, "PT0.12345678S"), Array(0, 123456789, "PT0.123456789S"), Array(1, 0, "PT1S"), Array(-1, 0, "PT-1S"), Array(-1, 1000, "PT-0.999999S"), Array(-1, 900000000, "PT-0.1S"), Array(60, 0, "PT1M"), Array(3600, 0, "PT1H"), Array(7261, 0, "PT2H1M1S"))
  }

  @Test(dataProvider = "ToString") def test_toString(seconds: Long, nanos: Int, expected: String): Unit = {
    val t: Duration = Duration.ofSeconds(seconds, nanos)
    assertEquals(t.toString, expected)
  }
}
