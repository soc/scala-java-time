package org.threeten.bp

import org.junit.Test
import org.junit.Assert._

class TestExamples {

  @Test def testExamples: Unit = {

    // always returns 2009-02-13T23:31:30
    val fixedClock = Clock.fixed(Instant.ofEpochSecond(1234567890L), ZoneOffset.ofHours(0))

    val date = LocalDateTime.now(fixedClock)

    assertEquals(date.getYear, 2009)
    assertEquals(date.getMonth, Month.FEBRUARY)
    assertEquals(date.getMonthValue, 2)
    assertEquals(date.getDayOfWeek, DayOfWeek.FRIDAY)
    assertEquals(date.getDayOfMonth, 13)
    assertEquals(date.getDayOfYear, 44)
    assertEquals(date.getHour, 23)
    assertEquals(date.getMinute, 31)
    assertEquals(date.getSecond, 30)

    val tomorrow = date.plusDays(1)

    val duration = Duration.between(date, tomorrow)
    assertEquals(duration.toMinutes, 1440L)

    val instant = Instant.now(fixedClock)

    val period = Period.between(date.toLocalDate, tomorrow.toLocalDate)
    assertEquals(period.get(temporal.ChronoUnit.DAYS), 1L)

    val date1 = LocalDate.of(2001, 1, 31)
    val date2 = LocalDate.of(2001, 2, 28)
    assertEquals(date1.plusMonths(1), date2)
    val date3 = date1.`with`(temporal.TemporalAdjusters.next(DayOfWeek.SUNDAY))
    val date4 = LocalDate.of(2001, 2, 4)
    assertEquals(date3, date4)

    val offsetTime = OffsetTime.of(date.toLocalTime, ZoneOffset.ofHours(1))
    assertEquals(offsetTime.getHour, 23)
    assertEquals(offsetTime.isBefore(OffsetTime.of(date.toLocalTime, ZoneOffset.ofHours(0))), true)

    val localeUS = java.util.Locale.US
    assertEquals(localeUS.getLanguage, "en")

    assertEquals(date.format(format.DateTimeFormatter.BASIC_ISO_DATE), "20090213")
    val format1 = format.DateTimeFormatter.ofPattern("MMMM MM d HH mm ss EE EEEE yyyy G[ VV z Z]", java.util.Locale.GERMAN)
    assertEquals(date.format(format1), "Februar 02 13 23 31 30 Fr. Freitag 2009 n. Chr.")

    val japDate = chrono.JapaneseDate.now(fixedClock)
    assertEquals(japDate.toString, "Japanese Heisei 21-02-13")

    //val hijDate = chrono.HijrahDate.now(fixedClock)
    //assertEquals(hijDate.toString, "")

    val thaiDate = chrono.ThaiBuddhistDate.now(fixedClock)
    assertEquals(thaiDate.toString, "ThaiBuddhist BE 2552-02-13")

    val mingDate = chrono.MinguoDate.now(fixedClock)
    assertEquals(mingDate.toString, "Minguo ROC 98-02-13")
  }
}
