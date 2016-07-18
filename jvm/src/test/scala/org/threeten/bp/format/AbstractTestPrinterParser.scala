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

import java.util.Locale
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.DateTimeException
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalField

import scala.annotation.meta.setter

/** Abstract PrinterParser test. */
object AbstractTestPrinterParser {
  private val EMPTY: TemporalAccessor = new TemporalAccessor() {
    def isSupported(field: TemporalField): Boolean = true
    def getLong(field: TemporalField): Long = throw new DateTimeException("Mock")
  }
}

// TODO: what's going on here?
@Test class AbstractTestPrinterParser {
  @(Test @setter)(enabled = false)
  protected var printEmptyContext: DateTimePrintContext = null
  @(Test @setter)(enabled = false)
  protected var printContext: DateTimePrintContext = null
  @(Test @setter)(enabled = false)
  protected var parseContext: DateTimeParseContext = null
  @(Test @setter)(enabled = false)
  protected var buf: java.lang.StringBuilder = null

  @BeforeMethod def setUp(): Unit = {
    printEmptyContext = new DateTimePrintContext(AbstractTestPrinterParser.EMPTY, Locale.ENGLISH, DecimalStyle.STANDARD)
    val zdt: ZonedDateTime = LocalDateTime.of(2011, 6, 30, 12, 30, 40, 0).atZone(ZoneId.of("Europe/Paris"))
    printContext = new DateTimePrintContext(zdt, Locale.ENGLISH, DecimalStyle.STANDARD)
    parseContext = new DateTimeParseContext(Locale.ENGLISH, DecimalStyle.STANDARD, IsoChronology.INSTANCE)
    buf = new java.lang.StringBuilder
  }
}
