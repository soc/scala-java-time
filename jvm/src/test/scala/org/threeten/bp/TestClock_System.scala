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
import org.testng.Assert.fail
import java.io.IOException
import org.testng.annotations.Test

/** Test system clock. */
object TestClock_System {
  private val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
  private val PARIS: ZoneId = ZoneId.of("Europe/Paris")
}

@Test class TestClock_System {
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_isSerializable(): Unit = {
    AbstractTest.assertSerializable(Clock.systemUTC)
    AbstractTest.assertSerializable(Clock.systemDefaultZone)
    AbstractTest.assertSerializable(Clock.system(TestClock_System.PARIS))
  }

  def test_instant(): Unit = {
    val system: Clock = Clock.systemUTC
    assertEquals(system.getZone, ZoneOffset.UTC)

    {
      var i: Int = 0
      while (i < 10000) {
        {
          val instant: Instant = system.instant
          val systemMillis: Long = System.currentTimeMillis
          if (systemMillis - instant.toEpochMilli < 10) {
            return
          }
        }
        {
          i += 1
          i - 1
        }
      }
    }
    fail()
  }

  def test_millis(): Unit = {
    val system: Clock = Clock.systemUTC
    assertEquals(system.getZone, ZoneOffset.UTC)

    {
      var i: Int = 0
      while (i < 10000) {
        {
          val instant: Long = system.millis
          val systemMillis: Long = System.currentTimeMillis
          if (systemMillis - instant < 10) {
            return
          }
        }
        {
          i += 1
          i - 1
        }
      }
    }
    fail()
  }

  def test_systemUTC(): Unit = {
    val test: Clock = Clock.systemUTC
    assertEquals(test.getZone, ZoneOffset.UTC)
    assertEquals(test, Clock.system(ZoneOffset.UTC))
  }

  def test_systemDefaultZone(): Unit = {
    val test: Clock = Clock.systemDefaultZone
    assertEquals(test.getZone, ZoneId.systemDefault)
    assertEquals(test, Clock.system(ZoneId.systemDefault))
  }

  def test_system_ZoneId(): Unit = {
    val test: Clock = Clock.system(TestClock_System.PARIS)
    assertEquals(test.getZone, TestClock_System.PARIS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_zoneId_nullZoneId(): Unit = {
    Clock.system(null)
  }

  def test_withZone(): Unit = {
    val test: Clock = Clock.system(TestClock_System.PARIS)
    val changed: Clock = test.withZone(TestClock_System.MOSCOW)
    assertEquals(test.getZone, TestClock_System.PARIS)
    assertEquals(changed.getZone, TestClock_System.MOSCOW)
  }

  def test_withZone_same(): Unit = {
    val test: Clock = Clock.system(TestClock_System.PARIS)
    val changed: Clock = test.withZone(TestClock_System.PARIS)
    assertSame(test, changed)
  }

  def test_withZone_fromUTC(): Unit = {
    val test: Clock = Clock.systemUTC
    val changed: Clock = test.withZone(TestClock_System.PARIS)
    assertEquals(changed.getZone, TestClock_System.PARIS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_withZone_null(): Unit = {
    Clock.systemUTC.withZone(null)
  }

  def test_equals(): Unit = {
    val a: Clock = Clock.systemUTC
    val b: Clock = Clock.systemUTC
    assertEquals(a == a, true)
    assertEquals(a == b, true)
    assertEquals(b == a, true)
    assertEquals(b == b, true)
    val c: Clock = Clock.system(TestClock_System.PARIS)
    val d: Clock = Clock.system(TestClock_System.PARIS)
    assertEquals(c == c, true)
    assertEquals(c == d, true)
    assertEquals(d == c, true)
    assertEquals(d == d, true)
    assertEquals(a == c, false)
    assertEquals(c == a, false)
    assertEquals(a == null, false)
    assertEquals(a == "other type", false)
    assertEquals(a == Clock.fixed(Instant.now, ZoneOffset.UTC), false)
  }

  def test_hashCode(): Unit = {
    val a: Clock = Clock.system(ZoneOffset.UTC)
    val b: Clock = Clock.system(ZoneOffset.UTC)
    assertEquals(a.hashCode, a.hashCode)
    assertEquals(a.hashCode, b.hashCode)
    val c: Clock = Clock.system(TestClock_System.PARIS)
    assertEquals(a.hashCode == c.hashCode, false)
  }

  def test_toString(): Unit = {
    val test: Clock = Clock.system(TestClock_System.PARIS)
    assertEquals(test.toString, "SystemClock[Europe/Paris]")
  }
}
