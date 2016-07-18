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
package org.threeten.bp.format

import org.testng.Assert.assertEquals
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import java.util.Locale
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.MockFieldValue
import org.threeten.bp.temporal.TemporalField

/** Test TextPrinterParser. */
object TestTextPrinter {
  private val PROVIDER: DateTimeTextProvider = DateTimeTextProvider.getInstance
}

@Test class TestTextPrinter extends AbstractTestPrinterParser {
  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_emptyCalendrical(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextPrinter.PROVIDER)
    pp.print(printEmptyContext, buf)
  }

  @throws(classOf[Exception])
  def test_print_append(): Unit = {
    printContext.setDateTime(LocalDate.of(2012, 4, 18))
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(DAY_OF_WEEK, TextStyle.FULL, TestTextPrinter.PROVIDER)
    buf.append("EXISTING")
    pp.print(printContext, buf)
    assertEquals(buf.toString, "EXISTINGWednesday")
  }

  @DataProvider(name = "print") private[format] def provider_dow: Array[Array[Any]] = {
    Array[Array[Any]](Array(DAY_OF_WEEK, TextStyle.FULL, 1, "Monday"), Array(DAY_OF_WEEK, TextStyle.FULL, 2, "Tuesday"), Array(DAY_OF_WEEK, TextStyle.FULL, 3, "Wednesday"), Array(DAY_OF_WEEK, TextStyle.FULL, 4, "Thursday"), Array(DAY_OF_WEEK, TextStyle.FULL, 5, "Friday"), Array(DAY_OF_WEEK, TextStyle.FULL, 6, "Saturday"), Array(DAY_OF_WEEK, TextStyle.FULL, 7, "Sunday"), Array(DAY_OF_WEEK, TextStyle.SHORT, 1, "Mon"), Array(DAY_OF_WEEK, TextStyle.SHORT, 2, "Tue"), Array(DAY_OF_WEEK, TextStyle.SHORT, 3, "Wed"), Array(DAY_OF_WEEK, TextStyle.SHORT, 4, "Thu"), Array(DAY_OF_WEEK, TextStyle.SHORT, 5, "Fri"), Array(DAY_OF_WEEK, TextStyle.SHORT, 6, "Sat"), Array(DAY_OF_WEEK, TextStyle.SHORT, 7, "Sun"), Array(DAY_OF_MONTH, TextStyle.FULL, 1, "1"), Array(DAY_OF_MONTH, TextStyle.FULL, 2, "2"), Array(DAY_OF_MONTH, TextStyle.FULL, 3, "3"), Array(DAY_OF_MONTH, TextStyle.FULL, 28, "28"), Array(DAY_OF_MONTH, TextStyle.FULL, 29, "29"), Array(DAY_OF_MONTH, TextStyle.FULL, 30, "30"), Array(DAY_OF_MONTH, TextStyle.FULL, 31, "31"), Array(DAY_OF_MONTH, TextStyle.SHORT, 1, "1"), Array(DAY_OF_MONTH, TextStyle.SHORT, 2, "2"), Array(DAY_OF_MONTH, TextStyle.SHORT, 3, "3"), Array(DAY_OF_MONTH, TextStyle.SHORT, 28, "28"), Array(DAY_OF_MONTH, TextStyle.SHORT, 29, "29"), Array(DAY_OF_MONTH, TextStyle.SHORT, 30, "30"), Array(DAY_OF_MONTH, TextStyle.SHORT, 31, "31"), Array(MONTH_OF_YEAR, TextStyle.FULL, 1, "January"), Array(MONTH_OF_YEAR, TextStyle.FULL, 12, "December"), Array(MONTH_OF_YEAR, TextStyle.SHORT, 1, "Jan"), Array(MONTH_OF_YEAR, TextStyle.SHORT, 12, "Dec"))
  }

  @Test(dataProvider = "print")
  @throws(classOf[Exception])
  def test_print(field: TemporalField, style: TextStyle, value: Int, expected: String): Unit = {
    printContext.setDateTime(new MockFieldValue(field, value))
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(field, style, TestTextPrinter.PROVIDER)
    pp.print(printContext, buf)
    assertEquals(buf.toString, expected)
  }

  @throws(classOf[Exception])
  def test_print_french_long(): Unit = {
    printContext.setLocale(Locale.FRENCH)
    printContext.setDateTime(LocalDate.of(2012, 1, 1))
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextPrinter.PROVIDER)
    pp.print(printContext, buf)
    assertEquals(buf.toString, "janvier")
  }

  @throws(classOf[Exception])
  def test_print_french_short(): Unit = {
    printContext.setLocale(Locale.FRENCH)
    printContext.setDateTime(LocalDate.of(2012, 1, 1))
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextPrinter.PROVIDER)
    pp.print(printContext, buf)
    assertEquals(buf.toString, "janv.")
  }

  @throws(classOf[Exception])
  def test_toString1(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.FULL, TestTextPrinter.PROVIDER)
    assertEquals(pp.toString, "Text(MonthOfYear)")
  }

  @throws(classOf[Exception])
  def test_toString2(): Unit = {
    val pp: DateTimeFormatterBuilder.TextPrinterParser = new DateTimeFormatterBuilder.TextPrinterParser(MONTH_OF_YEAR, TextStyle.SHORT, TestTextPrinter.PROVIDER)
    assertEquals(pp.toString, "Text(MonthOfYear,SHORT)")
  }
}
