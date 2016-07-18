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
import org.threeten.bp.temporal.ChronoField.AMPM_OF_DAY
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import java.util.Locale
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.ChronoField

/** Test SimpleDateTimeTextProvider. */
@Test class TestSimpleDateTimeTextProvider {
  private val enUS: Locale = new Locale("en", "US")
  private val ptBR: Locale = new Locale("pt", "BR")
  private val frFR: Locale = new Locale("fr", "FR")

  @BeforeMethod def setUp(): Unit = {}

  @DataProvider(name = "Text") private[format] def data_text: Array[Array[Any]] =
    Array[Array[Any]](
      Array(DAY_OF_WEEK, 1, TextStyle.SHORT, enUS, "Mon"), Array(DAY_OF_WEEK, 2, TextStyle.SHORT, enUS, "Tue"), Array(DAY_OF_WEEK, 3, TextStyle.SHORT, enUS, "Wed"), Array(DAY_OF_WEEK, 4, TextStyle.SHORT, enUS, "Thu"), Array(DAY_OF_WEEK, 5, TextStyle.SHORT, enUS, "Fri"), Array(DAY_OF_WEEK, 6, TextStyle.SHORT, enUS, "Sat"), Array(DAY_OF_WEEK, 7, TextStyle.SHORT, enUS, "Sun"),
      Array(DAY_OF_WEEK, 1, TextStyle.SHORT, ptBR, "Seg"), Array(DAY_OF_WEEK, 2, TextStyle.SHORT, ptBR, "Ter"), Array(DAY_OF_WEEK, 3, TextStyle.SHORT, ptBR, "Qua"), Array(DAY_OF_WEEK, 4, TextStyle.SHORT, ptBR, "Qui"), Array(DAY_OF_WEEK, 5, TextStyle.SHORT, ptBR, "Sex"), Array(DAY_OF_WEEK, 6, TextStyle.SHORT, ptBR, "S\u00E1b"), Array(DAY_OF_WEEK, 7, TextStyle.SHORT, ptBR, "Dom"),
      Array(DAY_OF_WEEK, 1, TextStyle.FULL, enUS, "Monday"), Array(DAY_OF_WEEK, 2, TextStyle.FULL, enUS, "Tuesday"), Array(DAY_OF_WEEK, 3, TextStyle.FULL, enUS, "Wednesday"), Array(DAY_OF_WEEK, 4, TextStyle.FULL, enUS, "Thursday"), Array(DAY_OF_WEEK, 5, TextStyle.FULL, enUS, "Friday"), Array(DAY_OF_WEEK, 6, TextStyle.FULL, enUS, "Saturday"), Array(DAY_OF_WEEK, 7, TextStyle.FULL, enUS, "Sunday"),
      Array(DAY_OF_WEEK, 1, TextStyle.FULL, ptBR, "Segunda-feira"), Array(DAY_OF_WEEK, 2, TextStyle.FULL, ptBR, "Ter\u00E7a-feira"), Array(DAY_OF_WEEK, 3, TextStyle.FULL, ptBR, "Quarta-feira"), Array(DAY_OF_WEEK, 4, TextStyle.FULL, ptBR, "Quinta-feira"), Array(DAY_OF_WEEK, 5, TextStyle.FULL, ptBR, "Sexta-feira"), Array(DAY_OF_WEEK, 6, TextStyle.FULL, ptBR, "S\u00E1bado"), Array(DAY_OF_WEEK, 7, TextStyle.FULL, ptBR, "Domingo"),
      Array(MONTH_OF_YEAR, 1, TextStyle.SHORT, enUS, "Jan"), Array(MONTH_OF_YEAR, 2, TextStyle.SHORT, enUS, "Feb"), Array(MONTH_OF_YEAR, 3, TextStyle.SHORT, enUS, "Mar"), Array(MONTH_OF_YEAR, 4, TextStyle.SHORT, enUS, "Apr"), Array(MONTH_OF_YEAR, 5, TextStyle.SHORT, enUS, "May"), Array(MONTH_OF_YEAR, 6, TextStyle.SHORT, enUS, "Jun"), Array(MONTH_OF_YEAR, 7, TextStyle.SHORT, enUS, "Jul"), Array(MONTH_OF_YEAR, 8, TextStyle.SHORT, enUS, "Aug"), Array(MONTH_OF_YEAR, 9, TextStyle.SHORT, enUS, "Sep"), Array(MONTH_OF_YEAR, 10, TextStyle.SHORT, enUS, "Oct"), Array(MONTH_OF_YEAR, 11, TextStyle.SHORT, enUS, "Nov"), Array(MONTH_OF_YEAR, 12, TextStyle.SHORT, enUS, "Dec"),
      Array(MONTH_OF_YEAR, 1, TextStyle.SHORT, frFR, "janv."), Array(MONTH_OF_YEAR, 2, TextStyle.SHORT, frFR, "f\u00E9vr."), Array(MONTH_OF_YEAR, 3, TextStyle.SHORT, frFR, "mars"), Array(MONTH_OF_YEAR, 4, TextStyle.SHORT, frFR, "avr."), Array(MONTH_OF_YEAR, 5, TextStyle.SHORT, frFR, "mai"), Array(MONTH_OF_YEAR, 6, TextStyle.SHORT, frFR, "juin"), Array(MONTH_OF_YEAR, 7, TextStyle.SHORT, frFR, "juil."), Array(MONTH_OF_YEAR, 8, TextStyle.SHORT, frFR, "ao\u00FBt"), Array(MONTH_OF_YEAR, 9, TextStyle.SHORT, frFR, "sept."), Array(MONTH_OF_YEAR, 10, TextStyle.SHORT, frFR, "oct."), Array(MONTH_OF_YEAR, 11, TextStyle.SHORT, frFR, "nov."), Array(MONTH_OF_YEAR, 12, TextStyle.SHORT, frFR, "d\u00E9c."),
      Array(MONTH_OF_YEAR, 1, TextStyle.FULL, enUS, "January"), Array(MONTH_OF_YEAR, 2, TextStyle.FULL, enUS, "February"), Array(MONTH_OF_YEAR, 3, TextStyle.FULL, enUS, "March"), Array(MONTH_OF_YEAR, 4, TextStyle.FULL, enUS, "April"), Array(MONTH_OF_YEAR, 5, TextStyle.FULL, enUS, "May"), Array(MONTH_OF_YEAR, 6, TextStyle.FULL, enUS, "June"), Array(MONTH_OF_YEAR, 7, TextStyle.FULL, enUS, "July"), Array(MONTH_OF_YEAR, 8, TextStyle.FULL, enUS, "August"), Array(MONTH_OF_YEAR, 9, TextStyle.FULL, enUS, "September"), Array(MONTH_OF_YEAR, 10, TextStyle.FULL, enUS, "October"), Array(MONTH_OF_YEAR, 11, TextStyle.FULL, enUS, "November"), Array(MONTH_OF_YEAR, 12, TextStyle.FULL, enUS, "December"),
      Array(MONTH_OF_YEAR, 1, TextStyle.FULL, ptBR, "Janeiro"), Array(MONTH_OF_YEAR, 2, TextStyle.FULL, ptBR, "Fevereiro"), Array(MONTH_OF_YEAR, 3, TextStyle.FULL, ptBR, "Mar\u00E7o"), Array(MONTH_OF_YEAR, 4, TextStyle.FULL, ptBR, "Abril"), Array(MONTH_OF_YEAR, 5, TextStyle.FULL, ptBR, "Maio"), Array(MONTH_OF_YEAR, 6, TextStyle.FULL, ptBR, "Junho"), Array(MONTH_OF_YEAR, 7, TextStyle.FULL, ptBR, "Julho"), Array(MONTH_OF_YEAR, 8, TextStyle.FULL, ptBR, "Agosto"), Array(MONTH_OF_YEAR, 9, TextStyle.FULL, ptBR, "Setembro"), Array(MONTH_OF_YEAR, 10, TextStyle.FULL, ptBR, "Outubro"), Array(MONTH_OF_YEAR, 11, TextStyle.FULL, ptBR, "Novembro"), Array(MONTH_OF_YEAR, 12, TextStyle.FULL, ptBR, "Dezembro"),
      Array(AMPM_OF_DAY, 0, TextStyle.SHORT, enUS, "AM"), Array(AMPM_OF_DAY, 1, TextStyle.SHORT, enUS, "PM"))

  @Test(dataProvider = "Text") def test_getText(field: TemporalField, value: Number, style: TextStyle, locale: Locale, expected: String): Unit = {
    val tp: DateTimeTextProvider = DateTimeTextProvider.getInstance

    // Work around difference between JRE locale data and CLDR data:
    // JRE specifies capitalized brazilian month and day-of-week names,
    // while CLDR specifies lower-cased names.
    if (locale == ptBR && (field == ChronoField.MONTH_OF_YEAR || field == ChronoField.DAY_OF_WEEK))
      assertEquals(tp.getText(field, value.longValue, style, locale).capitalize, expected)
    else
      assertEquals(tp.getText(field, value.longValue, style, locale), expected)
  }
}
