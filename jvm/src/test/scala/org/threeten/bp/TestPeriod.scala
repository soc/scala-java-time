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

import org.testng.Assert.assertEquals
import org.testng.Assert.assertSame
import org.testng.Assert.assertTrue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import org.testng.SkipException
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/** Test. */
object TestPeriod {
  private def pymd(y: Int, m: Int, d: Int): Period = {
    Period.of(y, m, d)
  }

  private def date(y: Int, m: Int, d: Int): LocalDate = {
    LocalDate.of(y, m, d)
  }
}

@Test class TestPeriod {
  def test_interfaces(): Unit = {
    assertTrue(classOf[Serializable].isAssignableFrom(classOf[Period]))
  }

  @DataProvider(name = "serialization") private[bp] def data_serialization: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(Period.ZERO), Array(Period.ofDays(1)), Array(Period.of(1, 2, 3)))
  }

  @Test(dataProvider = "serialization")
  @throws(classOf[Exception])
  def test_serialization(period: Period): Unit = {
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val oos: ObjectOutputStream = new ObjectOutputStream(baos)
    oos.writeObject(period)
    oos.close()
    val ois: ObjectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    if (period.isZero) {
      assertSame(ois.readObject, period)
    }
    else {
      assertEquals(ois.readObject, period)
    }
  }

  @Test def test_immutable(): Unit = {
    throw new SkipException("private constructor shows up public due to companion object")
    AbstractTest.assertImmutable(classOf[Period])
  }

  def factory_zeroSingleton(): Unit = {
    assertSame(Period.ZERO, Period.ZERO)
    assertSame(Period.of(0, 0, 0), Period.ZERO)
    assertSame(Period.ofYears(0), Period.ZERO)
    assertSame(Period.ofMonths(0), Period.ZERO)
    assertSame(Period.ofDays(0), Period.ZERO)
  }

  def factory_of_ints(): Unit = {
    assertPeriod(Period.of(1, 2, 3), 1, 2, 3)
    assertPeriod(Period.of(0, 2, 3), 0, 2, 3)
    assertPeriod(Period.of(1, 0, 0), 1, 0, 0)
    assertPeriod(Period.of(0, 0, 0), 0, 0, 0)
    assertPeriod(Period.of(-1, -2, -3), -1, -2, -3)
  }

  def factory_ofYears(): Unit = {
    assertPeriod(Period.ofYears(1), 1, 0, 0)
    assertPeriod(Period.ofYears(0), 0, 0, 0)
    assertPeriod(Period.ofYears(-1), -1, 0, 0)
    assertPeriod(Period.ofYears(Int.MaxValue), Int.MaxValue, 0, 0)
    assertPeriod(Period.ofYears(Int.MinValue), Int.MinValue, 0, 0)
  }

  def factory_ofMonths(): Unit = {
    assertPeriod(Period.ofMonths(1), 0, 1, 0)
    assertPeriod(Period.ofMonths(0), 0, 0, 0)
    assertPeriod(Period.ofMonths(-1), 0, -1, 0)
    assertPeriod(Period.ofMonths(Int.MaxValue), 0, Int.MaxValue, 0)
    assertPeriod(Period.ofMonths(Int.MinValue), 0, Int.MinValue, 0)
  }

  def factory_ofDays(): Unit = {
    assertPeriod(Period.ofDays(1), 0, 0, 1)
    assertPeriod(Period.ofDays(0), 0, 0, 0)
    assertPeriod(Period.ofDays(-1), 0, 0, -1)
    assertPeriod(Period.ofDays(Int.MaxValue), 0, 0, Int.MaxValue)
    assertPeriod(Period.ofDays(Int.MinValue), 0, 0, Int.MinValue)
  }

  @DataProvider(name = "between") private[bp] def data_between: Array[Array[Any]] = {
    Array[Array[Any]](Array(2010, 1, 1, 2010, 1, 1, 0, 0, 0), Array(2010, 1, 1, 2010, 1, 2, 0, 0, 1), Array(2010, 1, 1, 2010, 1, 31, 0, 0, 30), Array(2010, 1, 1, 2010, 2, 1, 0, 1, 0), Array(2010, 1, 1, 2010, 2, 28, 0, 1, 27), Array(2010, 1, 1, 2010, 3, 1, 0, 2, 0), Array(2010, 1, 1, 2010, 12, 31, 0, 11, 30), Array(2010, 1, 1, 2011, 1, 1, 1, 0, 0), Array(2010, 1, 1, 2011, 12, 31, 1, 11, 30), Array(2010, 1, 1, 2012, 1, 1, 2, 0, 0), Array(2010, 1, 10, 2010, 1, 1, 0, 0, -9), Array(2010, 1, 10, 2010, 1, 2, 0, 0, -8), Array(2010, 1, 10, 2010, 1, 9, 0, 0, -1), Array(2010, 1, 10, 2010, 1, 10, 0, 0, 0), Array(2010, 1, 10, 2010, 1, 11, 0, 0, 1), Array(2010, 1, 10, 2010, 1, 31, 0, 0, 21), Array(2010, 1, 10, 2010, 2, 1, 0, 0, 22), Array(2010, 1, 10, 2010, 2, 9, 0, 0, 30), Array(2010, 1, 10, 2010, 2, 10, 0, 1, 0), Array(2010, 1, 10, 2010, 2, 28, 0, 1, 18), Array(2010, 1, 10, 2010, 3, 1, 0, 1, 19), Array(2010, 1, 10, 2010, 3, 9, 0, 1, 27), Array(2010, 1, 10, 2010, 3, 10, 0, 2, 0), Array(2010, 1, 10, 2010, 12, 31, 0, 11, 21), Array(2010, 1, 10, 2011, 1, 1, 0, 11, 22), Array(2010, 1, 10, 2011, 1, 9, 0, 11, 30), Array(2010, 1, 10, 2011, 1, 10, 1, 0, 0), Array(2010, 3, 30, 2011, 5, 1, 1, 1, 1), Array(2010, 4, 30, 2011, 5, 1, 1, 0, 1), Array(2010, 2, 28, 2012, 2, 27, 1, 11, 30), Array(2010, 2, 28, 2012, 2, 28, 2, 0, 0), Array(2010, 2, 28, 2012, 2, 29, 2, 0, 1), Array(2012, 2, 28, 2014, 2, 27, 1, 11, 30), Array(2012, 2, 28, 2014, 2, 28, 2, 0, 0), Array(2012, 2, 28, 2014, 3, 1, 2, 0, 1), Array(2012, 2, 29, 2014, 2, 28, 1, 11, 30), Array(2012, 2, 29, 2014, 3, 1, 2, 0, 1), Array(2012, 2, 29, 2014, 3, 2, 2, 0, 2), Array(2012, 2, 29, 2016, 2, 28, 3, 11, 30), Array(2012, 2, 29, 2016, 2, 29, 4, 0, 0), Array(2012, 2, 29, 2016, 3, 1, 4, 0, 1), Array(2010, 1, 1, 2009, 12, 31, 0, 0, -1), Array(2010, 1, 1, 2009, 12, 30, 0, 0, -2), Array(2010, 1, 1, 2009, 12, 2, 0, 0, -30), Array(2010, 1, 1, 2009, 12, 1, 0, -1, 0), Array(2010, 1, 1, 2009, 11, 30, 0, -1, -1), Array(2010, 1, 1, 2009, 11, 2, 0, -1, -29), Array(2010, 1, 1, 2009, 11, 1, 0, -2, 0), Array(2010, 1, 1, 2009, 1, 2, 0, -11, -30), Array(2010, 1, 1, 2009, 1, 1, -1, 0, 0), Array(2010, 1, 15, 2010, 1, 15, 0, 0, 0), Array(2010, 1, 15, 2010, 1, 14, 0, 0, -1), Array(2010, 1, 15, 2010, 1, 1, 0, 0, -14), Array(2010, 1, 15, 2009, 12, 31, 0, 0, -15), Array(2010, 1, 15, 2009, 12, 16, 0, 0, -30), Array(2010, 1, 15, 2009, 12, 15, 0, -1, 0), Array(2010, 1, 15, 2009, 12, 14, 0, -1, -1), Array(2010, 2, 28, 2009, 3, 1, 0, -11, -27), Array(2010, 2, 28, 2009, 2, 28, -1, 0, 0), Array(2010, 2, 28, 2009, 2, 27, -1, 0, -1), Array(2010, 2, 28, 2008, 2, 29, -1, -11, -28), Array(2010, 2, 28, 2008, 2, 28, -2, 0, 0), Array(2010, 2, 28, 2008, 2, 27, -2, 0, -1), Array(2012, 2, 29, 2009, 3, 1, -2, -11, -28), Array(2012, 2, 29, 2009, 2, 28, -3, 0, -1), Array(2012, 2, 29, 2009, 2, 27, -3, 0, -2), Array(2012, 2, 29, 2008, 3, 1, -3, -11, -28), Array(2012, 2, 29, 2008, 2, 29, -4, 0, 0), Array(2012, 2, 29, 2008, 2, 28, -4, 0, -1))
  }

  @Test(dataProvider = "between") def factory_between_LocalDate(y1: Int, m1: Int, d1: Int, y2: Int, m2: Int, d2: Int, ye: Int, me: Int, de: Int): Unit = {
    val start: LocalDate = LocalDate.of(y1, m1, d1)
    val end: LocalDate = LocalDate.of(y2, m2, d2)
    val test: Period = Period.between(start, end)
    assertPeriod(test, ye, me, de)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_between_LocalDate_nullFirst(): Unit = {
    Period.between(null.asInstanceOf[LocalDate], LocalDate.of(2010, 1, 1))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_between_LocalDate_nullSecond(): Unit = {
    Period.between(LocalDate.of(2010, 1, 1), null.asInstanceOf[LocalDate])
  }

  @DataProvider(name = "parse") private[bp] def data_parse: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("P0D", Period.ZERO), Array("P0W", Period.ZERO), Array("P0M", Period.ZERO), Array("P0Y", Period.ZERO), Array("P0Y0D", Period.ZERO), Array("P0Y0W", Period.ZERO), Array("P0Y0M", Period.ZERO), Array("P0M0D", Period.ZERO), Array("P0M0W", Period.ZERO), Array("P0W0D", Period.ZERO), Array("P1D", Period.ofDays(1)), Array("P2D", Period.ofDays(2)), Array("P-2D", Period.ofDays(-2)), Array("-P2D", Period.ofDays(-2)), Array("-P-2D", Period.ofDays(2)), Array("P" + Int.MaxValue + "D", Period.ofDays(Int.MaxValue)), Array("P" + Int.MinValue + "D", Period.ofDays(Int.MinValue)), Array("P1W", Period.ofDays(7)), Array("P2W", Period.ofDays(14)), Array("P-2W", Period.ofDays(-14)), Array("-P2W", Period.ofDays(-14)), Array("-P-2W", Period.ofDays(14)), Array("P1M", Period.ofMonths(1)), Array("P2M", Period.ofMonths(2)), Array("P-2M", Period.ofMonths(-2)), Array("-P2M", Period.ofMonths(-2)), Array("-P-2M", Period.ofMonths(2)), Array("P" + Int.MaxValue + "M", Period.ofMonths(Int.MaxValue)), Array("P" + Int.MinValue + "M", Period.ofMonths(Int.MinValue)), Array("P1Y", Period.ofYears(1)), Array("P2Y", Period.ofYears(2)), Array("P-2Y", Period.ofYears(-2)), Array("-P2Y", Period.ofYears(-2)), Array("-P-2Y", Period.ofYears(2)), Array("P" + Int.MaxValue + "Y", Period.ofYears(Int.MaxValue)), Array("P" + Int.MinValue + "Y", Period.ofYears(Int.MinValue)), Array("P1Y2M3W4D", Period.of(1, 2, 3 * 7 + 4)))
  }

  @Test(dataProvider = "parse") def test_parse(text: String, expected: Period): Unit = {
    assertEquals(Period.parse(text), expected)
  }

  @Test(dataProvider = "toStringAndParse") def test_parse_toString(test: Period, expected: String): Unit = {
    assertEquals(test, Period.parse(expected))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_parse_nullText(): Unit = {
    Period.parse(null.asInstanceOf[String])
  }

  def test_isZero(): Unit = {
    assertEquals(Period.of(1, 2, 3).isZero, false)
    assertEquals(Period.of(1, 0, 0).isZero, false)
    assertEquals(Period.of(0, 2, 0).isZero, false)
    assertEquals(Period.of(0, 0, 3).isZero, false)
    assertEquals(Period.of(0, 0, 0).isZero, true)
  }

  def test_isNegative(): Unit = {
    assertEquals(Period.of(0, 0, 0).isNegative, false)
    assertEquals(Period.of(1, 2, 3).isNegative, false)
    assertEquals(Period.of(1, 0, 0).isNegative, false)
    assertEquals(Period.of(0, 2, 0).isNegative, false)
    assertEquals(Period.of(0, 0, 3).isNegative, false)
    assertEquals(Period.of(-1, -2, -3).isNegative, true)
    assertEquals(Period.of(-1, 0, 0).isNegative, true)
    assertEquals(Period.of(0, -2, 0).isNegative, true)
    assertEquals(Period.of(0, 0, -3).isNegative, true)
    assertEquals(Period.of(-1, 2, 3).isNegative, true)
    assertEquals(Period.of(1, -2, 3).isNegative, true)
    assertEquals(Period.of(1, 2, -3).isNegative, true)
  }

  def test_withYears(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.withYears(10), 10, 2, 3)
  }

  def test_withYears_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.withYears(1), test)
  }

  def test_withYears_toZero(): Unit = {
    val test: Period = Period.ofYears(1)
    assertSame(test.withYears(0), Period.ZERO)
  }

  def test_withMonths(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.withMonths(10), 1, 10, 3)
  }

  def test_withMonths_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.withMonths(2), test)
  }

  def test_withMonths_toZero(): Unit = {
    val test: Period = Period.ofMonths(1)
    assertSame(test.withMonths(0), Period.ZERO)
  }

  def test_withDays(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.withDays(10), 1, 2, 10)
  }

  def test_withDays_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.withDays(3), test)
  }

  def test_withDays_toZero(): Unit = {
    val test: Period = Period.ofDays(1)
    assertSame(test.withDays(0), Period.ZERO)
  }

  @DataProvider(name = "plus") private[bp] def data_plus: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(5, 0, 0), TestPeriod.pymd(5, 0, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(-5, 0, 0), TestPeriod.pymd(-5, 0, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 5, 0), TestPeriod.pymd(0, 5, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, -5, 0), TestPeriod.pymd(0, -5, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, 5), TestPeriod.pymd(0, 0, 5)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, -5), TestPeriod.pymd(0, 0, -5)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(2, 3, 4), TestPeriod.pymd(2, 3, 4)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(-2, -3, -4), TestPeriod.pymd(-2, -3, -4)), Array(TestPeriod.pymd(4, 5, 6), TestPeriod.pymd(2, 3, 4), TestPeriod.pymd(6, 8, 10)), Array(TestPeriod.pymd(4, 5, 6), TestPeriod.pymd(-2, -3, -4), TestPeriod.pymd(2, 2, 2)))
  }

  @Test(dataProvider = "plus") def test_plus(base: Period, add: Period, expected: Period): Unit = {
    assertEquals(base.plus(add), expected)
  }

  def test_plusYears(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.plusYears(10), 11, 2, 3)
    assertPeriod(test.plus(Period.ofYears(10)), 11, 2, 3)
  }

  def test_plusYears_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.plusYears(0), test)
    assertPeriod(test.plus(Period.ofYears(0)), 1, 2, 3)
  }

  def test_plusYears_toZero(): Unit = {
    val test: Period = Period.ofYears(-1)
    assertSame(test.plusYears(1), Period.ZERO)
    assertSame(test.plus(Period.ofYears(1)), Period.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusYears_overflowTooBig(): Unit = {
    val test: Period = Period.ofYears(Int.MaxValue)
    test.plusYears(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusYears_overflowTooSmall(): Unit = {
    val test: Period = Period.ofYears(Int.MinValue)
    test.plusYears(-1)
  }

  def test_plusMonths(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.plusMonths(10), 1, 12, 3)
    assertPeriod(test.plus(Period.ofMonths(10)), 1, 12, 3)
  }

  def test_plusMonths_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.plusMonths(0), test)
    assertEquals(test.plus(Period.ofMonths(0)), test)
  }

  def test_plusMonths_toZero(): Unit = {
    val test: Period = Period.ofMonths(-1)
    assertSame(test.plusMonths(1), Period.ZERO)
    assertSame(test.plus(Period.ofMonths(1)), Period.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusMonths_overflowTooBig(): Unit = {
    val test: Period = Period.ofMonths(Int.MaxValue)
    test.plusMonths(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusMonths_overflowTooSmall(): Unit = {
    val test: Period = Period.ofMonths(Int.MinValue)
    test.plusMonths(-1)
  }

  def test_plusDays(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.plusDays(10), 1, 2, 13)
  }

  def test_plusDays_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.plusDays(0), test)
  }

  def test_plusDays_toZero(): Unit = {
    val test: Period = Period.ofDays(-1)
    assertSame(test.plusDays(1), Period.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusDays_overflowTooBig(): Unit = {
    val test: Period = Period.ofDays(Int.MaxValue)
    test.plusDays(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusDays_overflowTooSmall(): Unit = {
    val test: Period = Period.ofDays(Int.MinValue)
    test.plusDays(-1)
  }

  @DataProvider(name = "minus") private[bp] def data_minus: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(5, 0, 0), TestPeriod.pymd(-5, 0, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(-5, 0, 0), TestPeriod.pymd(5, 0, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 5, 0), TestPeriod.pymd(0, -5, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, -5, 0), TestPeriod.pymd(0, 5, 0)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, 5), TestPeriod.pymd(0, 0, -5)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(0, 0, -5), TestPeriod.pymd(0, 0, 5)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(2, 3, 4), TestPeriod.pymd(-2, -3, -4)), Array(TestPeriod.pymd(0, 0, 0), TestPeriod.pymd(-2, -3, -4), TestPeriod.pymd(2, 3, 4)), Array(TestPeriod.pymd(4, 5, 6), TestPeriod.pymd(2, 3, 4), TestPeriod.pymd(2, 2, 2)), Array(TestPeriod.pymd(4, 5, 6), TestPeriod.pymd(-2, -3, -4), TestPeriod.pymd(6, 8, 10)))
  }

  @Test(dataProvider = "minus") def test_minus(base: Period, subtract: Period, expected: Period): Unit = {
    assertEquals(base.minus(subtract), expected)
  }

  def test_minusYears(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.minusYears(10), -9, 2, 3)
  }

  def test_minusYears_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.minusYears(0), test)
  }

  def test_minusYears_toZero(): Unit = {
    val test: Period = Period.ofYears(1)
    assertSame(test.minusYears(1), Period.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusYears_overflowTooBig(): Unit = {
    val test: Period = Period.ofYears(Int.MaxValue)
    test.minusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusYears_overflowTooSmall(): Unit = {
    val test: Period = Period.ofYears(Int.MinValue)
    test.minusYears(1)
  }

  def test_minusMonths(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.minusMonths(10), 1, -8, 3)
  }

  def test_minusMonths_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.minusMonths(0), test)
  }

  def test_minusMonths_toZero(): Unit = {
    val test: Period = Period.ofMonths(1)
    assertSame(test.minusMonths(1), Period.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusMonths_overflowTooBig(): Unit = {
    val test: Period = Period.ofMonths(Int.MaxValue)
    test.minusMonths(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusMonths_overflowTooSmall(): Unit = {
    val test: Period = Period.ofMonths(Int.MinValue)
    test.minusMonths(1)
  }

  def test_minusDays(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.minusDays(10), 1, 2, -7)
  }

  def test_minusDays_noChange(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.minusDays(0), test)
  }

  def test_minusDays_toZero(): Unit = {
    val test: Period = Period.ofDays(1)
    assertSame(test.minusDays(1), Period.ZERO)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusDays_overflowTooBig(): Unit = {
    val test: Period = Period.ofDays(Int.MaxValue)
    test.minusDays(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusDays_overflowTooSmall(): Unit = {
    val test: Period = Period.ofDays(Int.MinValue)
    test.minusDays(1)
  }

  def test_multipliedBy(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.multipliedBy(2), 2, 4, 6)
    assertPeriod(test.multipliedBy(-3), -3, -6, -9)
  }

  def test_multipliedBy_zeroBase(): Unit = {
    assertSame(Period.ZERO.multipliedBy(2), Period.ZERO)
  }

  def test_multipliedBy_zero(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.multipliedBy(0), Period.ZERO)
  }

  def test_multipliedBy_one(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertSame(test.multipliedBy(1), test)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_multipliedBy_overflowTooBig(): Unit = {
    val test: Period = Period.ofYears(Int.MaxValue / 2 + 1)
    test.multipliedBy(2)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_multipliedBy_overflowTooSmall(): Unit = {
    val test: Period = Period.ofYears(Int.MinValue / 2 - 1)
    test.multipliedBy(2)
  }

  def test_negated(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertPeriod(test.negated, -1, -2, -3)
  }

  def test_negated_zero(): Unit = {
    assertSame(Period.ZERO.negated, Period.ZERO)
  }

  def test_negated_max(): Unit = {
    assertPeriod(Period.ofYears(Int.MaxValue).negated, -Int.MaxValue, 0, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_negated_overflow(): Unit = {
    Period.ofYears(Int.MinValue).negated
  }

  @DataProvider(name = "normalized") private[bp] def data_normalized: Array[Array[Any]] = {
    Array[Array[Any]](Array(0, 0, 0, 0), Array(1, 0, 1, 0), Array(-1, 0, -1, 0), Array(1, 1, 1, 1), Array(1, 2, 1, 2), Array(1, 11, 1, 11), Array(1, 12, 2, 0), Array(1, 13, 2, 1), Array(1, 23, 2, 11), Array(1, 24, 3, 0), Array(1, 25, 3, 1), Array(1, -1, 0, 11), Array(1, -2, 0, 10), Array(1, -11, 0, 1), Array(1, -12, 0, 0), Array(1, -13, 0, -1), Array(1, -23, 0, -11), Array(1, -24, -1, 0), Array(1, -25, -1, -1), Array(1, -35, -1, -11), Array(1, -36, -2, 0), Array(1, -37, -2, -1), Array(-1, 1, 0, -11), Array(-1, 11, 0, -1), Array(-1, 12, 0, 0), Array(-1, 13, 0, 1), Array(-1, 23, 0, 11), Array(-1, 24, 1, 0), Array(-1, 25, 1, 1), Array(-1, -1, -1, -1), Array(-1, -11, -1, -11), Array(-1, -12, -2, 0), Array(-1, -13, -2, -1))
  }

  @Test(dataProvider = "normalized") def test_normalized(inputYears: Int, inputMonths: Int, expectedYears: Int, expectedMonths: Int): Unit = {
    assertPeriod(Period.of(inputYears, inputMonths, 0).normalized, expectedYears, expectedMonths, 0)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_normalizedMonthsISO_min(): Unit = {
    val base: Period = Period.of(Int.MinValue, -12, 0)
    base.normalized
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_normalizedMonthsISO_max(): Unit = {
    val base: Period = Period.of(Int.MaxValue, 12, 0)
    base.normalized
  }

  @DataProvider(name = "addTo") private[bp] def data_addTo: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(TestPeriod.pymd(0, 0, 0), TestPeriod.date(2012, 6, 30), TestPeriod.date(2012, 6, 30)), Array(TestPeriod.pymd(1, 0, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2013, 6, 10)), Array(TestPeriod.pymd(0, 1, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 7, 10)), Array(TestPeriod.pymd(0, 0, 1), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 6, 11)), Array(TestPeriod.pymd(-1, 0, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2011, 6, 10)), Array(TestPeriod.pymd(0, -1, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 5, 10)), Array(TestPeriod.pymd(0, 0, -1), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 6, 9)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 6, 27), TestPeriod.date(2013, 8, 30)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 6, 28), TestPeriod.date(2013, 8, 31)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 6, 29), TestPeriod.date(2013, 9, 1)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 6, 30), TestPeriod.date(2013, 9, 2)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 7, 1), TestPeriod.date(2013, 9, 4)), Array(TestPeriod.pymd(1, 0, 0), TestPeriod.date(2011, 2, 28), TestPeriod.date(2012, 2, 28)), Array(TestPeriod.pymd(4, 0, 0), TestPeriod.date(2011, 2, 28), TestPeriod.date(2015, 2, 28)), Array(TestPeriod.pymd(1, 0, 0), TestPeriod.date(2012, 2, 29), TestPeriod.date(2013, 2, 28)), Array(TestPeriod.pymd(4, 0, 0), TestPeriod.date(2012, 2, 29), TestPeriod.date(2016, 2, 29)), Array(TestPeriod.pymd(1, 1, 0), TestPeriod.date(2011, 1, 29), TestPeriod.date(2012, 2, 29)), Array(TestPeriod.pymd(1, 2, 0), TestPeriod.date(2012, 2, 29), TestPeriod.date(2013, 4, 29)))
  }

  @Test(dataProvider = "addTo") def test_addTo(period: Period, baseDate: LocalDate, expected: LocalDate): Unit = {
    assertEquals(period.addTo(baseDate), expected)
  }

  @Test(dataProvider = "addTo") def test_addTo_usingLocalDatePlus(period: Period, baseDate: LocalDate, expected: LocalDate): Unit = {
    assertEquals(baseDate.plus(period), expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addTo_nullZero(): Unit = {
    Period.ZERO.addTo(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_addTo_nullNonZero(): Unit = {
    Period.ofDays(2).addTo(null)
  }

  @DataProvider(name = "subtractFrom") private[bp] def data_subtractFrom: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(TestPeriod.pymd(0, 0, 0), TestPeriod.date(2012, 6, 30), TestPeriod.date(2012, 6, 30)), Array(TestPeriod.pymd(1, 0, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2011, 6, 10)), Array(TestPeriod.pymd(0, 1, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 5, 10)), Array(TestPeriod.pymd(0, 0, 1), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 6, 9)), Array(TestPeriod.pymd(-1, 0, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2013, 6, 10)), Array(TestPeriod.pymd(0, -1, 0), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 7, 10)), Array(TestPeriod.pymd(0, 0, -1), TestPeriod.date(2012, 6, 10), TestPeriod.date(2012, 6, 11)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 8, 30), TestPeriod.date(2011, 6, 27)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 8, 31), TestPeriod.date(2011, 6, 27)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 9, 1), TestPeriod.date(2011, 6, 28)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 9, 2), TestPeriod.date(2011, 6, 29)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 9, 3), TestPeriod.date(2011, 6, 30)), Array(TestPeriod.pymd(1, 2, 3), TestPeriod.date(2012, 9, 4), TestPeriod.date(2011, 7, 1)), Array(TestPeriod.pymd(1, 0, 0), TestPeriod.date(2011, 2, 28), TestPeriod.date(2010, 2, 28)), Array(TestPeriod.pymd(4, 0, 0), TestPeriod.date(2011, 2, 28), TestPeriod.date(2007, 2, 28)), Array(TestPeriod.pymd(1, 0, 0), TestPeriod.date(2012, 2, 29), TestPeriod.date(2011, 2, 28)), Array(TestPeriod.pymd(4, 0, 0), TestPeriod.date(2012, 2, 29), TestPeriod.date(2008, 2, 29)), Array(TestPeriod.pymd(1, 1, 0), TestPeriod.date(2013, 3, 29), TestPeriod.date(2012, 2, 29)), Array(TestPeriod.pymd(1, 2, 0), TestPeriod.date(2012, 2, 29), TestPeriod.date(2010, 12, 29)))
  }

  @Test(dataProvider = "subtractFrom") def test_subtractFrom(period: Period, baseDate: LocalDate, expected: LocalDate): Unit = {
    assertEquals(period.subtractFrom(baseDate), expected)
  }

  @Test(dataProvider = "subtractFrom") def test_subtractFrom_usingLocalDateMinus(period: Period, baseDate: LocalDate, expected: LocalDate): Unit = {
    assertEquals(baseDate.minus(period), expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_subtractFrom_nullZero(): Unit = {
    Period.ZERO.subtractFrom(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_subtractFrom_nullNonZero(): Unit = {
    Period.ofDays(2).subtractFrom(null)
  }

  def test_equals(): Unit = {
    assertEquals(Period.of(1, 0, 0) == Period.ofYears(1), true)
    assertEquals(Period.of(0, 1, 0) == Period.ofMonths(1), true)
    assertEquals(Period.of(0, 0, 1) == Period.ofDays(1), true)
    assertEquals(Period.of(1, 2, 3) == Period.of(1, 2, 3), true)
    assertEquals(Period.ofYears(1) == Period.ofYears(1), true)
    assertEquals(Period.ofYears(1) == Period.ofYears(2), false)
    assertEquals(Period.ofMonths(1) == Period.ofMonths(1), true)
    assertEquals(Period.ofMonths(1) == Period.ofMonths(2), false)
    assertEquals(Period.ofDays(1) == Period.ofDays(1), true)
    assertEquals(Period.ofDays(1) == Period.ofDays(2), false)
    assertEquals(Period.of(1, 2, 3) == Period.of(1, 2, 3), true)
    assertEquals(Period.of(1, 2, 3) == Period.of(0, 2, 3), false)
    assertEquals(Period.of(1, 2, 3) == Period.of(1, 0, 3), false)
    assertEquals(Period.of(1, 2, 3) == Period.of(1, 2, 0), false)
  }

  def test_equals_self(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertEquals(test == test, true)
  }

  def test_equals_null(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertEquals(test == null, false)
  }

  def test_equals_otherClass(): Unit = {
    val test: Period = Period.of(1, 2, 3)
    assertEquals(test == "", false)
  }

  def test_hashCode(): Unit = {
    val test5: Period = Period.ofDays(5)
    val test6: Period = Period.ofDays(6)
    val test5M: Period = Period.ofMonths(5)
    val test5Y: Period = Period.ofYears(5)
    assertEquals(test5.hashCode == test5.hashCode, true)
    assertEquals(test5.hashCode == test6.hashCode, false)
    assertEquals(test5.hashCode == test5M.hashCode, false)
    assertEquals(test5.hashCode == test5Y.hashCode, false)
  }

  @DataProvider(name = "toStringAndParse") private[bp] def data_toString: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(Period.ZERO, "P0D"), Array(Period.ofDays(0), "P0D"), Array(Period.ofYears(1), "P1Y"), Array(Period.ofMonths(1), "P1M"), Array(Period.ofDays(1), "P1D"), Array(Period.of(1, 2, 3), "P1Y2M3D"))
  }

  @Test(dataProvider = "toStringAndParse") def test_toString(input: Period, expected: String): Unit = {
    assertEquals(input.toString, expected)
  }

  private def assertPeriod(test: Period, y: Int, mo: Int, d: Int): Unit = {
    assertEquals(test.getYears, y, "years")
    assertEquals(test.getMonths, mo, "months")
    assertEquals(test.getDays, d, "days")
  }
}
