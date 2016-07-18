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

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

import java.sql.Time
import java.sql.Timestamp

/** Test. */
object TestDateTimeUtils {
  private val PARIS: ZoneId = ZoneId.of("Europe/Paris")
  private val PARIS_TZ: TimeZone = TimeZone.getTimeZone("Europe/Paris")
}

@Test class TestDateTimeUtils {
  def test_toInstant_Date(): Unit = {
    val date: Date = new Date(123456)
    assertEquals(DateTimeUtils.toInstant(date), Instant.ofEpochMilli(123456))
  }

  def test_toDate_Instant(): Unit = {
    val instant: Instant = Instant.ofEpochMilli(123456)
    assertEquals(DateTimeUtils.toDate(instant), new Date(123456))
  }

  /*
  def test_toInstant_Calendar(): Unit = {
    val calendar: Calendar = Calendar.getInstance
    calendar.setTimeInMillis(123456)
    assertEquals(DateTimeUtils.toInstant(calendar), Instant.ofEpochMilli(123456))
  }
  */

  def test_toZDT_Calendar(): Unit = {
    val zdt: ZonedDateTime = ZonedDateTime.of(2012, 6, 30, 11, 30, 40, 0, TestDateTimeUtils.PARIS)
    val calendar: Calendar = Calendar.getInstance(TestDateTimeUtils.PARIS_TZ)
    calendar.setFirstDayOfWeek(Calendar.MONDAY)
    calendar.setMinimalDaysInFirstWeek(4)
    calendar.clear()
    calendar.set(2012, 6 - 1, 30, 11, 30, 40)
    assertEquals(DateTimeUtils.toZonedDateTime(calendar), zdt)
  }

  def test_toCalendar_ZDT(): Unit = {
    val zdt: ZonedDateTime = ZonedDateTime.of(2012, 6, 30, 11, 30, 40, 0, TestDateTimeUtils.PARIS)
    val calendar: GregorianCalendar = new GregorianCalendar(TestDateTimeUtils.PARIS_TZ)
    calendar.setFirstDayOfWeek(Calendar.MONDAY)
    calendar.setMinimalDaysInFirstWeek(4)
    calendar.set(2012, 6 - 1, 30, 11, 30, 40)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.setTimeInMillis(calendar.getTimeInMillis)
    calendar.setGregorianChange(new Date(Long.MinValue))
    val test: GregorianCalendar = DateTimeUtils.toGregorianCalendar(zdt)
    assertEquals(test, calendar)
  }

  def test_toZoneId_TimeZone(): Unit = {
    assertEquals(DateTimeUtils.toZoneId(TestDateTimeUtils.PARIS_TZ), TestDateTimeUtils.PARIS)
  }

  def test_toTimeZone_ZoneId(): Unit = {
    assertEquals(DateTimeUtils.toTimeZone(TestDateTimeUtils.PARIS), TestDateTimeUtils.PARIS_TZ)
  }

  /*
  def test_toLocalDate_SqlDate(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlDate: Date = new Date(2012 - 1900, 6 - 1, 30)
    val localDate: LocalDate = LocalDate.of(2012, 6, 30)
    assertEquals(DateTimeUtils.toLocalDate(sqlDate), localDate)
  }
  */

  def test_toSqlDate_LocalDate(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlDate: Date = new Date(2012 - 1900, 6 - 1, 30)
    val localDate: LocalDate = LocalDate.of(2012, 6, 30)
    assertEquals(DateTimeUtils.toSqlDate(localDate), sqlDate)
  }

  def test_toLocalTime_SqlTime(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlTime: Time = new Time(11, 30, 40)
    val localTime: LocalTime = LocalTime.of(11, 30, 40)
    assertEquals(DateTimeUtils.toLocalTime(sqlTime), localTime)
  }

  def test_toSqlTime_LocalTime(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlTime: Time = new Time(11, 30, 40)
    val localTime: LocalTime = LocalTime.of(11, 30, 40)
    assertEquals(DateTimeUtils.toSqlTime(localTime), sqlTime)
  }

  def test_toLocalDateTime_SqlTimestamp(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlDateTime: Timestamp = new Timestamp(2012 - 1900, 6 - 1, 30, 11, 30, 40, 0)
    val localDateTime: LocalDateTime = LocalDateTime.of(2012, 6, 30, 11, 30, 40, 0)
    assertEquals(DateTimeUtils.toLocalDateTime(sqlDateTime), localDateTime)
  }

  def test_toSqlTimestamp_LocalDateTime(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlDateTime: Timestamp = new Timestamp(2012 - 1900, 6 - 1, 30, 11, 30, 40, 0)
    val localDateTime: LocalDateTime = LocalDateTime.of(2012, 6, 30, 11, 30, 40, 0)
    assertEquals(DateTimeUtils.toSqlTimestamp(localDateTime), sqlDateTime)
  }

  def test_toInstant_SqlTimestamp(): Unit = {
    @SuppressWarnings(Array("deprecation")) val sqlDateTime: Timestamp = new Timestamp(2012 - 1900, 6 - 1, 30, 11, 30, 40, 0)
    assertEquals(DateTimeUtils.toInstant(sqlDateTime), Instant.ofEpochMilli(sqlDateTime.getTime))
  }

  def test_toSqlTimestamp_Instant(): Unit = {
    val instant: Instant = Instant.ofEpochMilli(123456)
    val sqlDateTime: Timestamp = new Timestamp(instant.toEpochMilli)
    assertEquals(DateTimeUtils.toSqlTimestamp(instant), sqlDateTime)
  }
}
