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
package org.threeten.bp.zone

import org.testng.Assert.assertEquals
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition

/** Test ZoneRules for fixed offset time-zones. */
object TestFixedZoneRules {
  private val OFFSET_PONE: ZoneOffset = ZoneOffset.ofHours(1)
  private val OFFSET_PTWO: ZoneOffset = ZoneOffset.ofHours(2)
  private val OFFSET_M18: ZoneOffset = ZoneOffset.ofHours(-18)
  private val LDT: LocalDateTime = LocalDateTime.of(2010, 12, 3, 11, 30)
  private val INSTANT: Instant = LDT.toInstant(OFFSET_PONE)
}

@Test class TestFixedZoneRules {
  private def make(offset: ZoneOffset): ZoneRules = {
    offset.getRules
  }

  @DataProvider(name = "rules") private[zone] def data_rules: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(make(TestFixedZoneRules.OFFSET_PONE), TestFixedZoneRules.OFFSET_PONE), Array(make(TestFixedZoneRules.OFFSET_PTWO), TestFixedZoneRules.OFFSET_PTWO), Array(make(TestFixedZoneRules.OFFSET_M18), TestFixedZoneRules.OFFSET_M18))
  }

  @Test(dataProvider = "rules")
  @throws(classOf[Exception])
  def test_serialization(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val out: ObjectOutputStream = new ObjectOutputStream(baos)
    out.writeObject(test)
    baos.close()
    val bytes: Array[Byte] = baos.toByteArray
    val bais: ByteArrayInputStream = new ByteArrayInputStream(bytes)
    val in: ObjectInputStream = new ObjectInputStream(bais)
    val result: ZoneRules = in.readObject.asInstanceOf[ZoneRules]
    assertEquals(result, test)
    assertEquals(result.getClass, test.getClass)
  }

  @Test def test_data_nullInput(): Unit = {
    val test: ZoneRules = make(TestFixedZoneRules.OFFSET_PONE)
    assertEquals(test.getOffset(null.asInstanceOf[Instant]), TestFixedZoneRules.OFFSET_PONE)
    assertEquals(test.getOffset(null.asInstanceOf[LocalDateTime]), TestFixedZoneRules.OFFSET_PONE)
    assertEquals(test.getValidOffsets(null).size, 1)
    assertEquals(test.getValidOffsets(null).get(0), TestFixedZoneRules.OFFSET_PONE)
    assertEquals(test.getTransition(null), null)
    assertEquals(test.getStandardOffset(null), TestFixedZoneRules.OFFSET_PONE)
    assertEquals(test.getDaylightSavings(null), Duration.ZERO)
    assertEquals(test.isDaylightSavings(null), false)
    assertEquals(test.nextTransition(null), null)
    assertEquals(test.previousTransition(null), null)
  }

  @Test(dataProvider = "rules") def test_getOffset_Instant(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getOffset(TestFixedZoneRules.INSTANT), expectedOffset)
    assertEquals(test.getOffset(null.asInstanceOf[Instant]), expectedOffset)
  }

  @Test(dataProvider = "rules") def test_getOffset_LocalDateTime(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getOffset(TestFixedZoneRules.LDT), expectedOffset)
    assertEquals(test.getOffset(null.asInstanceOf[LocalDateTime]), expectedOffset)
  }

  @Test(dataProvider = "rules") def test_getValidOffsets_LDT(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getValidOffsets(TestFixedZoneRules.LDT).size, 1)
    assertEquals(test.getValidOffsets(TestFixedZoneRules.LDT).get(0), expectedOffset)
    assertEquals(test.getValidOffsets(null).size, 1)
    assertEquals(test.getValidOffsets(null).get(0), expectedOffset)
  }

  @Test(dataProvider = "rules") def test_getTransition_LDT(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getTransition(TestFixedZoneRules.LDT), null)
    assertEquals(test.getTransition(null), null)
  }

  @Test(dataProvider = "rules") def test_isValidOffset_LDT_ZO(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.isValidOffset(TestFixedZoneRules.LDT, expectedOffset), true)
    assertEquals(test.isValidOffset(TestFixedZoneRules.LDT, ZoneOffset.UTC), false)
    assertEquals(test.isValidOffset(TestFixedZoneRules.LDT, null), false)
    assertEquals(test.isValidOffset(null, expectedOffset), true)
    assertEquals(test.isValidOffset(null, ZoneOffset.UTC), false)
    assertEquals(test.isValidOffset(null, null), false)
  }

  @Test(dataProvider = "rules") def test_getStandardOffset_Instant(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getStandardOffset(TestFixedZoneRules.INSTANT), expectedOffset)
    assertEquals(test.getStandardOffset(null), expectedOffset)
  }

  @Test(dataProvider = "rules") def test_getDaylightSavings_Instant(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getDaylightSavings(TestFixedZoneRules.INSTANT), Duration.ZERO)
    assertEquals(test.getDaylightSavings(null), Duration.ZERO)
  }

  @Test(dataProvider = "rules") def test_isDaylightSavings_Instant(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.isDaylightSavings(TestFixedZoneRules.INSTANT), false)
    assertEquals(test.isDaylightSavings(null), false)
  }

  @Test(dataProvider = "rules") def test_nextTransition_Instant(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.nextTransition(TestFixedZoneRules.INSTANT), null)
    assertEquals(test.nextTransition(null), null)
  }

  @Test(dataProvider = "rules") def test_previousTransition_Instant(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.previousTransition(TestFixedZoneRules.INSTANT), null)
    assertEquals(test.previousTransition(null), null)
  }

  @Test(dataProvider = "rules") def test_getTransitions(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getTransitions.size, 0)
  }

  @Test(expectedExceptions = Array(classOf[UnsupportedOperationException])) def test_getTransitions_immutable(): Unit = {
    val test: ZoneRules = make(TestFixedZoneRules.OFFSET_PTWO)
    test.getTransitions.add(ZoneOffsetTransition.of(TestFixedZoneRules.LDT, TestFixedZoneRules.OFFSET_PONE, TestFixedZoneRules.OFFSET_PTWO))
  }

  @Test(dataProvider = "rules") def test_getTransitionRules(test: ZoneRules, expectedOffset: ZoneOffset): Unit = {
    assertEquals(test.getTransitionRules.size, 0)
  }

  @Test(expectedExceptions = Array(classOf[UnsupportedOperationException])) def test_getTransitionRules_immutable(): Unit = {
    val test: ZoneRules = make(TestFixedZoneRules.OFFSET_PTWO)
    test.getTransitionRules.add(ZoneOffsetTransitionRule.of(Month.JULY, 2, null, LocalTime.of(12, 30), false, TimeDefinition.STANDARD, TestFixedZoneRules.OFFSET_PONE, TestFixedZoneRules.OFFSET_PTWO, TestFixedZoneRules.OFFSET_PONE))
  }

  @Test def test_equalsHashCode(): Unit = {
    val a: ZoneRules = make(TestFixedZoneRules.OFFSET_PONE)
    val b: ZoneRules = make(TestFixedZoneRules.OFFSET_PTWO)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
    assertEquals(a == "Rubbish", false)
    assertEquals(a == null, false)
    assertEquals(a.hashCode == a.hashCode, true)
    assertEquals(b.hashCode == b.hashCode, true)
  }
}
