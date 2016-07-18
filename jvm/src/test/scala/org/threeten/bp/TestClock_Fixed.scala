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

/** Test fixed clock. */
object TestClock_Fixed {
  private val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
  private val PARIS: ZoneId = ZoneId.of("Europe/Paris")
  private val INSTANT: Instant = LocalDateTime.of(2008, 6, 30, 11, 30, 10, 500).atZone(ZoneOffset.ofHours(2)).toInstant
}

@Test class TestClock_Fixed {
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_isSerializable(): Unit = {
    AbstractTest.assertSerializable(Clock.fixed(TestClock_Fixed.INSTANT, ZoneOffset.UTC))
    AbstractTest.assertSerializable(Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS))
  }

  def test_fixed_InstantZoneId(): Unit = {
    val test: Clock = Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS)
    assertEquals(test.instant, TestClock_Fixed.INSTANT)
    assertEquals(test.getZone, TestClock_Fixed.PARIS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_fixed_InstantZoneId_nullInstant(): Unit = {
    Clock.fixed(null, TestClock_Fixed.PARIS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_fixed_InstantZoneId_nullZoneId(): Unit = {
    Clock.fixed(TestClock_Fixed.INSTANT, null)
  }

  def test_withZone(): Unit = {
    val test: Clock = Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS)
    val changed: Clock = test.withZone(TestClock_Fixed.MOSCOW)
    assertEquals(test.getZone, TestClock_Fixed.PARIS)
    assertEquals(changed.getZone, TestClock_Fixed.MOSCOW)
  }

  def test_withZone_same(): Unit = {
    val test: Clock = Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS)
    val changed: Clock = test.withZone(TestClock_Fixed.PARIS)
    assertSame(test, changed)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_withZone_null(): Unit = {
    Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS).withZone(null)
  }

  def test_equals(): Unit = {
    val a: Clock = Clock.fixed(TestClock_Fixed.INSTANT, ZoneOffset.UTC)
    val b: Clock = Clock.fixed(TestClock_Fixed.INSTANT, ZoneOffset.UTC)
    assertEquals(a == a, true)
    assertEquals(a == b, true)
    assertEquals(b == a, true)
    assertEquals(b == b, true)
    val c: Clock = Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS)
    assertEquals(a == c, false)
    val d: Clock = Clock.fixed(TestClock_Fixed.INSTANT.minusNanos(1), ZoneOffset.UTC)
    assertEquals(a == d, false)
    assertEquals(a == null, false)
    assertEquals(a == "other type", false)
    assertEquals(a == Clock.systemUTC, false)
  }

  def test_hashCode(): Unit = {
    val a: Clock = Clock.fixed(TestClock_Fixed.INSTANT, ZoneOffset.UTC)
    val b: Clock = Clock.fixed(TestClock_Fixed.INSTANT, ZoneOffset.UTC)
    assertEquals(a.hashCode, a.hashCode)
    assertEquals(a.hashCode, b.hashCode)
    val c: Clock = Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS)
    assertEquals(a.hashCode == c.hashCode, false)
    val d: Clock = Clock.fixed(TestClock_Fixed.INSTANT.minusNanos(1), ZoneOffset.UTC)
    assertEquals(a.hashCode == d.hashCode, false)
  }

  def test_toString(): Unit = {
    val test: Clock = Clock.fixed(TestClock_Fixed.INSTANT, TestClock_Fixed.PARIS)
    assertEquals(test.toString, "FixedClock[2008-06-30T09:30:10.000000500Z,Europe/Paris]")
  }
}
