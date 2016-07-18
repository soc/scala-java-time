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
package org.threeten.bp.chrono

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Locale
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.ResolverStyle
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalAmount
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalUnit
import org.threeten.bp.temporal.ValueRange

/** Test assertions that must be true for all built-in chronologies. */
object TestChronoZonedDateTime {

  /** FixedAdjusted returns a fixed DateTime in all adjustments.
    * Construct an adjuster with the DateTime that should be returned from adjustIntoAdjustment.
    */
  private[chrono] class FixedAdjuster private[chrono](private var datetime: Temporal) extends TemporalAdjuster with TemporalAmount {

    def adjustInto(ignore: Temporal): Temporal = {
      datetime
    }

    def addTo(ignore: Temporal): Temporal = {
      datetime
    }

    def subtractFrom(ignore: Temporal): Temporal = {
      datetime
    }

    def getUnits: java.util.List[TemporalUnit] = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def get(unit: TemporalUnit): Long = {
      throw new UnsupportedOperationException("Not supported yet.")
    }
  }

  /** FixedPeriodUnit returns a fixed DateTime in all adjustments.
    * Construct an FixedPeriodUnit with the DateTime that should be returned from doPlus.
    */
  private[chrono] class FixedPeriodUnit private[chrono](private var dateTime: Temporal) extends TemporalUnit {

    override def toString: String = {
      "FixedPeriodUnit"
    }

    def getDuration: Duration = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isDurationEstimated: Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isDateBased: Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isTimeBased: Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isSupportedBy(dateTime: Temporal): Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def addTo[R <: Temporal](dateTime: R, periodToAdd: Long): R = {
      this.dateTime.asInstanceOf[R]
    }

    def between(temporal1: Temporal, temporal2: Temporal): Long = {
      throw new UnsupportedOperationException("Not supported yet.")
    }
  }

  /** FixedDateTimeField returns a fixed DateTime in all adjustments.
    * Construct an FixedDateTimeField with the DateTime that should be returned from adjustInto.
    */
  private[chrono] class FixedDateTimeField private[chrono](private var dateTime: Temporal) extends TemporalField {

    override def toString: String = {
      "FixedDateTimeField"
    }

    def getBaseUnit: TemporalUnit = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def getRangeUnit: TemporalUnit = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def range: ValueRange = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isDateBased: Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isTimeBased: Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def isSupportedBy(dateTime: TemporalAccessor): Boolean = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def rangeRefinedBy(dateTime: TemporalAccessor): ValueRange = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def getFrom(dateTime: TemporalAccessor): Long = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def adjustInto[R <: Temporal](dateTime: R, newValue: Long): R = {
      this.dateTime.asInstanceOf[R]
    }

    def getDisplayName(locale: Locale): String = {
      throw new UnsupportedOperationException("Not supported yet.")
    }

    def resolve(fieldValues: java.util.Map[TemporalField, java.lang.Long], partialTemporal: TemporalAccessor, resolverStyle: ResolverStyle): TemporalAccessor = {
      null
    }
  }

}

@Test class TestChronoZonedDateTime {
  @DataProvider(name = "calendars") private[chrono] def data_of_calendars: Array[Array[Chronology]] = {
    Array[Array[Chronology]](Array(HijrahChronology.INSTANCE), Array(IsoChronology.INSTANCE), Array(JapaneseChronology.INSTANCE), Array(MinguoChronology.INSTANCE), Array(ThaiBuddhistChronology.INSTANCE))
  }

  @Test(dataProvider = "calendars") def test_badWithAdjusterChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val czdt: ChronoZonedDateTime[_] = chrono.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val czdt2: ChronoZonedDateTime[_] = chrono2.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
      val adjuster: TemporalAdjuster = new TestChronoZonedDateTime.FixedAdjuster(czdt2)
      if (chrono ne chrono2) {
        try {
          czdt.`with`(adjuster)
          Assert.fail("WithAdjuster should have thrown a ClassCastException, " + "required: " + czdt + ", supplied: " + czdt2)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoZonedDateTime[_] = czdt.`with`(adjuster)
        assertEquals(result, czdt2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badPlusAdjusterChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val czdt: ChronoZonedDateTime[_] = chrono.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val czdt2: ChronoZonedDateTime[_] = chrono2.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
      val adjuster: TemporalAmount = new TestChronoZonedDateTime.FixedAdjuster(czdt2)
      if (chrono ne chrono2) {
        try {
          czdt.plus(adjuster)
          Assert.fail("WithAdjuster should have thrown a ClassCastException, " + "required: " + czdt + ", supplied: " + czdt2)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoZonedDateTime[_] = czdt.plus(adjuster)
        assertEquals(result, czdt2, "WithAdjuster failed to replace date time")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badMinusAdjusterChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val czdt: ChronoZonedDateTime[_] = chrono.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val czdt2: ChronoZonedDateTime[_] = chrono2.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
      val adjuster: TemporalAmount = new TestChronoZonedDateTime.FixedAdjuster(czdt2)
      if (chrono ne chrono2) {
        try {
          czdt.minus(adjuster)
          Assert.fail("WithAdjuster should have thrown a ClassCastException, " + "required: " + czdt + ", supplied: " + czdt2)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoZonedDateTime[_] = czdt.minus(adjuster)
        assertEquals(result, czdt2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badPlusPeriodUnitChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val czdt: ChronoZonedDateTime[_] = chrono.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val czdt2: ChronoZonedDateTime[_] = chrono2.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
      val adjuster: TemporalUnit = new TestChronoZonedDateTime.FixedPeriodUnit(czdt2)
      if (chrono ne chrono2) {
        try {
          czdt.plus(1, adjuster)
          Assert.fail("PeriodUnit.doPlus plus should have thrown a ClassCastException, " + czdt + " can not be cast to " + czdt2)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoZonedDateTime[_] = czdt.plus(1, adjuster)
        assertEquals(result, czdt2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badMinusPeriodUnitChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val czdt: ChronoZonedDateTime[_] = chrono.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val czdt2: ChronoZonedDateTime[_] = chrono2.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
      val adjuster: TemporalUnit = new TestChronoZonedDateTime.FixedPeriodUnit(czdt2)
      if (chrono ne chrono2) {
        try {
          czdt.minus(1, adjuster)
          Assert.fail("PeriodUnit.doPlus minus should have thrown a ClassCastException, " + czdt.getClass + " can not be cast to " + czdt2.getClass)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoZonedDateTime[_] = czdt.minus(1, adjuster)
        assertEquals(result, czdt2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badDateTimeFieldChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val czdt: ChronoZonedDateTime[_] = chrono.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val czdt2: ChronoZonedDateTime[_] = chrono2.date(refDate).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC)
      val adjuster: TemporalField = new TestChronoZonedDateTime.FixedDateTimeField(czdt2)
      if (chrono ne chrono2) {
        try {
          czdt.`with`(adjuster, 1)
          Assert.fail("DateTimeField adjustInto() should have thrown a ClassCastException, " + czdt.getClass + " can not be cast to " + czdt2.getClass)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoZonedDateTime[_] = czdt.`with`(adjuster, 1)
        assertEquals(result, czdt2, "DateTimeField adjustInto() failed to replace date")
      }
    }
  }

  @SuppressWarnings(Array("unused"))
  @Test(dataProvider = "calendars") def test_zonedDateTime_comparisons(chrono: Chronology): Unit = {
    val dates: java.util.List[ChronoZonedDateTime[_ <: ChronoLocalDate]] = new java.util.ArrayList[ChronoZonedDateTime[_ <: ChronoLocalDate]]
    val date: ChronoZonedDateTime[_ <: ChronoLocalDate] = chrono.date(LocalDate.of(1900, 1, 1)).atTime(LocalTime.MIN).atZone(ZoneOffset.UTC)
    if (chrono ne JapaneseChronology.INSTANCE)
      dates.add(date.minus(100, ChronoUnit.YEARS))
    dates.add(date.minus(1, ChronoUnit.YEARS))
    dates.add(date.minus(1, ChronoUnit.MONTHS))
    dates.add(date.minus(1, ChronoUnit.WEEKS))
    dates.add(date.minus(1, ChronoUnit.DAYS))
    dates.add(date.minus(1, ChronoUnit.HOURS))
    dates.add(date.minus(1, ChronoUnit.MINUTES))
    dates.add(date.minus(1, ChronoUnit.SECONDS))
    dates.add(date.minus(1, ChronoUnit.NANOS))
    dates.add(date)
    dates.add(date.plus(1, ChronoUnit.NANOS))
    dates.add(date.plus(1, ChronoUnit.SECONDS))
    dates.add(date.plus(1, ChronoUnit.MINUTES))
    dates.add(date.plus(1, ChronoUnit.HOURS))
    dates.add(date.plus(1, ChronoUnit.DAYS))
    dates.add(date.plus(1, ChronoUnit.WEEKS))
    dates.add(date.plus(1, ChronoUnit.MONTHS))
    dates.add(date.plus(1, ChronoUnit.YEARS))
    dates.add(date.plus(100, ChronoUnit.YEARS))
    for (clist <- data_of_calendars) {
      val otherDates: java.util.List[ChronoZonedDateTime[_ <: ChronoLocalDate]] = new java.util.ArrayList[ChronoZonedDateTime[_ <: ChronoLocalDate]]
      val chrono2: Chronology = IsoChronology.INSTANCE
      import scala.collection.JavaConversions._
      for (d <- dates) {
        otherDates.add(chrono2.date(d).atTime(d.toLocalTime).atZone(d.getZone))
      }
      var i: Int = 0
      while (i < dates.size) {
        val a: ChronoZonedDateTime[_ <: ChronoLocalDate] = dates.get(i)
        var j: Int = 0
        while (j < otherDates.size) {
          val b: ChronoZonedDateTime[_ <: ChronoLocalDate] = otherDates.get(j)
          val cmp: Int = ChronoZonedDateTime.timeLineOrder.compare(a, b)
          if (i < j) {
            assertTrue(cmp < 0, a + " compare " + b)
            assertEquals(a.isBefore(b), true, a + " isBefore " + b)
            assertEquals(a.isAfter(b), false, a + " ifAfter " + b)
            assertEquals(a.isEqual(b), false, a + " isEqual " + b)
          }
          else if (i > j) {
            assertTrue(cmp > 0, a + " compare " + b)
            assertEquals(a.isBefore(b), false, a + " isBefore " + b)
            assertEquals(a.isAfter(b), true, a + " ifAfter " + b)
            assertEquals(a.isEqual(b), false, a + " isEqual " + b)
          }
          else {
            assertTrue(cmp == 0, a + " compare " + b)
            assertEquals(a.isBefore(b), false, a + " isBefore " + b)
            assertEquals(a.isAfter(b), false, a + " ifAfter " + b)
            assertEquals(a.isEqual(b), true, a + " isEqual " + b)
          }
          j += 1
        }
        i += 1
      }
    }
  }

  @Test(dataProvider = "calendars")
  @throws(classOf[Exception])
  def test_ChronoZonedDateTimeSerialization(chrono: Chronology): Unit = {
    val ref: ZonedDateTime = LocalDate.of(2000, 1, 5).atTime(12, 1, 2, 3).atZone(ZoneId.of("GMT+01:23"))
    val orginal: ChronoZonedDateTime[_] = chrono.date(ref).atTime(ref.toLocalTime).atZone(ref.getZone)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val out: ObjectOutputStream = new ObjectOutputStream(baos)
    out.writeObject(orginal)
    out.close()
    val bais: ByteArrayInputStream = new ByteArrayInputStream(baos.toByteArray)
    val in: ObjectInputStream = new ObjectInputStream(bais)
    val ser: ChronoZonedDateTime[_] = in.readObject.asInstanceOf[ChronoZonedDateTime[_]]
    assertEquals(ser, orginal, "deserialized date is wrong")
  }
}
