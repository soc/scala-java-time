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
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.StringTokenizer
import org.testng.annotations.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.Year
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition

object TestTzdbZoneRulesCompiler {
  private[zone] var PARSE_YEAR: Method = null
  private[zone] var PARSE_MONTH: Method = null
  private[zone] var PARSE_DOW: Method = null
  private[zone] var PARSE_MDT: Method = null
  private[zone] var PARSE_LSR: Method = null
  try {
    PARSE_YEAR = classOf[TzdbZoneRulesCompiler].getDeclaredMethod("parseYear", classOf[String], Integer.TYPE)
    PARSE_YEAR.setAccessible(true)
  } catch {
    case ex: Exception => throw new RuntimeException(ex)
  }
  try {
    PARSE_MONTH = classOf[TzdbZoneRulesCompiler].getDeclaredMethod("parseMonth", classOf[String])
    PARSE_MONTH.setAccessible(true)
  } catch {
    case ex: Exception => throw new RuntimeException(ex)
  }
  try {
    PARSE_DOW = classOf[TzdbZoneRulesCompiler].getDeclaredMethod("parseDayOfWeek", classOf[String])
    PARSE_DOW.setAccessible(true)
  } catch {
    case ex: Exception => throw new RuntimeException(ex)
  }
  try {
    PARSE_MDT = classOf[TzdbZoneRulesCompiler].getDeclaredMethod("parseMonthDayTime", classOf[StringTokenizer], classOf[TzdbZoneRulesCompiler#TZDBMonthDayTime])
    PARSE_MDT.setAccessible(true)
  } catch {
    case ex: Exception => throw new RuntimeException(ex)
  }
  try {
    val scalaVersion = scala.util.Properties.versionNumberString
    val methodName =
      if (scalaVersion.startsWith("2.11"))
        "org$threeten$bp$zone$TzdbZoneRulesCompiler$$parseLeapSecondRule" // !!! WTF?
      else if (scalaVersion.startsWith("2.12"))
        "parseLeapSecondRule"
      else
        ???
    PARSE_LSR = classOf[TzdbZoneRulesCompiler].getDeclaredMethod(methodName, classOf[String])
    PARSE_LSR.setAccessible(true)
  } catch {
    case ex: Exception => throw new RuntimeException(ex)
  }
}

/** Test TzdbZoneRulesCompiler.
  */
@Test class TestTzdbZoneRulesCompiler {
  @Test
  @throws[Exception]
  def test_parseYear_specific(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "2010", 2000), 2010)
  }

  @Test
  @throws[Exception]
  def test_parseYear_min(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "min", 2000), Year.MIN_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_mini(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "mini", 2000), Year.MIN_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_minim(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "minim", 2000), Year.MIN_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_minimu(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "minimu", 2000), Year.MIN_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_minimum(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "minimum", 2000), Year.MIN_VALUE)
  }

  @Test(expectedExceptions = Array(classOf[NumberFormatException]))
  @throws[Exception]
  def test_parseYear_minTooShort(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseYear(test, "mi", 2000)
  }

  @Test(expectedExceptions = Array(classOf[NumberFormatException]))
  @throws[Exception]
  def test_parseYear_minTooLong(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseYear(test, "minimuma", 2000)
  }

  @Test
  @throws[Exception]
  def test_parseYear_max(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "max", 2000), Year.MAX_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_maxi(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "maxi", 2000), Year.MAX_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_maxim(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "maxim", 2000), Year.MAX_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_maximu(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "maximu", 2000), Year.MAX_VALUE)
  }

  @Test
  @throws[Exception]
  def test_parseYear_maximum(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "maximum", 2000), Year.MAX_VALUE)
  }

  @Test(expectedExceptions = Array(classOf[NumberFormatException]))
  @throws[Exception]
  def test_parseYear_maxTooShort(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseYear(test, "ma", 2000)
  }

  @Test(expectedExceptions = Array(classOf[NumberFormatException]))
  @throws[Exception]
  def test_parseYear_maxTooLong(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseYear(test, "maximuma", 2000)
  }

  @Test
  @throws[Exception]
  def test_parseYear_only(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "only", 2000), 2000)
  }

  @Test
  @throws[Exception]
  def test_parseYear_only_uppercase(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseYear(test, "ONLY", 2000), 2000)
  }

  @Test(expectedExceptions = Array(classOf[NumberFormatException]))
  @throws[Exception]
  def test_parseYear_invalidYear(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseYear(test, "ABC", 2000)
  }

  @throws[Exception]
  private def parseYear(test: TzdbZoneRulesCompiler, str: String, year: Int): Int = {
    try TestTzdbZoneRulesCompiler.PARSE_YEAR.invoke(test, str, year.asInstanceOf[AnyRef]).asInstanceOf[Integer]
    catch {
      case ex: InvocationTargetException =>
        if (ex.getCause != null)
          throw ex.getCause.asInstanceOf[Exception]
        throw ex
    }
  }

  @Test
  @throws[Exception]
  def test_parseMonth(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseMonth(test, "Jan"), Month.JANUARY)
    assertEquals(parseMonth(test, "Feb"), Month.FEBRUARY)
    assertEquals(parseMonth(test, "Mar"), Month.MARCH)
    assertEquals(parseMonth(test, "Apr"), Month.APRIL)
    assertEquals(parseMonth(test, "May"), Month.MAY)
    assertEquals(parseMonth(test, "Jun"), Month.JUNE)
    assertEquals(parseMonth(test, "Jul"), Month.JULY)
    assertEquals(parseMonth(test, "Aug"), Month.AUGUST)
    assertEquals(parseMonth(test, "Sep"), Month.SEPTEMBER)
    assertEquals(parseMonth(test, "Oct"), Month.OCTOBER)
    assertEquals(parseMonth(test, "Nov"), Month.NOVEMBER)
    assertEquals(parseMonth(test, "Dec"), Month.DECEMBER)
    assertEquals(parseMonth(test, "January"), Month.JANUARY)
    assertEquals(parseMonth(test, "February"), Month.FEBRUARY)
    assertEquals(parseMonth(test, "March"), Month.MARCH)
    assertEquals(parseMonth(test, "April"), Month.APRIL)
    assertEquals(parseMonth(test, "May"), Month.MAY)
    assertEquals(parseMonth(test, "June"), Month.JUNE)
    assertEquals(parseMonth(test, "July"), Month.JULY)
    assertEquals(parseMonth(test, "August"), Month.AUGUST)
    assertEquals(parseMonth(test, "September"), Month.SEPTEMBER)
    assertEquals(parseMonth(test, "October"), Month.OCTOBER)
    assertEquals(parseMonth(test, "November"), Month.NOVEMBER)
    assertEquals(parseMonth(test, "December"), Month.DECEMBER)
    assertEquals(parseMonth(test, "Janu"), Month.JANUARY)
    assertEquals(parseMonth(test, "Janua"), Month.JANUARY)
    assertEquals(parseMonth(test, "Januar"), Month.JANUARY)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws[Exception]
  def test_parseMonth_invalidMonth(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseMonth(test, "ABC")
  }

  @throws[Exception]
  private def parseMonth(test: TzdbZoneRulesCompiler, str: String): Month = {
    try {
      TestTzdbZoneRulesCompiler.PARSE_MONTH.invoke(test, str).asInstanceOf[Month]
    }
    catch {
      case ex: InvocationTargetException =>
        if (ex.getCause != null) {
          throw ex.getCause.asInstanceOf[Exception]
        }
        throw ex
    }
  }

  @Test
  @throws[Exception]
  def test_parseDayOfWeek(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    assertEquals(parseDayOfWeek(test, "Mon"), DayOfWeek.MONDAY)
    assertEquals(parseDayOfWeek(test, "Tue"), DayOfWeek.TUESDAY)
    assertEquals(parseDayOfWeek(test, "Wed"), DayOfWeek.WEDNESDAY)
    assertEquals(parseDayOfWeek(test, "Thu"), DayOfWeek.THURSDAY)
    assertEquals(parseDayOfWeek(test, "Fri"), DayOfWeek.FRIDAY)
    assertEquals(parseDayOfWeek(test, "Sat"), DayOfWeek.SATURDAY)
    assertEquals(parseDayOfWeek(test, "Sun"), DayOfWeek.SUNDAY)
    assertEquals(parseDayOfWeek(test, "Monday"), DayOfWeek.MONDAY)
    assertEquals(parseDayOfWeek(test, "Tuesday"), DayOfWeek.TUESDAY)
    assertEquals(parseDayOfWeek(test, "Wednesday"), DayOfWeek.WEDNESDAY)
    assertEquals(parseDayOfWeek(test, "Thursday"), DayOfWeek.THURSDAY)
    assertEquals(parseDayOfWeek(test, "Friday"), DayOfWeek.FRIDAY)
    assertEquals(parseDayOfWeek(test, "Saturday"), DayOfWeek.SATURDAY)
    assertEquals(parseDayOfWeek(test, "Sunday"), DayOfWeek.SUNDAY)
    assertEquals(parseDayOfWeek(test, "Mond"), DayOfWeek.MONDAY)
    assertEquals(parseDayOfWeek(test, "Monda"), DayOfWeek.MONDAY)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws[Exception]
  def test_parseDayOfWeek_invalidMonth(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseMonth(test, "ABC")
  }

  @throws[Exception]
  private def parseDayOfWeek(test: TzdbZoneRulesCompiler, str: String): DayOfWeek = {
    try {
      TestTzdbZoneRulesCompiler.PARSE_DOW.invoke(test, str).asInstanceOf[DayOfWeek]
    }
    catch {
      case ex: InvocationTargetException =>
        if (ex.getCause != null) {
          throw ex.getCause.asInstanceOf[Exception]
        }
        throw ex
    }
  }

  @Test
  @throws[Exception]
  def test_parseMonthDayTime_marLastSun0220(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val mdt: TzdbZoneRulesCompiler#TZDBRule = parseMonthDayTime(test, "Mar lastSun 2:20")
    assertEquals(mdt.month, Month.MARCH)
    assertEquals(mdt.dayOfWeek, DayOfWeek.SUNDAY)
    assertEquals(mdt.dayOfMonth, -1)
    assertEquals(mdt.adjustForwards, false)
    assertEquals(mdt.time, LocalTime.of(2, 20))
    assertEquals(mdt.endOfDay, false)
    assertEquals(mdt.timeDefinition, TimeDefinition.WALL)
  }

  @Test
  @throws[Exception]
  def test_parseMonthDayTime_jun50220s(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val mdt: TzdbZoneRulesCompiler#TZDBRule = parseMonthDayTime(test, "Jun 5 2:20s")
    assertEquals(mdt.month, Month.JUNE)
    assertEquals(mdt.dayOfWeek, null)
    assertEquals(mdt.dayOfMonth, 5)
    assertEquals(mdt.adjustForwards, true)
    assertEquals(mdt.time, LocalTime.of(2, 20))
    assertEquals(mdt.endOfDay, false)
    assertEquals(mdt.timeDefinition, TimeDefinition.STANDARD)
  }

  @Test
  @throws[Exception]
  def test_parseMonthDayTime_maySatAfter50220u(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val mdt: TzdbZoneRulesCompiler#TZDBRule = parseMonthDayTime(test, "May Sat>=5 2:20u")
    assertEquals(mdt.month, Month.MAY)
    assertEquals(mdt.dayOfWeek, DayOfWeek.SATURDAY)
    assertEquals(mdt.dayOfMonth, 5)
    assertEquals(mdt.adjustForwards, true)
    assertEquals(mdt.time, LocalTime.of(2, 20))
    assertEquals(mdt.endOfDay, false)
    assertEquals(mdt.timeDefinition, TimeDefinition.UTC)
  }

  @Test
  @throws[Exception]
  def test_parseMonthDayTime_maySatBefore50220u(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val mdt: TzdbZoneRulesCompiler#TZDBRule = parseMonthDayTime(test, "May Sat<=5 24:00g")
    assertEquals(mdt.month, Month.MAY)
    assertEquals(mdt.dayOfWeek, DayOfWeek.SATURDAY)
    assertEquals(mdt.dayOfMonth, 5)
    assertEquals(mdt.adjustForwards, false)
    assertEquals(mdt.time, LocalTime.of(0, 0))
    assertEquals(mdt.endOfDay, true)
    assertEquals(mdt.timeDefinition, TimeDefinition.UTC)
  }

  @Test
  @throws[Exception]
  def test_parseMonthDayTime_maySatBefore15Dash(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val mdt: TzdbZoneRulesCompiler#TZDBRule = parseMonthDayTime(test, "May Sat<=15 -")
    assertEquals(mdt.month, Month.MAY)
    assertEquals(mdt.dayOfWeek, DayOfWeek.SATURDAY)
    assertEquals(mdt.dayOfMonth, 15)
    assertEquals(mdt.adjustForwards, false)
    assertEquals(mdt.time, LocalTime.of(0, 0))
    assertEquals(mdt.endOfDay, false)
    assertEquals(mdt.timeDefinition, TimeDefinition.WALL)
  }

  @Test
  @throws[Exception]
  def test_parseMonthDayTime_maylastSunShortTime(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val mdt: TzdbZoneRulesCompiler#TZDBRule = parseMonthDayTime(test, "May lastSun 3z")
    assertEquals(mdt.month, Month.MAY)
    assertEquals(mdt.dayOfWeek, DayOfWeek.SUNDAY)
    assertEquals(mdt.dayOfMonth, -1)
    assertEquals(mdt.adjustForwards, false)
    assertEquals(mdt.time, LocalTime.of(3, 0))
    assertEquals(mdt.endOfDay, false)
    assertEquals(mdt.timeDefinition, TimeDefinition.UTC)
  }

  @throws[Exception]
  private def parseMonthDayTime(test: TzdbZoneRulesCompiler, str: String): TzdbZoneRulesCompiler#TZDBRule = {
    try {
      val mdt: TzdbZoneRulesCompiler#TZDBRule = new test.TZDBRule
      TestTzdbZoneRulesCompiler.PARSE_MDT.invoke(test, new StringTokenizer(str), mdt)
      mdt
    }
    catch {
      case ex: InvocationTargetException =>
        if (ex.getCause != null)
          throw ex.getCause.asInstanceOf[Exception]
        throw ex
    }
  }

  @Test
  @throws[Exception]
  def test_parseLeapSecondRule_at_midnight(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val lsr: TzdbZoneRulesCompiler.LeapSecondRule = parseLeapSecondRule(test, "Leap\t1972 Jun\t30   23:59:60 +   S")
    assertEquals(lsr.leapDate, LocalDate.of(1972, Month.JUNE, 30))
    assertEquals(lsr.secondAdjustment, +1)
  }

  @Test
  @throws[Exception]
  def test_parseLeapSecondRule_just_before_midnight(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    val lsr: TzdbZoneRulesCompiler.LeapSecondRule = parseLeapSecondRule(test, "Leap\t2009 May\t1   23:59:59 - S")
    assertEquals(lsr.leapDate, LocalDate.of(2009, Month.MAY, 1))
    assertEquals(lsr.secondAdjustment, -1)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws[Exception]
  def test_parseLeapSecondRule_too_short(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseLeapSecondRule(test, "Leap\t2009 May\t1  23:59:60 S")
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws[Exception]
  def test_parseLeapSecondRule_bad_adjustment(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseLeapSecondRule(test, "Leap\t2009 May\t1   23:59:60 % S")
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  @throws[Exception]
  def test_parseLeapSecondRule_rolling(): Unit = {
    val test: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler("2010c", new java.util.ArrayList[File], null, false)
    parseLeapSecondRule(test, "Leap\t2009 May\t1   23:59:60 - R")
  }

  @throws[Exception]
  private def parseLeapSecondRule(test: TzdbZoneRulesCompiler, str: String): TzdbZoneRulesCompiler.LeapSecondRule = {
    try {
      TestTzdbZoneRulesCompiler.PARSE_LSR.invoke(test, str).asInstanceOf[TzdbZoneRulesCompiler.LeapSecondRule]
    }
    catch {
      case ex: InvocationTargetException =>
        if (ex.getCause != null) {
          throw ex.getCause.asInstanceOf[Exception]
        }
        throw ex
    }
  }
}
