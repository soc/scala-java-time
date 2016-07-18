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
import org.testng.annotations.Test

/** Test Clock. */
object TestClock {

  private[bp] class MockInstantClock private[bp](override val millis: Long, private[bp] val zone: ZoneId) extends Clock {

    def instant: Instant = Instant.ofEpochMilli(millis)

    def getZone: ZoneId = zone

    def withZone(timeZone: ZoneId): Clock = new TestClock.MockInstantClock(millis, timeZone)

    override def equals(obj: Any): Boolean = false

    override def hashCode: Int = 0

    override def toString: String = "Mock"
  }

  private val INSTANT: Instant = Instant.ofEpochSecond(1873687, 357000000)
  private val ZONE: ZoneId = ZoneId.of("Europe/Paris")
  private val MOCK_INSTANT: Clock = new TestClock.MockInstantClock(INSTANT.toEpochMilli, ZONE)
}

@Test class TestClock {
  @Test def test_mockInstantClock_get(): Unit = {
    assertEquals(TestClock.MOCK_INSTANT.instant, TestClock.INSTANT)
    assertEquals(TestClock.MOCK_INSTANT.millis, TestClock.INSTANT.toEpochMilli)
    assertEquals(TestClock.MOCK_INSTANT.getZone, TestClock.ZONE)
  }

  @Test def test_mockInstantClock_withZone(): Unit = {
    val london: ZoneId = ZoneId.of("Europe/London")
    val changed: Clock = TestClock.MOCK_INSTANT.withZone(london)
    assertEquals(TestClock.MOCK_INSTANT.instant, TestClock.INSTANT)
    assertEquals(TestClock.MOCK_INSTANT.millis, TestClock.INSTANT.toEpochMilli)
    assertEquals(changed.getZone, london)
  }
}
