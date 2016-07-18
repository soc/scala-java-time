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

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.StreamCorruptedException
import java.net.URL
import java.util.{Objects, Arrays}
import java.lang.Iterable
import java.util.concurrent.ConcurrentNavigableMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicReferenceArray

private[zone] object TzdbZoneRulesProvider {

  /** A version of the TZDB rules. */
  private[zone] class Version private[zone](private[zone] val versionId: String,
                                            private val regionArray: Array[String],
                                            private val ruleIndices: Array[Short],
                                            private val ruleData: AtomicReferenceArray[AnyRef]) {

    private[zone] def getRules(regionId: String): ZoneRules = {
      val regionIndex: Int = Arrays.binarySearch(regionArray.asInstanceOf[Array[AnyRef]], regionId)
      if (regionIndex < 0)
        return null
      try createRule(ruleIndices(regionIndex))
      catch {
        case ex: Exception =>
          throw new ZoneRulesException(s"Invalid binary time-zone data: TZDB:$regionId, version: $versionId", ex)
      }
    }

    @throws[Exception]
    private[zone] def createRule(index: Short): ZoneRules = {
      var obj: AnyRef = ruleData.get(index)
      if (obj.isInstanceOf[Array[Byte]]) {
        val bytes: Array[Byte] = obj.asInstanceOf[Array[Byte]]
        val dis: DataInputStream = new DataInputStream(new ByteArrayInputStream(bytes))
        obj = Ser.read(dis)
        ruleData.set(index, obj)
      }
      obj.asInstanceOf[ZoneRules]
    }

    override def toString: String = versionId
  }
}

/** Loads time-zone rules for 'TZDB'.
  * <p>
  * This class is public for the service loader to access.
  *
  * <h3>Specification for implementors</h3>
  * This class is immutable and thread-safe.
  *
  * @constructor Creates an instance. Created by the {@code ServiceLoader}.
  * @throws ZoneRulesException if unable to load
  */
final class TzdbZoneRulesProvider extends ZoneRulesProvider {
  /** All the regions that are available. */
  private val regionIds: java.util.Set[String] = new CopyOnWriteArraySet[String]
  /** All the versions that are available. */
  private val versions: ConcurrentNavigableMap[String, TzdbZoneRulesProvider.Version] = new ConcurrentSkipListMap[String, TzdbZoneRulesProvider.Version]
  /** All the URLs that have been loaded.
    * Uses String to avoid equals() on URL.
    */
  private val loadedUrls: java.util.Set[String] = new CopyOnWriteArraySet[String]

  if (!load(classOf[ZoneRulesProvider].getClassLoader))
    throw new ZoneRulesException("No time-zone rules found for 'TZDB'")

  protected def provideZoneIds: java.util.Set[String] = new java.util.HashSet[String](regionIds)

  protected def provideRules(zoneId: String, forCaching: Boolean): ZoneRules = {
    Objects.requireNonNull(zoneId, "zoneId")
    val rules: ZoneRules = versions.lastEntry.getValue.getRules(zoneId)
    if (rules == null)
      throw new ZoneRulesException(s"Unknown time-zone ID: $zoneId")
    else
      rules
  }

  protected def provideVersions(zoneId: String): java.util.NavigableMap[String, ZoneRules] = {
    val map: java.util.TreeMap[String, ZoneRules] = new java.util.TreeMap[String, ZoneRules]
    val versionsIterator = versions.values.iterator
    while (versionsIterator.hasNext) {
      val version = versionsIterator.next()
      val rules: ZoneRules = version.getRules(zoneId)
      if (rules != null) {
        map.put(version.versionId, rules)
      }
    }
    map
  }

  /** Loads the rules.
    *
    * @param classLoader  the class loader to use, not null
    * @return true if updated
    * @throws ZoneRulesException if unable to load
    */
  private def load(classLoader: ClassLoader): Boolean = {
    var updated: Boolean = false
    var url: URL = null
    try {
      val en: java.util.Enumeration[URL] = classLoader.getResources("org/threeten/bp/TZDB.dat")
      while (en.hasMoreElements) {
        url = en.nextElement
        updated |= load(url)
      }
    }
    catch {
      case ex: Exception => throw new ZoneRulesException(s"Unable to load TZDB time-zone rules: $url", ex)
    }
    updated
  }

  /** Loads the rules from a URL, often in a jar file.
    *
    * @param url  the jar file to load, not null
    * @return true if updated
    * @throws ClassNotFoundException if a classpath error occurs
    * @throws IOException if an IO error occurs
    * @throws ZoneRulesException if the data is already loaded for the version
    */
  @throws[ClassNotFoundException]
  @throws[IOException]
  @throws[ZoneRulesException]
  private def load(url: URL): Boolean = {
    var updated: Boolean = false
    if (loadedUrls.add(url.toExternalForm)) {
      var in: InputStream = null
      try {
        in = url.openStream()
        updated |= load(in)
      } finally {
        if (in != null)
          in.close()
      }
    }
    updated
  }

  /** Loads the rules from an input stream.
    *
    * @param in  the stream to load, not null, not closed after use
    * @throws Exception if an error occurs
    */
  @throws[IOException]
  @throws[StreamCorruptedException]
  private def load(in: InputStream): Boolean = {
    var updated: Boolean = false
    val loadedVersions: java.util.Iterator[TzdbZoneRulesProvider.Version] = loadData(in).iterator
    while (loadedVersions.hasNext) {
      val loadedVersion = loadedVersions.next()
      val existing: TzdbZoneRulesProvider.Version = versions.putIfAbsent(loadedVersion.versionId, loadedVersion)
      if (existing != null && !(existing.versionId == loadedVersion.versionId))
        throw new ZoneRulesException(s"Data already loaded for TZDB time-zone rules version: ${loadedVersion.versionId}")
      updated = true
    }
    updated
  }

  /** Loads the rules from an input stream.
    *
    * @param in  the stream to load, not null, not closed after use
    * @throws Exception if an error occurs
    */
  @throws[IOException]
  @throws[StreamCorruptedException]
  private def loadData(in: InputStream): Iterable[TzdbZoneRulesProvider.Version] = {
    val dis: DataInputStream = new DataInputStream(in)
    if (dis.readByte != 1)
      throw new StreamCorruptedException("File format not recognised")
    val groupId: String = dis.readUTF
    if (!("TZDB" == groupId))
      throw new StreamCorruptedException("File format not recognised")
    val versionCount: Int = dis.readShort
    val versionArray: Array[String] = new Array[String](versionCount)

    {
      var i: Int = 0
      while (i < versionCount) {
        versionArray(i) = dis.readUTF
        i += 1
      }
    }
    val regionCount: Int = dis.readShort
    val regionArray: Array[String] = new Array[String](regionCount)

    {
      var i: Int = 0
      while (i < regionCount) {
        regionArray(i) = dis.readUTF
        i += 1
      }
    }
    regionIds.addAll(Arrays.asList(regionArray: _*))
    val ruleCount: Int = dis.readShort
    val ruleArray: Array[AnyRef] = new Array[AnyRef](ruleCount)

    {
      var i: Int = 0
      while (i < ruleCount) {
        val bytes: Array[Byte] = new Array[Byte](dis.readShort)
        dis.readFully(bytes)
        ruleArray(i) = bytes
        i += 1
      }
    }
    val ruleData: AtomicReferenceArray[AnyRef] = new AtomicReferenceArray[AnyRef](ruleArray)
    val versionSet: java.util.Set[TzdbZoneRulesProvider.Version] = new java.util.HashSet[TzdbZoneRulesProvider.Version](versionCount)

    {
      var i: Int = 0
      while (i < versionCount) {
        val versionRegionCount: Int = dis.readShort
        val versionRegionArray: Array[String] = new Array[String](versionRegionCount)
        val versionRulesArray: Array[Short] = new Array[Short](versionRegionCount)

        var j: Int = 0
        while (j < versionRegionCount) {
          versionRegionArray(j) = regionArray(dis.readShort)
          versionRulesArray(j) = dis.readShort
          j += 1
        }
        versionSet.add(new TzdbZoneRulesProvider.Version(versionArray(i), versionRegionArray, versionRulesArray, ruleData))
        i += 1
      }
    }
    versionSet
  }

  override def toString: String = "TZDB"
}
