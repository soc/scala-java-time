/*
 * Copyright (c) 2007-present Stephen Colebourne & Michael Nascimento Santos
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
import org.testng.Assert.assertSame
import java.io.IOException
import org.testng.annotations.Test

/** Test tick clock. */
object TestClock_Tick {
  private val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
  private val PARIS: ZoneId = ZoneId.of("Europe/Paris")
  private val AMOUNT: Duration = Duration.ofSeconds(2)
  private val ZDT: ZonedDateTime = LocalDateTime.of(2008, 6, 30, 11, 30, 10, 500).atZone(ZoneOffset.ofHours(2))
  private val INSTANT: Instant = ZDT.toInstant
}

@Test class TestClock_Tick {
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_isSerializable(): Unit = {
    AbstractTest.assertSerializable(Clock.tickSeconds(TestClock_Tick.PARIS))
    AbstractTest.assertSerializable(Clock.tickMinutes(TestClock_Tick.MOSCOW))
    AbstractTest.assertSerializable(Clock.tick(Clock.fixed(TestClock_Tick.INSTANT, TestClock_Tick.PARIS), TestClock_Tick.AMOUNT))
  }

  def test_tick_ClockDuration_250millis(): Unit = {
    {
      var i: Int = 0
      while (i < 1000) {
        {
          val test: Clock = Clock.tick(Clock.fixed(TestClock_Tick.ZDT.withNano(i * 1000000).toInstant, TestClock_Tick.PARIS), Duration.ofMillis(250))
          assertEquals(test.instant, TestClock_Tick.ZDT.withNano((i / 250) * 250000000).toInstant)
          assertEquals(test.getZone, TestClock_Tick.PARIS)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  def test_tick_ClockDuration_250micros(): Unit = {
    {
      var i: Int = 0
      while (i < 1000) {
        {
          val test: Clock = Clock.tick(Clock.fixed(TestClock_Tick.ZDT.withNano(i * 1000).toInstant, TestClock_Tick.PARIS), Duration.ofNanos(250000))
          assertEquals(test.instant, TestClock_Tick.ZDT.withNano((i / 250) * 250000).toInstant)
          assertEquals(test.getZone, TestClock_Tick.PARIS)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  def test_tick_ClockDuration_20nanos(): Unit = {
    {
      var i: Int = 0
      while (i < 1000) {
        {
          val test: Clock = Clock.tick(Clock.fixed(TestClock_Tick.ZDT.withNano(i).toInstant, TestClock_Tick.PARIS), Duration.ofNanos(20))
          assertEquals(test.instant, TestClock_Tick.ZDT.withNano((i / 20) * 20).toInstant)
          assertEquals(test.getZone, TestClock_Tick.PARIS)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  def test_tick_ClockDuration_zeroDuration(): Unit = {
    val underlying: Clock = Clock.system(TestClock_Tick.PARIS)
    val test: Clock = Clock.tick(underlying, Duration.ZERO)
    assertSame(test, underlying)
  }

  def test_tick_ClockDuration_1nsDuration(): Unit = {
    val underlying: Clock = Clock.system(TestClock_Tick.PARIS)
    val test: Clock = Clock.tick(underlying, Duration.ofNanos(1))
    assertSame(test, underlying)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_tick_ClockDuration_maxDuration(): Unit = {
    Clock.tick(Clock.systemUTC, Duration.ofSeconds(Long.MaxValue))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_tick_ClockDuration_subMilliNotDivisible_123ns(): Unit = {
    Clock.tick(Clock.systemUTC, Duration.ofSeconds(0, 123))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_tick_ClockDuration_subMilliNotDivisible_999ns(): Unit = {
    Clock.tick(Clock.systemUTC, Duration.ofSeconds(0, 999))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_tick_ClockDuration_subMilliNotDivisible_999999999ns(): Unit = {
    Clock.tick(Clock.systemUTC, Duration.ofSeconds(0, 999999999))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_tick_ClockDuration_negative1ns(): Unit = {
    Clock.tick(Clock.systemUTC, Duration.ofSeconds(0, -1))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_tick_ClockDuration_negative1s(): Unit = {
    Clock.tick(Clock.systemUTC, Duration.ofSeconds(-1))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_tick_ClockDuration_nullClock(): Unit = {
    Clock.tick(null, Duration.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_tick_ClockDuration_nullDuration(): Unit = {
    Clock.tick(Clock.systemUTC, null)
  }

  @throws(classOf[Exception])
  def test_tickSeconds_ZoneId(): Unit = {
    val test: Clock = Clock.tickSeconds(TestClock_Tick.PARIS)
    assertEquals(test.getZone, TestClock_Tick.PARIS)
    assertEquals(test.instant.getNano, 0)
    Thread.sleep(100)
    assertEquals(test.instant.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_tickSeconds_ZoneId_nullZoneId(): Unit = {
    Clock.tickSeconds(null)
  }

  def test_tickMinutes_ZoneId(): Unit = {
    val test: Clock = Clock.tickMinutes(TestClock_Tick.PARIS)
    assertEquals(test.getZone, TestClock_Tick.PARIS)
    val instant: Instant = test.instant
    assertEquals(instant.getEpochSecond % 60, 0)
    assertEquals(instant.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_tickMinutes_ZoneId_nullZoneId(): Unit = {
    Clock.tickMinutes(null)
  }

  def test_withZone(): Unit = {
    val test: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500))
    val changed: Clock = test.withZone(TestClock_Tick.MOSCOW)
    assertEquals(test.getZone, TestClock_Tick.PARIS)
    assertEquals(changed.getZone, TestClock_Tick.MOSCOW)
  }

  def test_withZone_same(): Unit = {
    val test: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500))
    val changed: Clock = test.withZone(TestClock_Tick.PARIS)
    assertSame(test, changed)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_withZone_null(): Unit = {
    Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500)).withZone(null)
  }

  def test__equals(): Unit = {
    val a: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500))
    val b: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500))
    assertEquals(a == a, true)
    assertEquals(a == b, true)
    assertEquals(b == a, true)
    assertEquals(b == b, true)
    val c: Clock = Clock.tick(Clock.system(TestClock_Tick.MOSCOW), Duration.ofMillis(500))
    assertEquals(a == c, false)
    val d: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(499))
    assertEquals(a == d, false)
    assertEquals(a == null, false)
    assertEquals(a == "other type", false)
    assertEquals(a == Clock.systemUTC, false)
  }

  def test_hashCode(): Unit = {
    val a: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500))
    val b: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(500))
    assertEquals(a.hashCode, a.hashCode)
    assertEquals(a.hashCode, b.hashCode)
    val c: Clock = Clock.tick(Clock.system(TestClock_Tick.MOSCOW), Duration.ofMillis(500))
    assertEquals(a.hashCode == c.hashCode, false)
    val d: Clock = Clock.tick(Clock.system(TestClock_Tick.PARIS), Duration.ofMillis(499))
    assertEquals(a.hashCode == d.hashCode, false)
  }

  def test_toString(): Unit = {
    val test: Clock = Clock.tick(Clock.systemUTC, Duration.ofMillis(500))
    assertEquals(test.toString, "TickClock[SystemClock[Z],PT0.5S]")
  }
}
