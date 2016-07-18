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
object TestChronoLocalDate {

  /** FixedAdjusted returns a fixed DateTime in all adjustments.
    * Construct an adjuster with the DateTime that should be returned.
    */
  private[chrono] class FixedAdjuster private[chrono](private var datetime: Temporal) extends TemporalAdjuster with TemporalAmount {

    def adjustInto(ignore: Temporal): Temporal = datetime

    def addTo(ignore: Temporal): Temporal = datetime

    def subtractFrom(ignore: Temporal): Temporal = datetime

    def getUnits: java.util.List[TemporalUnit] =
      throw new UnsupportedOperationException("Not supported yet.")

    def get(unit: TemporalUnit): Long =
      throw new UnsupportedOperationException("Not supported yet.")
  }

  /** FixedPeriodUnit returns a fixed DateTime in all adjustments.
    * Construct an FixedPeriodUnit with the DateTime that should be returned.
    */
  private[chrono] class FixedPeriodUnit private[chrono](private var dateTime: Temporal) extends TemporalUnit {

    override def toString: String = "FixedPeriodUnit"

    def getDuration: Duration =
      throw new UnsupportedOperationException("Not supported yet.")

    def isDurationEstimated: Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    def isDateBased: Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    def isTimeBased: Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    def isSupportedBy(dateTime: Temporal): Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    @SuppressWarnings(Array("unchecked")) def addTo[R <: Temporal](dateTime: R, periodToAdd: Long): R =
      this.dateTime.asInstanceOf[R]

    def between(temporal1: Temporal, temporal2: Temporal): Long =
      throw new UnsupportedOperationException("Not supported yet.")
  }

  /** FixedDateTimeField returns a fixed DateTime in all adjustments.
    * Construct an FixedDateTimeField with the DateTime that should be returned from doSet.
    */
  private[chrono] class FixedDateTimeField private[chrono](private var dateTime: Temporal) extends TemporalField {

    override def toString: String = "FixedDateTimeField"

    def getBaseUnit: TemporalUnit =
      throw new UnsupportedOperationException("Not supported yet.")

    def getRangeUnit: TemporalUnit =
      throw new UnsupportedOperationException("Not supported yet.")

    def range: ValueRange =
      throw new UnsupportedOperationException("Not supported yet.")

    def isDateBased: Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    def isTimeBased: Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    def isSupportedBy(dateTime: TemporalAccessor): Boolean =
      throw new UnsupportedOperationException("Not supported yet.")

    def rangeRefinedBy(dateTime: TemporalAccessor): ValueRange =
      throw new UnsupportedOperationException("Not supported yet.")

    def getFrom(dateTime: TemporalAccessor): Long =
      throw new UnsupportedOperationException("Not supported yet.")

    @SuppressWarnings(Array("unchecked")) def adjustInto[R <: Temporal](dateTime: R, newValue: Long): R =
      this.dateTime.asInstanceOf[R]


    def getDisplayName(locale: Locale): String =
      throw new UnsupportedOperationException("Not supported yet.")

    def resolve(fieldValues: java.util.Map[TemporalField, java.lang.Long], partialTemporal: TemporalAccessor, resolverStyle: ResolverStyle): TemporalAccessor =
      null
  }

}

@Test class TestChronoLocalDate {
  @DataProvider(name = "calendars") private[chrono] def data_of_calendars: Array[Array[Chronology]] = {
    Array[Array[Chronology]](Array(HijrahChronology.INSTANCE), Array(IsoChronology.INSTANCE), Array(JapaneseChronology.INSTANCE), Array(MinguoChronology.INSTANCE), Array(ThaiBuddhistChronology.INSTANCE))
  }

  @Test(dataProvider = "calendars") def test_badWithAdjusterChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val date: ChronoLocalDate = chrono.date(refDate)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val date2: ChronoLocalDate = chrono2.date(refDate)
      val adjuster: TemporalAdjuster = new TestChronoLocalDate.FixedAdjuster(date2)
      if (chrono ne chrono2) {
        try {
          date.`with`(adjuster)
          Assert.fail("WithAdjuster should have thrown a ClassCastException")
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoLocalDate = date.`with`(adjuster)
        assertEquals(result, date2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badPlusAdjusterChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val date: ChronoLocalDate = chrono.date(refDate)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val date2: ChronoLocalDate = chrono2.date(refDate)
      val adjuster: TemporalAmount = new TestChronoLocalDate.FixedAdjuster(date2)
      if (chrono ne chrono2) {
        try {
          date.plus(adjuster)
          Assert.fail("WithAdjuster should have thrown a ClassCastException")
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoLocalDate = date.plus(adjuster)
        assertEquals(result, date2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badMinusAdjusterChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val date: ChronoLocalDate = chrono.date(refDate)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val date2: ChronoLocalDate = chrono2.date(refDate)
      val adjuster: TemporalAmount = new TestChronoLocalDate.FixedAdjuster(date2)
      if (chrono ne chrono2) {
        try {
          date.minus(adjuster)
          Assert.fail("WithAdjuster should have thrown a ClassCastException")
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoLocalDate = date.minus(adjuster)
        assertEquals(result, date2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badPlusPeriodUnitChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val date: ChronoLocalDate = chrono.date(refDate)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val date2: ChronoLocalDate = chrono2.date(refDate)
      val adjuster: TemporalUnit = new TestChronoLocalDate.FixedPeriodUnit(date2)
      if (chrono ne chrono2) {
        try {
          date.plus(1, adjuster)
          Assert.fail("PeriodUnit.doAdd plus should have thrown a ClassCastException" + date.getClass + ", can not be cast to " + date2.getClass)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoLocalDate = date.plus(1, adjuster)
        assertEquals(result, date2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badMinusPeriodUnitChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val date: ChronoLocalDate = chrono.date(refDate)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val date2: ChronoLocalDate = chrono2.date(refDate)
      val adjuster: TemporalUnit = new TestChronoLocalDate.FixedPeriodUnit(date2)
      if (chrono ne chrono2) {
        try {
          date.minus(1, adjuster)
          Assert.fail("PeriodUnit.doAdd minus should have thrown a ClassCastException" + date.getClass + ", can not be cast to " + date2.getClass)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoLocalDate = date.minus(1, adjuster)
        assertEquals(result, date2, "WithAdjuster failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_badDateTimeFieldChrono(chrono: Chronology): Unit = {
    val refDate: LocalDate = LocalDate.of(1900, 1, 1)
    val date: ChronoLocalDate = chrono.date(refDate)
    for (clist <- data_of_calendars) {
      val chrono2: Chronology = clist(0)
      val date2: ChronoLocalDate = chrono2.date(refDate)
      val adjuster: TemporalField = new TestChronoLocalDate.FixedDateTimeField(date2)
      if (chrono ne chrono2) {
        try {
          date.`with`(adjuster, 1)
          Assert.fail("DateTimeField doSet should have thrown a ClassCastException" + date.getClass + ", can not be cast to " + date2.getClass)
        }
        catch {
          case cce: ClassCastException =>
        }
      }
      else {
        val result: ChronoLocalDate = date.`with`(adjuster, 1)
        assertEquals(result, date2, "DateTimeField doSet failed to replace date")
      }
    }
  }

  @Test(dataProvider = "calendars") def test_date_comparisons(chrono: Chronology): Unit = {
    val dates: java.util.List[ChronoLocalDate] = new java.util.ArrayList[ChronoLocalDate]
    val date: ChronoLocalDate = chrono.date(LocalDate.of(1900, 1, 1))
    if (chrono ne JapaneseChronology.INSTANCE) {
      dates.add(date.minus(1000, ChronoUnit.YEARS))
      dates.add(date.minus(100, ChronoUnit.YEARS))
    }
    dates.add(date.minus(10, ChronoUnit.YEARS))
    dates.add(date.minus(1, ChronoUnit.YEARS))
    dates.add(date.minus(1, ChronoUnit.MONTHS))
    dates.add(date.minus(1, ChronoUnit.WEEKS))
    dates.add(date.minus(1, ChronoUnit.DAYS))
    dates.add(date)
    dates.add(date.plus(1, ChronoUnit.DAYS))
    dates.add(date.plus(1, ChronoUnit.WEEKS))
    dates.add(date.plus(1, ChronoUnit.MONTHS))
    dates.add(date.plus(1, ChronoUnit.YEARS))
    dates.add(date.plus(10, ChronoUnit.YEARS))
    dates.add(date.plus(100, ChronoUnit.YEARS))
    dates.add(date.plus(1000, ChronoUnit.YEARS))
    for (clist <- data_of_calendars) {
      scala.util.control.Breaks.breakable {
        val otherDates: java.util.List[ChronoLocalDate] = new java.util.ArrayList[ChronoLocalDate]
        val chrono2: Chronology = clist(0)
        if (chrono2 eq JapaneseChronology.INSTANCE)
          scala.util.control.Breaks.break()
        import scala.collection.JavaConversions._
        for (d <- dates) {
          otherDates.add(chrono2.date(d))
        }
        var i: Int = 0
        while (i < dates.size) {
          val a: ChronoLocalDate = dates.get(i)
          var j: Int = 0
          while (j < otherDates.size) {
            val b: ChronoLocalDate = otherDates.get(j)
            val cmp: Int = ChronoLocalDate.timeLineOrder.compare(a, b)
            if (i < j) {
              assertTrue(cmp < 0, a + " compare " + b)
              assertEquals(a.isBefore(b), true, a + " isBefore " + b)
              assertEquals(a.isAfter(b), false, a + " isAfter " + b)
              assertEquals(a.isEqual(b), false, a + " isEqual " + b)
            }
            else if (i > j) {
              assertTrue(cmp > 0, a + " compare " + b)
              assertEquals(a.isBefore(b), false, a + " isBefore " + b)
              assertEquals(a.isAfter(b), true, a + " isAfter " + b)
              assertEquals(a.isEqual(b), false, a + " isEqual " + b)
            }
            else {
              assertTrue(cmp == 0, a + " compare " + b)
              assertEquals(a.isBefore(b), false, a + " isBefore " + b)
              assertEquals(a.isAfter(b), false, a + " isAfter " + b)
              assertEquals(a.isEqual(b), true, a + " isEqual " + b)
            }
            j += 1
          }
          i += 1
        }
      }
    }
  }

  @Test(dataProvider = "calendars")
  @throws(classOf[Exception])
  def test_ChronoSerialization(chrono: Chronology): Unit = {
    val ref: LocalDate = LocalDate.of(1900, 1, 5)
    val orginal: ChronoLocalDate = chrono.date(ref)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val out: ObjectOutputStream = new ObjectOutputStream(baos)
    out.writeObject(orginal)
    out.close()
    val bais: ByteArrayInputStream = new ByteArrayInputStream(baos.toByteArray)
    val in: ObjectInputStream = new ObjectInputStream(bais)
    val ser: ChronoLocalDate = in.readObject.asInstanceOf[ChronoLocalDate]
    assertEquals(ser, orginal, "deserialized date is wrong")
  }
}
