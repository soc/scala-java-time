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
package org.threeten.bp.zone

import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertTrue
import java.util.Collections
import org.testng.annotations.Test
import org.threeten.bp.ZoneOffset

/** Test ZoneRulesProvider. */
object TestZoneRulesProvider {

  private[zone] class MockTempProvider extends ZoneRulesProvider {
    private[zone] final val rules: ZoneRules = ZoneOffset.of("+01:45").getRules

    def provideZoneIds: java.util.Set[String] = {
      new java.util.HashSet[String](Collections.singleton("FooLocation"))
    }

    protected def provideVersions(zoneId: String): java.util.NavigableMap[String, ZoneRules] = {
      val result: java.util.NavigableMap[String, ZoneRules] = new java.util.TreeMap[String, ZoneRules]
      result.put("BarVersion", rules)
      result
    }

    protected def provideRules(zoneId: String, forCaching: Boolean): ZoneRules = {
      if (zoneId == "FooLocation") {
        return rules
      }
      throw new ZoneRulesException("Invalid")
    }
  }

}

@Test class TestZoneRulesProvider {
  @Test def test_getAvailableGroupIds(): Unit = {
    val zoneIds: java.util.Set[String] = ZoneRulesProvider.getAvailableZoneIds
    assertEquals(zoneIds.contains("Europe/London"), true)
    zoneIds.clear()
    assertEquals(zoneIds.size, 0)
    val zoneIds2: java.util.Set[String] = ZoneRulesProvider.getAvailableZoneIds
    assertEquals(zoneIds2.contains("Europe/London"), true)
  }

  @Test def test_getRules_String(): Unit = {
    val rules: ZoneRules = ZoneRulesProvider.getRules("Europe/London", false)
    assertNotNull(rules)
    val rules2: ZoneRules = ZoneRulesProvider.getRules("Europe/London", false)
    assertEquals(rules2, rules)
  }

  @Test(expectedExceptions = Array(classOf[ZoneRulesException])) def test_getRules_String_unknownId(): Unit = {
    ZoneRulesProvider.getRules("Europe/Lon", false)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getRules_String_null(): Unit = {
    ZoneRulesProvider.getRules(null, false)
  }

  @Test def test_getVersions_String(): Unit = {
    val versions: java.util.NavigableMap[String, ZoneRules] = ZoneRulesProvider.getVersions("Europe/London")
    assertTrue(versions.size >= 1)
    val rules: ZoneRules = ZoneRulesProvider.getRules("Europe/London", false)
    assertEquals(versions.lastEntry.getValue, rules)
    val copy: java.util.NavigableMap[String, ZoneRules] = new java.util.TreeMap[String, ZoneRules](versions)
    versions.clear()
    assertEquals(versions.size, 0)
    val versions2: java.util.NavigableMap[String, ZoneRules] = ZoneRulesProvider.getVersions("Europe/London")
    assertEquals(versions2, copy)
  }

  @Test(expectedExceptions = Array(classOf[ZoneRulesException])) def test_getVersions_String_unknownId(): Unit = {
    ZoneRulesProvider.getVersions("Europe/Lon")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getVersions_String_null(): Unit = {
    ZoneRulesProvider.getVersions(null)
  }

  @Test def test_refresh(): Unit = {
    assertEquals(ZoneRulesProvider.refresh, false)
  }

  @Test def test_registerProvider(): Unit = {
    val pre: java.util.Set[String] = ZoneRulesProvider.getAvailableZoneIds
    assertEquals(pre.contains("FooLocation"), false)
    ZoneRulesProvider.registerProvider(new TestZoneRulesProvider.MockTempProvider)
    assertEquals(pre.contains("FooLocation"), false)
    val post: java.util.Set[String] = ZoneRulesProvider.getAvailableZoneIds
    assertEquals(post.contains("FooLocation"), true)
    assertEquals(ZoneRulesProvider.getRules("FooLocation", false), ZoneOffset.of("+01:45").getRules)
  }
}
