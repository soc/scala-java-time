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

import java.io.DataInput
import java.io.DataOutput
import java.io.Externalizable
import java.io.IOException
import java.io.InvalidClassException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.StreamCorruptedException
import org.threeten.bp.ZoneOffset

/** The shared serialization delegate for this package.
  *
  * <h4>Implementation notes</h4>
  * This class is mutable and should be created once per serialization.
  */
@SerialVersionUID(-8885321777449118786L)
private[zone] object Ser {
  /** Type for StandardZoneRules. */
  private[zone] val SZR: Byte = 1
  /** Type for ZoneOffsetTransition. */
  private[zone] val ZOT: Byte = 2
  /** Type for ZoneOffsetTransition. */
  private[zone] val ZOTRULE: Byte = 3

  @throws[IOException]
  private[zone] def write(`object`: AnyRef, out: DataOutput): Unit =
    writeInternal(SZR, `object`, out)

  @throws[IOException]
  private def writeInternal(`type`: Byte, `object`: AnyRef, out: DataOutput): Unit = {
    out.writeByte(`type`)
    `type` match {
      case SZR     => `object`.asInstanceOf[StandardZoneRules].writeExternal(out)
      case ZOT     => `object`.asInstanceOf[ZoneOffsetTransition].writeExternal(out)
      case ZOTRULE => `object`.asInstanceOf[ZoneOffsetTransitionRule].writeExternal(out)
      case _       => throw new InvalidClassException("Unknown serialized type")
    }
  }

  @throws[IOException]
  @throws[ClassNotFoundException]
  private[zone] def read(in: DataInput): AnyRef = {
    val `type`: Byte = in.readByte
    readInternal(`type`, in)
  }

  @throws[IOException]
  @throws[ClassNotFoundException]
  private def readInternal(`type`: Byte, in: DataInput): AnyRef = {
    `type` match {
      case SZR     => StandardZoneRules.readExternal(in)
      case ZOT     => ZoneOffsetTransition.readExternal(in)
      case ZOTRULE => ZoneOffsetTransitionRule.readExternal(in)
      case _       => throw new StreamCorruptedException("Unknown serialized type")
    }
  }

  /** Writes the state to the stream.
    *
    * @param offset  the offset, not null
    * @param out  the output stream, not null
    * @throws IOException if an error occurs
    */
  @throws[IOException]
  private[zone] def writeOffset(offset: ZoneOffset, out: DataOutput): Unit = {
    val offsetSecs: Int = offset.getTotalSeconds
    val offsetByte: Int = if (offsetSecs % 900 == 0) offsetSecs / 900 else 127
    out.writeByte(offsetByte)
    if (offsetByte == 127)
      out.writeInt(offsetSecs)
  }

  /** Reads the state from the stream.
    *
    * @param in  the input stream, not null
    * @return the created object, not null
    * @throws IOException if an error occurs
    */
  @throws[IOException]
  private[zone] def readOffset(in: DataInput): ZoneOffset = {
    val offsetByte: Int = in.readByte
    if (offsetByte == 127) ZoneOffset.ofTotalSeconds(in.readInt) else ZoneOffset.ofTotalSeconds(offsetByte * 900)
  }

  /** Writes the state to the stream.
    *
    * @param epochSec  the epoch seconds, not null
    * @param out  the output stream, not null
    * @throws IOException if an error occurs
    */
  @throws[IOException]
  private[zone] def writeEpochSec(epochSec: Long, out: DataOutput): Unit = {
    if (epochSec >= -4575744000L && epochSec < 10413792000L && epochSec % 900 == 0) {
      val store: Int = ((epochSec + 4575744000L) / 900).toInt
      out.writeByte((store >>> 16) & 255)
      out.writeByte((store >>> 8) & 255)
      out.writeByte(store & 255)
    }
    else {
      out.writeByte(255)
      out.writeLong(epochSec)
    }
  }

  /** Reads the state from the stream.
    *
    * @param in  the input stream, not null
    * @return the epoch seconds, not null
    * @throws IOException if an error occurs
    */
  @throws[IOException]
  private[zone] def readEpochSec(in: DataInput): Long = {
    val hiByte: Int = in.readByte & 255
    if (hiByte == 255)
      in.readLong
    else {
      val midByte: Int = in.readByte & 255
      val loByte: Int = in.readByte & 255
      val tot: Long = (hiByte << 16) + (midByte << 8) + loByte
      (tot * 900) - 4575744000L
    }
  }
}

/** Creates an instance for serialization.
  *
  * @param `type`  the type being serialized
  * @param object  the object being serialized
  */
@SerialVersionUID(-8885321777449118786L)
final class Ser private[zone](private var `type`: Byte, private var `object`: AnyRef) extends Externalizable {

  /** @constructor Constructor for deserialization. */
  def this() {
    this(0, null)
  }

  /** Implements the {@code Externalizable} interface to write the object.
    *
    * @param out  the data stream to write to, not null
    */
  @throws[IOException]
  def writeExternal(out: ObjectOutput): Unit = Ser.writeInternal(`type`, `object`, out)

  /** Implements the {@code Externalizable} interface to read the object.
    *
    * @param in  the data to read, not null
    */
  @throws[IOException]
  @throws[ClassNotFoundException]
  def readExternal(in: ObjectInput): Unit = {
    `type` = in.readByte
    `object` = Ser.readInternal(`type`, in)
  }

  /** Returns the object that will replace this one.
    *
    * @return the read object, should never be null
    */
  private def readResolve: AnyRef = `object`
}
