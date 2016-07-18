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
package org.threeten.bp.temporal

import org.testng.Assert.assertEquals
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import org.testng.SkipException
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.AbstractTest

/** Test. */
@Test class TestValueRange {
  @Test def test_immutable(): Unit = {
    throw new SkipException("private constructor shows up public due to companion object")
    AbstractTest.assertImmutable(classOf[ValueRange])
  }

  @throws(classOf[Exception])
  def test_serialization(): Unit = {
    val obj: AnyRef = ValueRange.of(1, 2, 3, 4)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val oos: ObjectOutputStream = new ObjectOutputStream(baos)
    oos.writeObject(obj)
    oos.close()
    val ois: ObjectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    assertEquals(ois.readObject, obj)
  }

  def test_of_longlong(): Unit = {
    val test: ValueRange = ValueRange.of(1, 12)
    assertEquals(test.getMinimum, 1)
    assertEquals(test.getLargestMinimum, 1)
    assertEquals(test.getSmallestMaximum, 12)
    assertEquals(test.getMaximum, 12)
    assertEquals(test.isFixed, true)
    assertEquals(test.isIntValue, true)
  }

  def test_of_longlong_big(): Unit = {
    val test: ValueRange = ValueRange.of(1, 123456789012345L)
    assertEquals(test.getMinimum, 1)
    assertEquals(test.getLargestMinimum, 1)
    assertEquals(test.getSmallestMaximum, 123456789012345L)
    assertEquals(test.getMaximum, 123456789012345L)
    assertEquals(test.isFixed, true)
    assertEquals(test.isIntValue, false)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_of_longlong_minGtMax(): Unit = {
    ValueRange.of(12, 1)
  }

  def test_of_longlonglong(): Unit = {
    val test: ValueRange = ValueRange.of(1, 28, 31)
    assertEquals(test.getMinimum, 1)
    assertEquals(test.getLargestMinimum, 1)
    assertEquals(test.getSmallestMaximum, 28)
    assertEquals(test.getMaximum, 31)
    assertEquals(test.isFixed, false)
    assertEquals(test.isIntValue, true)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_of_longlonglong_minGtMax(): Unit = {
    ValueRange.of(12, 1, 2)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_of_longlonglong_smallestmaxminGtMax(): Unit = {
    ValueRange.of(1, 31, 28)
  }

  @DataProvider(name = "valid") private[temporal] def data_valid: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, 1, 1, 1), Array(1, 1, 1, 2), Array(1, 1, 2, 2), Array(1, 2, 3, 4), Array(1, 1, 28, 31), Array(1, 3, 31, 31), Array(-5, -4, -3, -2), Array(-5, -4, 3, 4), Array(1, 20, 10, 31))
  }

  @Test(dataProvider = "valid") def test_of_longlonglonglong(sMin: Long, lMin: Long, sMax: Long, lMax: Long): Unit = {
    val test: ValueRange = ValueRange.of(sMin, lMin, sMax, lMax)
    assertEquals(test.getMinimum, sMin)
    assertEquals(test.getLargestMinimum, lMin)
    assertEquals(test.getSmallestMaximum, sMax)
    assertEquals(test.getMaximum, lMax)
    assertEquals(test.isFixed, sMin == lMin && sMax == lMax)
    assertEquals(test.isIntValue, true)
  }

  @DataProvider(name = "invalid") private[temporal] def data_invalid: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, 2, 31, 28), Array(1, 31, 2, 28), Array(31, 2, 1, 28), Array(31, 2, 3, 28), Array(2, 1, 28, 31), Array(2, 1, 31, 28), Array(12, 13, 1, 2))
  }

  @Test(dataProvider = "invalid", expectedExceptions = Array(classOf[IllegalArgumentException])) def test_of_longlonglonglong_invalid(sMin: Long, lMin: Long, sMax: Long, lMax: Long): Unit = {
    ValueRange.of(sMin, lMin, sMax, lMax)
  }

  def test_isValidValue_long(): Unit = {
    val test: ValueRange = ValueRange.of(1, 28, 31)
    assertEquals(test.isValidValue(0), false)
    assertEquals(test.isValidValue(1), true)
    assertEquals(test.isValidValue(2), true)
    assertEquals(test.isValidValue(30), true)
    assertEquals(test.isValidValue(31), true)
    assertEquals(test.isValidValue(32), false)
  }

  def test_isValidValue_long_int(): Unit = {
    val test: ValueRange = ValueRange.of(1, 28, 31)
    assertEquals(test.isValidValue(0), false)
    assertEquals(test.isValidValue(1), true)
    assertEquals(test.isValidValue(31), true)
    assertEquals(test.isValidValue(32), false)
  }

  def test_isValidValue_long_long(): Unit = {
    val test: ValueRange = ValueRange.of(1, 28, Int.MaxValue + 1L)
    assertEquals(test.isValidIntValue(0), false)
    assertEquals(test.isValidIntValue(1), false)
    assertEquals(test.isValidIntValue(31), false)
    assertEquals(test.isValidIntValue(32), false)
  }

  def test_equals1(): Unit = {
    val a: ValueRange = ValueRange.of(1, 2, 3, 4)
    val b: ValueRange = ValueRange.of(1, 2, 3, 4)
    assertEquals(a == a, true)
    assertEquals(a == b, true)
    assertEquals(b == a, true)
    assertEquals(b == b, true)
    assertEquals(a.hashCode == b.hashCode, true)
  }

  def test_equals2(): Unit = {
    val a: ValueRange = ValueRange.of(1, 2, 3, 4)
    assertEquals(a == ValueRange.of(0, 2, 3, 4), false)
    assertEquals(a == ValueRange.of(1, 3, 3, 4), false)
    assertEquals(a == ValueRange.of(1, 2, 4, 4), false)
    assertEquals(a == ValueRange.of(1, 2, 3, 5), false)
  }

  def test_equals_otherType(): Unit = {
    val a: ValueRange = ValueRange.of(1, 12)
    assertEquals(a == "Rubbish", false)
  }

  def test_equals_null(): Unit = {
    val a: ValueRange = ValueRange.of(1, 12)
    assertEquals(a == null, false)
  }

  def test_toString(): Unit = {
    assertEquals(ValueRange.of(1, 1, 4, 4).toString, "1 - 4")
    assertEquals(ValueRange.of(1, 1, 3, 4).toString, "1 - 3/4")
    assertEquals(ValueRange.of(1, 2, 3, 4).toString, "1/2 - 3/4")
    assertEquals(ValueRange.of(1, 2, 4, 4).toString, "1/2 - 4")
  }
}
