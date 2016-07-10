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

import java.sql.{Time => SQLTime, Timestamp => SQLTimestamp, Date => SQLDate}
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

/** A set of utilities to assist in bridging the gap to Java 8.
  *
  * This class is not found in Java SE 8 but provides methods that are.
  */
object DateTimeUtils {
  /** Converts a {@code java.util.Date} to an {@code Instant}.
    *
    * @param utilDate  the util date, not null
    * @return the instant, not null
    */
  def toInstant(utilDate: Date): Instant = Instant.ofEpochMilli(utilDate.getTime)

  /** Converts an {@code Instant} to a {@code java.util.Date}.
    *
    * Fractions of the instant smaller than milliseconds will be dropped.
    *
    * @param instant  the instant, not null
    * @return the util date, not null
    * @throws IllegalArgumentException if the conversion fails
    */
  def toDate(instant: Instant): Date =
    try new Date(instant.toEpochMilli)
    catch {
      case ex: ArithmeticException => throw new IllegalArgumentException(ex)
    }

  /** Converts a {@code Calendar} to a {@code ZonedDateTime}.
    *
    * Note that {@code GregorianCalendar} supports a Julian-Gregorian cutover
    * date and {@code ZonedDateTime} does not so some differences will occur.
    *
    * @param calendar  the calendar, not null
    * @return the instant, not null
    */
  def toZonedDateTime(calendar: Calendar): ZonedDateTime = {
    val instant: Instant = Instant.ofEpochMilli(calendar.getTimeInMillis)
    val zone: ZoneId = toZoneId(calendar.getTimeZone)
    ZonedDateTime.ofInstant(instant, zone)
  }

  /** Converts a {@code ZonedDateTime} to a {@code Calendar}.
    *
    * The resulting {@code GregorianCalendar} is pure Gregorian and uses
    * ISO week definitions, starting on Monday and with 4 days in a minimal week.
    *
    * Fractions of the instant smaller than milliseconds will be dropped.
    *
    * @param zdt  the zoned date-time, not null
    * @return the calendar, not null
    * @throws IllegalArgumentException if the conversion fails
    */
  def toGregorianCalendar(zdt: ZonedDateTime): GregorianCalendar = {
    val zone: TimeZone = toTimeZone(zdt.getZone)
    val cal: GregorianCalendar = new GregorianCalendar(zone)
    cal.setGregorianChange(new Date(Long.MinValue))
    cal.setFirstDayOfWeek(Calendar.MONDAY)
    cal.setMinimalDaysInFirstWeek(4)
    try cal.setTimeInMillis(zdt.toInstant.toEpochMilli)
    catch {
      case ex: ArithmeticException => throw new IllegalArgumentException(ex)
    }
    cal
  }

  /** Converts a {@code TimeZone} to a {@code ZoneId}.
    *
    * @param timeZone  the time-zone, not null
    * @return the zone, not null
    */
  def toZoneId(timeZone: TimeZone): ZoneId = ZoneId.of(timeZone.getID, ZoneId.SHORT_IDS)

  /** Converts a {@code ZoneId} to a {@code TimeZone}.
    *
    * @param zoneId  the zone, not null
    * @return the time-zone, not null
    */
  def toTimeZone(zoneId: ZoneId): TimeZone = {
    var tzid: String = zoneId.getId
    if (tzid.startsWith("+") || tzid.startsWith("-"))
      tzid = s"GMT$tzid"
    else if (tzid == "Z")
      tzid = "UTC"
    TimeZone.getTimeZone(tzid)
  }

  /** Converts a {@code java.sql.Date} to a {@code LocalDate}.
    *
    * @param sqlDate  the SQL date, not null
    * @return the local date, not null
    */
  def toLocalDate(sqlDate: SQLDate): LocalDate = LocalDate.of(sqlDate.getYear + 1900, sqlDate.getMonth + 1, sqlDate.getDate)

  /** Converts a {@code LocalDate} to a {@code java.sql.Date}.
    *
    * @param date  the local date, not null
    * @return the SQL date, not null
    */
  def toSqlDate(date: LocalDate): SQLDate = new SQLDate(date.getYear - 1900, date.getMonthValue - 1, date.getDayOfMonth)

  /** Converts a {@code java.sql.Time} to a {@code LocalTime}.
    *
    * @param sqlTime  the SQL time, not null
    * @return the local time, not null
    */
  def toLocalTime(sqlTime: SQLTime): LocalTime = LocalTime.of(sqlTime.getHours, sqlTime.getMinutes, sqlTime.getSeconds)

  /** Converts a {@code LocalTime} to a {@code java.sql.Time}.
    *
    * @param time  the local time, not null
    * @return the SQL time, not null
    */
  def toSqlTime(time: LocalTime): SQLTime = new SQLTime(time.getHour, time.getMinute, time.getSecond)

  /** Converts a {@code LocalDateTime} to a {@code java.sql.Timestamp}.
    *
    * @param dateTime  the local date-time, not null
    * @return the SQL timestamp, not null
    */
  def toSqlTimestamp(dateTime: LocalDateTime): SQLTimestamp =
    new SQLTimestamp(dateTime.getYear - 1900, dateTime.getMonthValue - 1, dateTime.getDayOfMonth, dateTime.getHour, dateTime.getMinute, dateTime.getSecond, dateTime.getNano)

  /** Converts a {@code java.sql.Timestamp} to a {@code LocalDateTime}.
    *
    * @param sqlTimestamp  the SQL timestamp, not null
    * @return the local date-time, not null
    */
  def toLocalDateTime(sqlTimestamp: SQLTimestamp): LocalDateTime =
    LocalDateTime.of(sqlTimestamp.getYear + 1900, sqlTimestamp.getMonth + 1, sqlTimestamp.getDate, sqlTimestamp.getHours, sqlTimestamp.getMinutes, sqlTimestamp.getSeconds, sqlTimestamp.getNanos)

  /** Converts an {@code Instant} to a {@code java.sql.Timestamp}.
    *
    * @param instant  the instant, not null
    * @return the SQL timestamp, not null
    */
  def toSqlTimestamp(instant: Instant): SQLTimestamp = {
    try {
      val ts: SQLTimestamp = new SQLTimestamp(instant.getEpochSecond * 1000)
      ts.setNanos(instant.getNano)
      ts
    }
    catch {
      case ex: ArithmeticException => throw new IllegalArgumentException(ex)
    }
  }

  /** Converts a {@code java.sql.Timestamp} to an {@code Instant}.
    *
    * @param sqlTimestamp  the SQL timestamp, not null
    * @return the instant, not null
    */
  def toInstant(sqlTimestamp: SQLTimestamp): Instant = Instant.ofEpochSecond(sqlTimestamp.getTime / 1000, sqlTimestamp.getNanos)
}
