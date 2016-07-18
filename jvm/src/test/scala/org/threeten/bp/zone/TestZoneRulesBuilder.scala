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
import org.testng.Assert.assertNotNull
import org.threeten.bp.DayOfWeek.FRIDAY
import org.threeten.bp.DayOfWeek.MONDAY
import org.threeten.bp.DayOfWeek.SUNDAY
import org.threeten.bp.DayOfWeek.THURSDAY
import org.threeten.bp.DayOfWeek.TUESDAY
import org.threeten.bp.Month.APRIL
import org.threeten.bp.Month.AUGUST
import org.threeten.bp.Month.FEBRUARY
import org.threeten.bp.Month.MARCH
import org.threeten.bp.Month.NOVEMBER
import org.threeten.bp.Month.OCTOBER
import org.threeten.bp.Month.SEPTEMBER
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition.STANDARD
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition.UTC
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition.WALL
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.Year
import org.threeten.bp.ZoneOffset

/** Test ZoneRulesBuilder. */
object TestZoneRulesBuilder {
  private val OFFSET_1: ZoneOffset = ZoneOffset.ofHours(1)
  private val OFFSET_2: ZoneOffset = ZoneOffset.ofHours(2)
  private val OFFSET_1_15: ZoneOffset = ZoneOffset.ofHoursMinutes(1, 15)
  private val OFFSET_2_30: ZoneOffset = ZoneOffset.ofHoursMinutes(2, 30)
  private val PERIOD_0: Int = 0
  private val PERIOD_1HOUR: Int = 60 * 60
  private val PERIOD_1HOUR30MIN: Int = ((1 * 60) + 30) * 60
  private val DATE_TIME_FIRST: LocalDateTime = dateTime(Year.MIN_VALUE, 1, 1, 0, 0)
  private val DATE_TIME_LAST: LocalDateTime = dateTime(Year.MAX_VALUE, 12, 31, 23, 59)
  private val DATE_TIME_2008_01_01: LocalDateTime = dateTime(2008, 1, 1, 0, 0)
  private val DATE_TIME_2008_07_01: LocalDateTime = dateTime(2008, 7, 1, 0, 0)

  private def assertGap(test: ZoneRules, y: Int, m: Int, d: Int, hr: Int, min: Int, before: ZoneOffset, after: ZoneOffset): Unit = {
    val dt: LocalDateTime = dateTime(y, m, d, hr, min)
    val zot: ZoneOffsetTransition = test.getTransition(dt)
    assertNotNull(zot)
    assertEquals(zot.isGap, true)
    assertEquals(zot.getOffsetBefore, before)
    assertEquals(zot.getOffsetAfter, after)
  }

  private def assertOverlap(test: ZoneRules, y: Int, m: Int, d: Int, hr: Int, min: Int, before: ZoneOffset, after: ZoneOffset): Unit = {
    val dt: LocalDateTime = dateTime(y, m, d, hr, min)
    val zot: ZoneOffsetTransition = test.getTransition(dt)
    assertNotNull(zot)
    assertEquals(zot.isOverlap, true)
    assertEquals(zot.getOffsetBefore, before)
    assertEquals(zot.getOffsetAfter, after)
  }

  def time(h: Int, m: Int): LocalTime = LocalTime.of(h, m)

  def dateTime(year: Int, month: Int, day: Int, h: Int, m: Int): LocalDateTime = LocalDateTime.of(year, month, day, h, m)

  def dateTime(year: Int, month: Month, day: Int, h: Int, m: Int): LocalDateTime = LocalDateTime.of(year, month, day, h, m)
}

@Test class TestZoneRulesBuilder {
  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_toRules_noWindows(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.toRules("Europe/London")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_toRules_null(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_2_30)
    b.toRules(null)
  }

  @Test def test_combined_singleCutover(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1950, 1, 1, 1, 0), STANDARD)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_2)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 1950, 1, 1, 1, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_2)
  }

  @Test def test_combined_localFixedRules(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1_15, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1950, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1_15)
    TestZoneRulesBuilder.assertOverlap(test, 1920, 1, 1, 0, 55, TestZoneRulesBuilder.OFFSET_1_15, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1800, 7, 1, 1, 0), TestZoneRulesBuilder.OFFSET_1_15)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1960, 1, 1, 1, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 1, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_01_01, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_07_01, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertGap(test, 2008, 3, 30, 1, 20, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertOverlap(test, 2008, 10, 26, 0, 20, TestZoneRulesBuilder.OFFSET_2_30, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_windowChangeDuringDST(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(2000, 7, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/Dublin")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 7, 1, 1, 20, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 3, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 29, 1, 20, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 12, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_windowChangeWithinDST(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(2000, 7, 1, 1, 0), WALL)
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(2000, 8, 1, 2, 0), WALL)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    val test: ZoneRules = b.toRules("Europe/Dublin")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 7, 1, 1, 20, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 3, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 8, 1, 1, 20, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 12, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_endsInSavings(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1_15, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    val test: ZoneRules = b.toRules("Pacific/Auckland")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1_15)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 1920, 1, 1, 0, 55, TestZoneRulesBuilder.OFFSET_1_15, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 26, 0, 59), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 26, 1, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 10, 29, 1, 20, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2001, 3, 25, 0, 20, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2001, 10, 28, 1, 20, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
  }

  @Test def test_combined_closeTransitions(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 20, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, MARCH, 20, TestZoneRulesBuilder.time(4, 2), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 20, 1, 59), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 20, 2, 0, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 20, 2, 59, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 20, 3, 0), TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 20, 3, 1), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 3, 20, 3, 2, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 3, 20, 4, 1, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 20, 4, 2), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_closeTransitionsMeet(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 20, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, MARCH, 20, TestZoneRulesBuilder.time(4, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 20, 1, 59), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 20, 2, 0, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 20, 2, 59, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 3, 20, 3, 0, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 3, 20, 3, 59, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 20, 4, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_weirdSavingsBeforeLast(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(1998, MARCH, 20, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, 20, null, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, 20, null, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 3, 20, 1, 30, TestZoneRulesBuilder.OFFSET_2_30, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 20, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2001, 3, 20, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2001, 10, 20, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_differentLengthLastRules1(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(1998, MARCH, 20, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1998, Year.MAX_VALUE, OCTOBER, 30, null, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(1999, MARCH, 21, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, MARCH, 22, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2001, MARCH, 23, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2002, Year.MAX_VALUE, MARCH, 24, null, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 1998, 3, 20, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 1998, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 1999, 3, 21, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 1999, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 22, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2001, 3, 23, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2001, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2002, 3, 24, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2002, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2003, 3, 24, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2003, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2004, 3, 24, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2004, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2005, 3, 24, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2005, 10, 30, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_combined_differentLengthLastRules2(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1920, 1, 1, 1, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(1998, Year.MAX_VALUE, MARCH, 30, null, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1998, OCTOBER, 20, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(1999, OCTOBER, 21, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(2000, OCTOBER, 22, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(2001, OCTOBER, 23, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(2002, Year.MAX_VALUE, OCTOBER, 24, null, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 1998, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 1998, 10, 20, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 1999, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 1999, 10, 21, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 22, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2001, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2001, 10, 23, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2002, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2002, 10, 24, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2003, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2003, 10, 24, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2004, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2004, 10, 24, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2005, 3, 30, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2005, 10, 24, 1, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_twoChangesSameDay(): Unit = {
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val plus3: ZoneOffset = ZoneOffset.ofHours(3)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(plus2)
    b.addRuleToWindow(2010, 2010, SEPTEMBER, 10, null, TestZoneRulesBuilder.time(12, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2010, 2010, SEPTEMBER, 10, null, TestZoneRulesBuilder.time(23, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Africa/Cairo")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus2)
    TestZoneRulesBuilder.assertGap(test, 2010, 9, 10, 12, 0, plus2, plus3)
    TestZoneRulesBuilder.assertOverlap(test, 2010, 9, 10, 23, 0, plus3, plus2)
  }

  @Test def test_twoChangesDifferentDefinition(): Unit = {
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val plus3: ZoneOffset = ZoneOffset.ofHours(3)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(plus2)
    b.addRuleToWindow(2010, 2010, SEPTEMBER, -1, TUESDAY, TestZoneRulesBuilder.time(0, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2010, 2010, SEPTEMBER, 29, null, TestZoneRulesBuilder.time(23, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Africa/Cairo")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus2)
    TestZoneRulesBuilder.assertGap(test, 2010, 9, 28, 0, 0, plus2, plus3)
    TestZoneRulesBuilder.assertOverlap(test, 2010, 9, 29, 23, 0, plus3, plus2)
  }

  @Test def test_argentina(): Unit = {
    val minus3: ZoneOffset = ZoneOffset.ofHours(-3)
    val minus4: ZoneOffset = ZoneOffset.ofHours(-4)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(minus3, TestZoneRulesBuilder.dateTime(1900, 1, 1, 0, 0), WALL)
    b.addWindow(minus3, TestZoneRulesBuilder.dateTime(1999, 10, 3, 0, 0), WALL)
    b.addRuleToWindow(1993, MARCH, 3, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(1999, OCTOBER, 3, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, MARCH, 3, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindow(minus4, TestZoneRulesBuilder.dateTime(2000, 3, 3, 0, 0), WALL)
    b.addRuleToWindow(1993, MARCH, 3, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(1999, OCTOBER, 3, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, MARCH, 3, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindowForever(minus3)
    val test: ZoneRules = b.toRules("America/Argentina/Tucuman")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 2, 22, 59), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 2, 23, 59), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 3, 0, 0), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 3, 1, 0), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 2, 22, 59), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 2, 23, 59), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 3, 0, 0), minus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 3, 3, 1, 0), minus3)
  }

  @Test def test_cairo_dateChange(): Unit = {
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val plus3: ZoneOffset = ZoneOffset.ofHours(3)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(plus2)
    b.addRuleToWindow(2008, Year.MAX_VALUE, APRIL, -1, FRIDAY, TestZoneRulesBuilder.time(0, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2008, Year.MAX_VALUE, AUGUST, -1, THURSDAY, TestZoneRulesBuilder.time(23, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Africa/Cairo")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus2)
    TestZoneRulesBuilder.assertGap(test, 2009, 4, 24, 0, 0, plus2, plus3)
    TestZoneRulesBuilder.assertOverlap(test, 2009, 8, 27, 23, 0, plus3, plus2)
  }

  @Test def test_cairo_twoChangesSameMonth(): Unit = {
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val plus3: ZoneOffset = ZoneOffset.ofHours(3)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(plus2)
    b.addRuleToWindow(2010, 2010, AUGUST, 11, null, TestZoneRulesBuilder.time(0, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(2010, 2010, SEPTEMBER, 10, null, TestZoneRulesBuilder.time(0, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2010, 2010, SEPTEMBER, -1, THURSDAY, TestZoneRulesBuilder.time(23, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Africa/Cairo")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus2)
    TestZoneRulesBuilder.assertGap(test, 2010, 9, 10, 0, 0, plus2, plus3)
    TestZoneRulesBuilder.assertOverlap(test, 2010, 9, 30, 23, 0, plus3, plus2)
  }

  @Test def test_sofia_lastRuleClash(): Unit = {
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val plus3: ZoneOffset = ZoneOffset.ofHours(3)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(plus2, TestZoneRulesBuilder.dateTime(1997, 1, 1, 0, 0), WALL)
    b.addRuleToWindow(1996, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1996, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindowForever(plus2)
    b.addRuleToWindow(1996, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1996, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/Sofia")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus2)
    TestZoneRulesBuilder.assertGap(test, 1996, 3, 31, 1, 0, plus2, plus3)
    TestZoneRulesBuilder.assertOverlap(test, 1996, 10, 27, 0, 0, plus3, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 27, 1, 0), plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 27, 2, 0), plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 27, 3, 0), plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 27, 4, 0), plus2)
  }

  @Test def test_prague(): Unit = {
    val plus1: ZoneOffset = ZoneOffset.ofHours(1)
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(plus1, TestZoneRulesBuilder.dateTime(1944, 9, 17, 2, 0), STANDARD)
    b.addRuleToWindow(1944, 1945, APRIL, 1, MONDAY, TestZoneRulesBuilder.time(2, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1944, OCTOBER, 2, TestZoneRulesBuilder.time(2, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    b.addRuleToWindow(1945, SEPTEMBER, 16, TestZoneRulesBuilder.time(2, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    b.addWindow(plus1, TestZoneRulesBuilder.dateTime(1979, 1, 1, 0, 0), WALL)
    b.addRuleToWindow(1945, APRIL, 8, TestZoneRulesBuilder.time(2, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1945, NOVEMBER, 18, TestZoneRulesBuilder.time(2, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    b.addWindowForever(plus1)
    val test: ZoneRules = b.toRules("Europe/Sofia")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus1)
    TestZoneRulesBuilder.assertGap(test, 1944, 4, 3, 2, 30, plus1, plus2)
    TestZoneRulesBuilder.assertOverlap(test, 1944, 9, 17, 2, 30, plus2, plus1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1944, 9, 17, 3, 30), plus1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1944, 9, 17, 4, 30), plus1)
    TestZoneRulesBuilder.assertGap(test, 1945, 4, 8, 2, 30, plus1, plus2)
    TestZoneRulesBuilder.assertOverlap(test, 1945, 11, 18, 2, 30, plus2, plus1)
  }

  @Test def test_tbilisi(): Unit = {
    val plus4: ZoneOffset = ZoneOffset.ofHours(4)
    val plus5: ZoneOffset = ZoneOffset.ofHours(5)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(plus4, TestZoneRulesBuilder.dateTime(1996, 10, 27, 0, 0), WALL)
    b.addRuleToWindow(1996, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1996, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindow(plus4, TestZoneRulesBuilder.dateTime(1997, 3, 30, 0, 0), WALL)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addWindowForever(plus4)
    b.addRuleToWindow(1996, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1996, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(0, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/Sofia")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus4)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus4)
    TestZoneRulesBuilder.assertGap(test, 1996, 3, 31, 0, 30, plus4, plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 26, 22, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 26, 23, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1996, 10, 27, 0, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1997, 3, 29, 22, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1997, 3, 29, 23, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1997, 3, 30, 0, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1997, 3, 30, 1, 30), plus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1997, 3, 30, 2, 30), plus5)
    TestZoneRulesBuilder.assertOverlap(test, 1997, 10, 25, 23, 30, plus5, plus4)
  }

  @Test def test_vincennes(): Unit = {
    val minus5: ZoneOffset = ZoneOffset.ofHours(-5)
    val minus6: ZoneOffset = ZoneOffset.ofHours(-6)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(minus6, TestZoneRulesBuilder.dateTime(2007, 11, 4, 2, 0), WALL)
    b.addRuleToWindow(2007, Year.MAX_VALUE, MARCH, 8, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2007, Year.MAX_VALUE, NOVEMBER, 1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindowForever(minus5)
    b.addRuleToWindow(2007, Year.MAX_VALUE, MARCH, 8, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2007, Year.MAX_VALUE, NOVEMBER, 1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("America/Indiana/Vincennes")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, minus6)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, minus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2007, 3, 11, 0, 0), minus6)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2007, 3, 11, 1, 0), minus6)
    TestZoneRulesBuilder.assertGap(test, 2007, 3, 11, 2, 0, minus6, minus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2007, 3, 11, 3, 0), minus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2007, 3, 11, 4, 0), minus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2007, 3, 11, 5, 0), minus5)
  }

  @Test def test_iqaluit(): Unit = {
    val minus4: ZoneOffset = ZoneOffset.ofHours(-4)
    val minus5: ZoneOffset = ZoneOffset.ofHours(-5)
    val minus6: ZoneOffset = ZoneOffset.ofHours(-6)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(minus5, TestZoneRulesBuilder.dateTime(1999, 10, 31, 2, 0), WALL)
    b.addRuleToWindow(1987, Year.MAX_VALUE, APRIL, 1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1987, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    b.addWindowForever(minus6)
    b.addRuleToWindow(1987, Year.MAX_VALUE, APRIL, 1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(1987, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(2, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("America/Iqaluit")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, minus5)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, minus6)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 30, 23, 0), minus4)
    TestZoneRulesBuilder.assertOverlap(test, 1999, 10, 31, 0, 0, minus4, minus6)
    TestZoneRulesBuilder.assertOverlap(test, 1999, 10, 31, 1, 0, minus4, minus6)
    TestZoneRulesBuilder.assertOverlap(test, 1999, 10, 31, 1, 59, minus4, minus6)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 31, 2, 0), minus6)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 10, 31, 3, 0), minus6)
  }

  @Test def test_jordan2400(): Unit = {
    val plus2: ZoneOffset = ZoneOffset.ofHours(2)
    val plus3: ZoneOffset = ZoneOffset.ofHours(3)
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(plus2)
    b.addRuleToWindow(2002, Year.MAX_VALUE, MARCH, -1, THURSDAY, TestZoneRulesBuilder.time(0, 0), true, WALL, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2002, Year.MAX_VALUE, SEPTEMBER, -1, FRIDAY, TestZoneRulesBuilder.time(0, 0), false, STANDARD, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Asia/Amman")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, plus2)
    TestZoneRulesBuilder.assertGap(test, 2002, 3, 29, 0, 0, plus2, plus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2002, 3, 28, 23, 0), plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2002, 3, 29, 1, 0), plus3)
    TestZoneRulesBuilder.assertOverlap(test, 2002, 9, 27, 0, 0, plus3, plus2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2002, 9, 26, 23, 0), plus3)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2002, 9, 27, 1, 0), plus2)
  }

  @Test def test_addWindow_constrainedRules(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1800, 7, 1, 0, 0), WALL)
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(2008, 6, 30, 0, 0), STANDARD)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_2_30)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_01_01, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_07_01, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 26, 1, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 29, 0, 30, TestZoneRulesBuilder.OFFSET_2_30, TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2008, 3, 30, 1, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2_30)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2008, 10, 26, 0, 30), TestZoneRulesBuilder.OFFSET_2_30)
  }

  @Test def test_addWindow_noRules(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1800, 7, 1, 0, 0), WALL)
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(2008, 6, 30, 0, 0), STANDARD)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_01_01, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_07_01, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addWindow_nullOffset(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(null.asInstanceOf[ZoneOffset], TestZoneRulesBuilder.dateTime(2008, 6, 30, 0, 0), STANDARD)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addWindow_nullTime(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, null.asInstanceOf[LocalDateTime], STANDARD)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addWindow_nullTimeDefinition(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(2008, 6, 30, 0, 0), null.asInstanceOf[ZoneOffsetTransitionRule.TimeDefinition])
  }

  @Test def test_addWindowForever_noRules(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_01_01, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_07_01, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_addWindowForever_rules(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.addRuleToWindow(2000, Year.MAX_VALUE, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_01_01, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_07_01, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertGap(test, 2008, 3, 30, 1, 20, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertOverlap(test, 2008, 10, 26, 0, 20, TestZoneRulesBuilder.OFFSET_2_30, TestZoneRulesBuilder.OFFSET_1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addWindowForever_nullOffset(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(null.asInstanceOf[ZoneOffset])
  }

  @Test def test_setFixedSavingsToWindow(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindow(TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.dateTime(1800, 7, 1, 0, 0), WALL)
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_2_30)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_01_01, TestZoneRulesBuilder.OFFSET_2_30)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_2008_07_01, TestZoneRulesBuilder.OFFSET_2_30)
    TestZoneRulesBuilder.assertGap(test, 1800, 7, 1, 0, 0, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2_30)
  }

  @Test def test_setFixedSavingsToWindow_first(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_FIRST, TestZoneRulesBuilder.OFFSET_2_30)
    assertOffsetInfo(test, TestZoneRulesBuilder.DATE_TIME_LAST, TestZoneRulesBuilder.OFFSET_2_30)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_setFixedSavingsToWindow_noWindow(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_setFixedSavingsToWindow_cannotMixSavingsWithRule(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2020, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_setFixedSavingsToWindow_cannotMixSavingsWithLastRule(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test def test_addRuleToWindow_endOfMonth(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2001, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, 2001, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 26, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 29, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2001, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2001, 3, 25, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2001, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2001, 10, 28, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2002, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_addRuleToWindow_endOfMonthFeb(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2004, 2005, FEBRUARY, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2004, 2005, OCTOBER, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2003, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2004, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2004, 2, 29, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2004, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2004, 10, 31, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2005, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2005, 2, 27, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2005, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2005, 10, 30, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2006, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test def test_addRuleToWindow_fromDayOfMonth(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2001, MARCH, 10, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, 2001, OCTOBER, 10, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 12, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 15, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2001, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2001, 3, 11, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2001, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2001, 10, 14, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2002, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_addRuleToWindow_noWindow(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_addRuleToWindow_cannotMixRuleWithSavings(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_addRuleToWindow_illegalYear1(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(Year.MIN_VALUE - 1, 2008, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_addRuleToWindow_illegalYear2(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MIN_VALUE - 1, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_illegalDayOfMonth_tooSmall(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2008, MARCH, -29, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_illegalDayOfMonth_zero(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2008, MARCH, 0, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_illegalDayOfMonth_tooLarge(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2008, MARCH, 32, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_nullMonth(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, null.asInstanceOf[Month], 31, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_nullTime(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, null.asInstanceOf[LocalTime], false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_illegalEndOfDayTime(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, 2008, MARCH, 1, SUNDAY, TestZoneRulesBuilder.time(1, 0), true, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_nullTimeDefinition(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, Year.MAX_VALUE, MARCH, -1, SUNDAY, TestZoneRulesBuilder.time(1, 0), false, null.asInstanceOf[ZoneOffsetTransitionRule.TimeDefinition], TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test def test_addRuleToWindow_singleYearObject(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(TestZoneRulesBuilder.dateTime(2000, MARCH, 26, 1, 0), UTC, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(TestZoneRulesBuilder.dateTime(2000, OCTOBER, 29, 1, 0), UTC, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 26, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 29, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2001, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_singleYearObject_nullTime(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(null.asInstanceOf[LocalDateTime], WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_singleYearObject_nullTimeDefinition(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(TestZoneRulesBuilder.dateTime(2000, MARCH, 31, 1, 0), null.asInstanceOf[ZoneOffsetTransitionRule.TimeDefinition], TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test def test_addRuleToWindow_singleYear(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 26, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_1HOUR)
    b.addRuleToWindow(2000, OCTOBER, 29, TestZoneRulesBuilder.time(1, 0), false, UTC, TestZoneRulesBuilder.PERIOD_0)
    val test: ZoneRules = b.toRules("Europe/London")
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(1999, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 1, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
    TestZoneRulesBuilder.assertGap(test, 2000, 3, 26, 2, 30, TestZoneRulesBuilder.OFFSET_1, TestZoneRulesBuilder.OFFSET_2)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2000, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_2)
    TestZoneRulesBuilder.assertOverlap(test, 2000, 10, 29, 2, 30, TestZoneRulesBuilder.OFFSET_2, TestZoneRulesBuilder.OFFSET_1)
    assertOffsetInfo(test, TestZoneRulesBuilder.dateTime(2001, 7, 1, 0, 0), TestZoneRulesBuilder.OFFSET_1)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_addRuleToWindow_singleYear_noWindow(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addRuleToWindow(2000, MARCH, 31, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException])) def test_addRuleToWindow_singleYear_cannotMixRuleWithSavings(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.setFixedSavingsToWindow(TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
    b.addRuleToWindow(2000, MARCH, 31, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_addRuleToWindow_singleYear_illegalYear(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(Year.MIN_VALUE - 1, MARCH, 31, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_singleYear_illegalDayOfMonth_tooSmall(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, -29, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_singleYear_illegalDayOfMonth_zero(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 0, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_addRuleToWindow_singleYear_illegalDayOfMonth_tooLarge(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 32, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_singleYear_nullMonth(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, null.asInstanceOf[Month], 31, TestZoneRulesBuilder.time(1, 0), false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_singleYear_nullTime(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 31, null.asInstanceOf[LocalTime], false, WALL, TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addRuleToWindow_singleYear_nullTimeDefinition(): Unit = {
    val b: ZoneRulesBuilder = new ZoneRulesBuilder
    b.addWindowForever(TestZoneRulesBuilder.OFFSET_1)
    b.addRuleToWindow(2000, MARCH, 31, TestZoneRulesBuilder.time(1, 0), false, null.asInstanceOf[ZoneOffsetTransitionRule.TimeDefinition], TestZoneRulesBuilder.PERIOD_1HOUR30MIN)
  }

  private def assertOffsetInfo(test: ZoneRules, dateTime: LocalDateTime, offset: ZoneOffset): Unit = {
    val offsets: java.util.List[ZoneOffset] = test.getValidOffsets(dateTime)
    assertEquals(offsets.size, 1)
    assertEquals(offsets.get(0), offset)
  }
}
