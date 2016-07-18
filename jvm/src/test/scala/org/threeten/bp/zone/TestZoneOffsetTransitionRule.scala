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
import java.io.IOException
import org.testng.annotations.Test
import org.threeten.bp.AbstractTest
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition

/** Test ZoneOffsetTransitionRule. */
object TestZoneOffsetTransitionRule {
  private val TIME_0100: LocalTime = LocalTime.of(1, 0)
  private val OFFSET_0200: ZoneOffset = ZoneOffset.ofHours(2)
  private val OFFSET_0300: ZoneOffset = ZoneOffset.ofHours(3)
}

@Test class TestZoneOffsetTransitionRule {
  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_nullMonth(): Unit = {
    ZoneOffsetTransitionRule.of(null, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_nullTime(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, null, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_nullTimeDefinition(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, null, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_nullStandardOffset(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, null, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_nullOffsetBefore(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, null, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_nullOffsetAfter(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, null)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_factory_invalidDayOfMonthIndicator_tooSmall(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, -29, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_factory_invalidDayOfMonthIndicator_zero(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 0, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_factory_invalidDayOfMonthIndicator_tooLarge(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 32, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_factory_invalidMidnightFlag(): Unit = {
    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, true, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
  }

  @Test
  @throws(classOf[Exception])
  def test_getters_floatingWeek(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.getMonth, Month.MARCH)
    assertEquals(test.getDayOfMonthIndicator, 20)
    assertEquals(test.getDayOfWeek, DayOfWeek.SUNDAY)
    assertEquals(test.getLocalTime, TestZoneOffsetTransitionRule.TIME_0100)
    assertEquals(test.isMidnightEndOfDay, false)
    assertEquals(test.getTimeDefinition, TimeDefinition.WALL)
    assertEquals(test.getStandardOffset, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.getOffsetBefore, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.getOffsetAfter, TestZoneOffsetTransitionRule.OFFSET_0300)
    AbstractTest.assertSerializable(test)
  }

  @Test
  @throws(classOf[Exception])
  def test_getters_floatingWeekBackwards(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.getMonth, Month.MARCH)
    assertEquals(test.getDayOfMonthIndicator, -1)
    assertEquals(test.getDayOfWeek, DayOfWeek.SUNDAY)
    assertEquals(test.getLocalTime, TestZoneOffsetTransitionRule.TIME_0100)
    assertEquals(test.isMidnightEndOfDay, false)
    assertEquals(test.getTimeDefinition, TimeDefinition.WALL)
    assertEquals(test.getStandardOffset, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.getOffsetBefore, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.getOffsetAfter, TestZoneOffsetTransitionRule.OFFSET_0300)
    AbstractTest.assertSerializable(test)
  }

  @Test
  @throws(classOf[Exception])
  def test_getters_fixedDate(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.getMonth, Month.MARCH)
    assertEquals(test.getDayOfMonthIndicator, 20)
    assertEquals(test.getDayOfWeek, null)
    assertEquals(test.getLocalTime, TestZoneOffsetTransitionRule.TIME_0100)
    assertEquals(test.isMidnightEndOfDay, false)
    assertEquals(test.getTimeDefinition, TimeDefinition.WALL)
    assertEquals(test.getStandardOffset, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.getOffsetBefore, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.getOffsetAfter, TestZoneOffsetTransitionRule.OFFSET_0300)
    AbstractTest.assertSerializable(test)
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization_unusualOffsets(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.STANDARD, ZoneOffset.ofHoursMinutesSeconds(-12, -20, -50), ZoneOffset.ofHoursMinutesSeconds(-4, -10, -34), ZoneOffset.ofHours(-18))
    AbstractTest.assertSerializable(test)
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization_endOfDay(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.FRIDAY, LocalTime.MIDNIGHT, true, TimeDefinition.UTC, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    AbstractTest.assertSerializable(test)
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization_unusualTime(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.WEDNESDAY, LocalTime.of(13, 34, 56), false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    AbstractTest.assertSerializable(test)
  }

  @Test
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  def test_serialization_format(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.TUESDAY, LocalTime.of(13, 34, 56), false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    AbstractTest.assertEqualsSerialisedForm(test)
  }

  @Test def test_createTransition_floatingWeek_gap_notEndOfDay(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val trans: ZoneOffsetTransition = new ZoneOffsetTransition(LocalDateTime.of(2000, Month.MARCH, 26, 1, 0), TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.createTransition(2000), trans)
  }

  @Test def test_createTransition_floatingWeek_overlap_endOfDay(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, true, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0200)
    val trans: ZoneOffsetTransition = new ZoneOffsetTransition(LocalDateTime.of(2000, Month.MARCH, 27, 0, 0), TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.createTransition(2000), trans)
  }

  @Test def test_createTransition_floatingWeekBackwards_last(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val trans: ZoneOffsetTransition = new ZoneOffsetTransition(LocalDateTime.of(2000, Month.MARCH, 26, 1, 0), TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.createTransition(2000), trans)
  }

  @Test def test_createTransition_floatingWeekBackwards_seventhLast(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -7, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val trans: ZoneOffsetTransition = new ZoneOffsetTransition(LocalDateTime.of(2000, Month.MARCH, 19, 1, 0), TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.createTransition(2000), trans)
  }

  @Test def test_createTransition_floatingWeekBackwards_secondLast(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -2, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val trans: ZoneOffsetTransition = new ZoneOffsetTransition(LocalDateTime.of(2000, Month.MARCH, 26, 1, 0), TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.createTransition(2000), trans)
  }

  @Test def test_createTransition_fixedDate(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val trans: ZoneOffsetTransition = new ZoneOffsetTransition(LocalDateTime.of(2000, Month.MARCH, 20, 1, 0), TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.createTransition(2000), trans)
  }

  @Test def test_equals_monthDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.APRIL, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_dayOfMonthDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 21, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_dayOfWeekDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SATURDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_dayOfWeekDifferentNull(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_localTimeDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_endOfDayDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, true, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_timeDefinitionDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_standardOffsetDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_offsetBeforeDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_offsetAfterDifferent(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(a == a, true)
    assertEquals(a == b, false)
    assertEquals(b == a, false)
    assertEquals(b == b, true)
  }

  @Test def test_equals_string_false(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == "TZDB", false)
  }

  @Test def test_equals_null_false(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a == null, false)
  }

  @Test def test_hashCode_floatingWeek_gap_notEndOfDay(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test def test_hashCode_floatingWeek_overlap_endOfDay_nullDayOfWeek(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.OCTOBER, 20, null, LocalTime.MIDNIGHT, true, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0200)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.OCTOBER, 20, null, LocalTime.MIDNIGHT, true, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test def test_hashCode_floatingWeekBackwards(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test def test_hashCode_fixedDate(): Unit = {
    val a: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    val b: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(a.hashCode, b.hashCode)
  }

  @Test def test_toString_floatingWeek_gap_notEndOfDay(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.toString, "TransitionRule[Gap +02:00 to +03:00, SUNDAY on or after MARCH 20 at 01:00 WALL, standard offset +02:00]")
  }

  @Test def test_toString_floatingWeek_overlap_endOfDay(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.OCTOBER, 20, DayOfWeek.SUNDAY, LocalTime.MIDNIGHT, true, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300, TestZoneOffsetTransitionRule.OFFSET_0200)
    assertEquals(test.toString, "TransitionRule[Overlap +03:00 to +02:00, SUNDAY on or after OCTOBER 20 at 24:00 WALL, standard offset +02:00]")
  }

  @Test def test_toString_floatingWeekBackwards_last(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.toString, "TransitionRule[Gap +02:00 to +03:00, SUNDAY on or before last day of MARCH at 01:00 WALL, standard offset +02:00]")
  }

  @Test def test_toString_floatingWeekBackwards_secondLast(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, -2, DayOfWeek.SUNDAY, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.WALL, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.toString, "TransitionRule[Gap +02:00 to +03:00, SUNDAY on or before last day minus 1 of MARCH at 01:00 WALL, standard offset +02:00]")
  }

  @Test def test_toString_fixedDate(): Unit = {
    val test: ZoneOffsetTransitionRule = ZoneOffsetTransitionRule.of(Month.MARCH, 20, null, TestZoneOffsetTransitionRule.TIME_0100, false, TimeDefinition.STANDARD, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0200, TestZoneOffsetTransitionRule.OFFSET_0300)
    assertEquals(test.toString, "TransitionRule[Gap +02:00 to +03:00, MARCH 20 at 01:00 STANDARD, standard offset +02:00]")
  }
}
