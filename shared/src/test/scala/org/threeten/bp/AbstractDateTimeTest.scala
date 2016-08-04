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
import org.testng.annotations.Test
import org.testng.Assert.fail
import org.threeten.bp.temporal.{TemporalAccessor, TemporalField, TemporalQuery}

/** Base test class for {@code DateTime}. */
abstract class AbstractDateTimeTest {
  /** Sample {@code DateTime} objects.
    *
    * @return the objects, not null
    */
  protected def samples: java.util.List[TemporalAccessor]

  /** List of valid supported fields.
    *
    * @return the fields, not null
    */
  protected def validFields: java.util.List[TemporalField]

  /** List of invalid unsupported fields.
    *
    * @return the fields, not null
    */
  protected def invalidFields: java.util.List[TemporalField]

  @Test def basicTest_isSupported_DateTimeField_supported(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      import scala.collection.JavaConversions._
      for (field <- validFields) {
        assertEquals(sample.isSupported(field), true, "Failed on " + sample + " " + field)
      }
    }
  }

  @Test def basicTest_isSupported_DateTimeField_unsupported(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      import scala.collection.JavaConversions._
      for (field <- invalidFields) {
        assertEquals(sample.isSupported(field), false, "Failed on " + sample + " " + field)
      }
    }
  }

  @Test def basicTest_isSupported_DateTimeField_null(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      assertEquals(sample.isSupported(null), false, "Failed on " + sample)
    }
  }

  @Test def basicTest_range_DateTimeField_unsupported(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      import scala.collection.JavaConversions._
      for (field <- invalidFields) {
        try {
          sample.range(field)
          fail("Failed on " + sample + " " + field)
        }
        catch {
          case ex: DateTimeException =>
        }
      }
    }
  }

  @Test def basicTest_range_DateTimeField_null(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      try {
        sample.range(null)
        fail("Failed on " + sample)
      }
      catch {
        case ex: NullPointerException =>
      }
    }
  }

  @Test def basicTest_get_DateTimeField_unsupported(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      import scala.collection.JavaConversions._
      for (field <- invalidFields) {
        try {
          sample.get(field)
          fail("Failed on " + sample + " " + field)
        }
        catch {
          case ex: DateTimeException =>
        }
      }
    }
  }

  @Test def basicTest_get_DateTimeField_null(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      try {
        sample.get(null)
        fail("Failed on " + sample)
      }
      catch {
        case ex: NullPointerException =>
      }
    }
  }

  @Test def basicTest_getLong_DateTimeField_unsupported(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      import scala.collection.JavaConversions._
      for (field <- invalidFields) {
        try {
          sample.getLong(field)
          fail("Failed on " + sample + " " + field)
        }
        catch {
          case ex: DateTimeException =>
        }
      }
    }
  }

  @Test def basicTest_getLong_DateTimeField_null(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      try {
        sample.getLong(null)
        fail("Failed on " + sample)
      }
      catch {
        case ex: NullPointerException =>
      }
    }
  }

  @Test def basicTest_query(): Unit = {
    import scala.collection.JavaConversions._
    for (sample <- samples) {
      assertEquals(sample.query(new TemporalQuery[String] {
        override def queryFrom(temporal: TemporalAccessor): String = "foo"
      }), "foo")
    }
  }
}
