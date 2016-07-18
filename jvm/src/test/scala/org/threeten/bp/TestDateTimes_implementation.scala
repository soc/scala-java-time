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
import org.testng.Assert.assertTrue
import java.lang.reflect.Modifier
import java.util.Collections
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import java.lang.{Long => JLong}

/** Test. */
@Test class TestDateTimes_implementation {

  @Test(enabled = false) // this originally tested Jdk8Methods ...
  def test_constructor(): Unit = {
    for (constructor <- classOf[Math].getDeclaredConstructors) {
      assertTrue(Modifier.isPrivate(constructor.getModifiers))
      constructor.setAccessible(true)
      constructor.newInstance(Collections.nCopies(constructor.getParameterTypes.length, null).toArray)
    }
  }

  @DataProvider(name = "safeAddIntProvider") private[bp] def safeAddIntProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE, 1, Integer.MIN_VALUE + 1), Array[Integer](-1, 1, 0), Array[Integer](0, 0, 0), Array[Integer](1, -1, 0), Array[Integer](Integer.MAX_VALUE, -1, Integer.MAX_VALUE - 1))

  @Test(dataProvider = "safeAddIntProvider") def test_safeAddInt(a: Int, b: Int, expected: Int): Unit =
    assertEquals(Math.addExact(a, b), expected)

  @DataProvider(name = "safeAddIntProviderOverflow") private[bp] def safeAddIntProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE, -1), Array[Integer](Integer.MIN_VALUE + 1, -2), Array[Integer](Integer.MAX_VALUE - 1, 2), Array[Integer](Integer.MAX_VALUE, 1))

  @Test(dataProvider = "safeAddIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeAddInt_overflow(a: Int, b: Int): Unit =
    Math.addExact(a, b)

  @DataProvider(name = "safeAddLongProvider") private[bp] def safeAddLongProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue, 1, Long.MinValue + 1), Array[JLong](-1, 1, 0), Array[JLong](0, 0, 0), Array[JLong](1, -1, 0), Array[JLong](Long.MaxValue, -1, Long.MaxValue - 1))

  @Test(dataProvider = "safeAddLongProvider") def test_safeAddLong(a: Long, b: Long, expected: Long): Unit =
    assertEquals(Math.addExact(a, b), expected)

  @DataProvider(name = "safeAddLongProviderOverflow") private[bp] def safeAddLongProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue, -1), Array[JLong](Long.MinValue + 1, -2), Array[JLong](Long.MaxValue - 1, 2), Array[JLong](Long.MaxValue, 1))

  @Test(dataProvider = "safeAddLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeAddLong_overflow(a: Long, b: Long): Unit =
    Math.addExact(a, b)

  @DataProvider(name = "safeSubtractIntProvider") private[bp] def safeSubtractIntProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE, -1, Integer.MIN_VALUE + 1), Array[Integer](-1, -1, 0), Array[Integer](0, 0, 0), Array[Integer](1, 1, 0), Array[Integer](Integer.MAX_VALUE, 1, Integer.MAX_VALUE - 1))

  @Test(dataProvider = "safeSubtractIntProvider") def test_safeSubtractInt(a: Int, b: Int, expected: Int): Unit =
    assertEquals(Math.subtractExact(a, b), expected)

  @DataProvider(name = "safeSubtractIntProviderOverflow") private[bp] def safeSubtractIntProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE, 1), Array[Integer](Integer.MIN_VALUE + 1, 2), Array[Integer](Integer.MAX_VALUE - 1, -2), Array[Integer](Integer.MAX_VALUE, -1))

  @Test(dataProvider = "safeSubtractIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeSubtractInt_overflow(a: Int, b: Int): Unit =
    Math.subtractExact(a, b)

  @DataProvider(name = "safeSubtractLongProvider") private[bp] def safeSubtractLongProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue, -1, Long.MinValue + 1), Array[JLong](-1, -1, 0), Array[JLong](0, 0, 0), Array[JLong](1, 1, 0), Array[JLong](Long.MaxValue, 1, Long.MaxValue - 1))

  @Test(dataProvider = "safeSubtractLongProvider") def test_safeSubtractLong(a: Long, b: Long, expected: Long): Unit =
    assertEquals(Math.subtractExact(a, b), expected)

  @DataProvider(name = "safeSubtractLongProviderOverflow") private[bp] def safeSubtractLongProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue, 1), Array[JLong](Long.MinValue + 1, 2), Array[JLong](Long.MaxValue - 1, -2), Array[JLong](Long.MaxValue, -1))

  @Test(dataProvider = "safeSubtractLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeSubtractLong_overflow(a: Long, b: Long): Unit =
    Math.subtractExact(a, b)

  @DataProvider(name = "safeMultiplyIntProvider") private[bp] def safeMultiplyIntProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE, 1, Integer.MIN_VALUE), Array[Integer](Integer.MIN_VALUE / 2, 2, Integer.MIN_VALUE), Array[Integer](-1, -1, 1), Array[Integer](-1, 1, -1), Array[Integer](0, -1, 0), Array[Integer](0, 0, 0), Array[Integer](0, 1, 0), Array[Integer](1, -1, -1), Array[Integer](1, 1, 1), Array[Integer](Integer.MAX_VALUE / 2, 2, Integer.MAX_VALUE - 1), Array[Integer](Integer.MAX_VALUE, -1, Integer.MIN_VALUE + 1))

  @Test(dataProvider = "safeMultiplyIntProvider") def test_safeMultiplyInt(a: Int, b: Int, expected: Int): Unit =
    assertEquals(Math.multiplyExact(a, b), expected)

  @DataProvider(name = "safeMultiplyIntProviderOverflow") private[bp] def safeMultiplyIntProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE, 2), Array[Integer](Integer.MIN_VALUE / 2 - 1, 2), Array[Integer](Integer.MAX_VALUE, 2), Array[Integer](Integer.MAX_VALUE / 2 + 1, 2), Array[Integer](Integer.MIN_VALUE, -1), Array[Integer](-1, Integer.MIN_VALUE))

  @Test(dataProvider = "safeMultiplyIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeMultiplyInt_overflow(a: Int, b: Int): Unit =
    Math.multiplyExact(a, b)

  @DataProvider(name = "safeMultiplyLongProvider") private[bp] def safeMultiplyLongProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array(Long.MinValue: JLong, 1: Integer, Long.MinValue: JLong), Array((Long.MinValue / 2): JLong, 2: Integer, Long.MinValue: JLong), Array(-1: JLong, -1: Integer, 1: JLong), Array(-1: JLong, 1: Integer, -1: JLong), Array(0: JLong, -1: Integer, 0: JLong), Array(0: JLong, 0: Integer, 0: JLong), Array(0: JLong, 1: Integer, 0: JLong), Array(1: JLong, -1: Integer, -1: JLong), Array(1: JLong, 1: Integer, 1: JLong), Array((Long.MaxValue / 2): JLong, 2: Integer, (Long.MaxValue - 1): JLong), Array(Long.MaxValue: JLong, -1: Integer, (Long.MinValue + 1): JLong), Array(-1: JLong, Integer.MIN_VALUE: Integer, -Integer.MIN_VALUE.toLong: JLong))

  @Test(dataProvider = "safeMultiplyLongProvider") def test_safeMultiplyLong(a: Long, b: Int, expected: Long): Unit =
    assertEquals(Math.multiplyExact(a, b), expected)

  @DataProvider(name = "safeMultiplyLongProviderOverflow") private[bp] def safeMultiplyLongProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array(Long.MinValue: JLong, 2: Integer), Array((Long.MinValue / 2 - 1): JLong, 2: Integer), Array(Long.MaxValue: JLong, 2: Integer), Array((Long.MaxValue / 2 + 1): JLong, 2: Integer), Array(Long.MinValue: JLong, -1: Integer))

  @Test(dataProvider = "safeMultiplyLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeMultiplyLong_overflow(a: Long, b: Int): Unit =
    Math.multiplyExact(a, b)

  @DataProvider(name = "safeMultiplyLongLongProvider") private[bp] def safeMultiplyLongLongProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue, 1, Long.MinValue), Array[JLong](Long.MinValue / 2, 2, Long.MinValue), Array[JLong](-1, -1, 1), Array[JLong](-1, 1, -1), Array[JLong](0, -1, 0), Array[JLong](0, 0, 0), Array[JLong](0, 1, 0), Array[JLong](1, -1, -1), Array[JLong](1, 1, 1), Array[JLong](Long.MaxValue / 2, 2, Long.MaxValue - 1), Array[JLong](Long.MaxValue, -1, Long.MinValue + 1))

  @Test(dataProvider = "safeMultiplyLongLongProvider") def test_safeMultiplyLongLong(a: Long, b: Long, expected: Long): Unit =
    assertEquals(Math.multiplyExact(a, b), expected)

  @DataProvider(name = "safeMultiplyLongLongProviderOverflow") private[bp] def safeMultiplyLongLongProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue, 2), Array[JLong](Long.MinValue / 2 - 1, 2), Array[JLong](Long.MaxValue, 2), Array[JLong](Long.MaxValue / 2 + 1, 2), Array[JLong](Long.MinValue, -1), Array[JLong](-1, Long.MinValue))

  @Test(dataProvider = "safeMultiplyLongLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeMultiplyLongLong_overflow(a: Long, b: Long): Unit =
    Math.multiplyExact(a, b)

  @DataProvider(name = "safeToIntProvider") private[bp] def safeToIntProvider: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[Integer](Integer.MIN_VALUE), Array[Integer](Integer.MIN_VALUE + 1), Array[Integer](-1), Array[Integer](0), Array[Integer](1), Array[Integer](Integer.MAX_VALUE - 1), Array[Integer](Integer.MAX_VALUE))

  @Test(dataProvider = "safeToIntProvider") def test_safeToInt(l: Long): Unit =
    assertEquals(Math.toIntExact(l), l)

  @DataProvider(name = "safeToIntProviderOverflow") private[bp] def safeToIntProviderOverflow: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array[JLong](Long.MinValue), Array[JLong](Integer.MIN_VALUE - 1L), Array[JLong](Integer.MAX_VALUE + 1L), Array[JLong](Long.MaxValue))

  @Test(dataProvider = "safeToIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeToInt_overflow(l: Long): Unit =
    Math.toIntExact(l)

  def test_safeCompare_int(): Unit =
    doTest_safeCompare_int(Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, -2, -1, 0, 1, 2, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE)

  private def doTest_safeCompare_int(values: Int*): Unit = {
    var i: Int = 0
    while (i < values.length) {
      val a: Int = values(i)
      var j: Int = 0
      while (j < values.length) {
        val b: Int = values(j)
        assertEquals(Integer.compare(a, b), if (a < b) -1 else if (a > b) 1 else 0, a + " <=> " + b)
        j += 1
      }
      i += 1
    }
  }

  def test_safeCompare_long(): Unit =
    doTest_safeCompare_long(Long.MinValue, Long.MinValue + 1, Long.MinValue + 2, Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, -2, -1, 0, 1, 2, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Long.MaxValue - 2, Long.MaxValue - 1, Long.MaxValue)

  private def doTest_safeCompare_long(values: Long*): Unit = {
    var i: Int = 0
    while (i < values.length) {
      val a: Long = values(i)
      var j: Int = 0
      while (j < values.length) {
        val b: Long = values(j)
        assertEquals(java.lang.Long.compare(a, b), if (a < b) -1 else if (a > b) 1 else 0, a + " <=> " + b)
        j += 1
      }
      i += 1
    }
  }

  @DataProvider(name = "FloorDiv") private[bp] def data_floorDiv: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array(5L: JLong, 4: Integer, 1L: JLong), Array(4L: JLong, 4: Integer, 1L: JLong), Array(3L: JLong, 4: Integer, 0L: JLong), Array(2L: JLong, 4: Integer, 0L: JLong), Array(1L: JLong, 4: Integer, 0L: JLong), Array(0L: JLong, 4: Integer, 0L: JLong), Array(-1L: JLong, 4: Integer, -1L: JLong), Array(-2L: JLong, 4: Integer, -1L: JLong), Array(-3L: JLong, 4: Integer, -1L: JLong), Array(-4L: JLong, 4: Integer, -1L: JLong), Array(-5L: JLong, 4: Integer, -2L: JLong))

  @Test(dataProvider = "FloorDiv") def test_floorDiv_long(a: Long, b: Int, expected: Long): Unit =
    assertEquals(Math.floorDiv(a, b), expected)

  @Test(dataProvider = "FloorDiv") def test_floorDiv_int(a: Long, b: Int, expected: Long): Unit = {
    if (a <= Integer.MAX_VALUE && a >= Integer.MIN_VALUE) {
      assertEquals(Math.floorDiv(a.toInt, b), expected.toInt)
    }
  }

  @DataProvider(name = "FloorMod") private[bp] def data_floorMod: Array[Array[_ <: AnyRef]] =
    Array[Array[_ <: AnyRef]](Array(5L: JLong, 4: Integer, 1: Integer), Array(4L: JLong, 4: Integer, 0: Integer), Array(3L: JLong, 4: Integer, 3: Integer), Array(2L: JLong, 4: Integer, 2: Integer), Array(1L: JLong, 4: Integer, 1: Integer), Array(0L: JLong, 4: Integer, 0: Integer), Array(-1L: JLong, 4: Integer, 3: Integer), Array(-2L: JLong, 4: Integer, 2: Integer), Array(-3L: JLong, 4: Integer, 1: Integer), Array(-4L: JLong, 4: Integer, 0: Integer), Array(-5L: JLong, 4: Integer, 3: Integer))

  @Test(dataProvider = "FloorMod") def test_floorMod_long(a: Long, b: Long, expected: Int): Unit =
    assertEquals(Math.floorMod(a, b), expected)

  @Test(dataProvider = "FloorMod") def test_floorMod_long(a: Long, b: Int, expected: Int): Unit =
    assertEquals(Math.floorMod(a, b), expected)

  @Test(dataProvider = "FloorMod") def test_floorMod_int(a: Long, b: Int, expected: Int): Unit = {
    if (a <= Integer.MAX_VALUE && a >= Integer.MIN_VALUE) {
      assertEquals(Math.floorMod(a.toInt, b), expected)
    }
  }
}
