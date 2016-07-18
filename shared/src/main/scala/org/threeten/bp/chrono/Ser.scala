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
package org.threeten.bp.chrono

import java.io.Externalizable
import java.io.IOException
import java.io.InvalidClassException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.StreamCorruptedException
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@SerialVersionUID(7857518227608961174L)
private object Ser {
  private[chrono] val JAPANESE_DATE_TYPE: Byte = 1
  private[chrono] val JAPANESE_ERA_TYPE: Byte = 2
  private[chrono] val HIJRAH_DATE_TYPE: Byte = 3
  private[chrono] val HIJRAH_ERA_TYPE: Byte = 4
  private[chrono] val MINGUO_DATE_TYPE: Byte = 5
  private[chrono] val MINGUO_ERA_TYPE: Byte = 6
  private[chrono] val THAIBUDDHIST_DATE_TYPE: Byte = 7
  private[chrono] val THAIBUDDHIST_ERA_TYPE: Byte = 8
  private[chrono] val CHRONO_TYPE: Byte = 11
  private[chrono] val CHRONO_LOCALDATETIME_TYPE: Byte = 12
  private[chrono] val CHRONO_ZONEDDATETIME_TYPE: Byte = 13

  @throws[IOException]
  private def writeInternal(`type`: Byte, `object`: AnyRef, out: ObjectOutput): Unit = {
    out.writeByte(`type`)
    `type` match {
      case JAPANESE_DATE_TYPE        => `object`.asInstanceOf[JapaneseDate].writeExternal(out)
      case JAPANESE_ERA_TYPE         => `object`.asInstanceOf[JapaneseEra].writeExternal(out)
      case HIJRAH_DATE_TYPE          => `object`.asInstanceOf[HijrahDate].writeExternal(out)
      case HIJRAH_ERA_TYPE           => `object`.asInstanceOf[HijrahEra].writeExternal(out)
      case MINGUO_DATE_TYPE          => `object`.asInstanceOf[MinguoDate].writeExternal(out)
      case MINGUO_ERA_TYPE           => `object`.asInstanceOf[MinguoEra].writeExternal(out)
      case THAIBUDDHIST_DATE_TYPE    => `object`.asInstanceOf[ThaiBuddhistDate].writeExternal(out)
      case THAIBUDDHIST_ERA_TYPE     => `object`.asInstanceOf[ThaiBuddhistEra].writeExternal(out)
      case CHRONO_TYPE               => `object`.asInstanceOf[Chronology].writeExternal(out)
      case CHRONO_LOCALDATETIME_TYPE => `object`.asInstanceOf[ChronoLocalDateTimeImpl[_]].writeExternal(out)
      case CHRONO_ZONEDDATETIME_TYPE => `object`.asInstanceOf[ChronoZonedDateTimeImpl[_]].writeExternal(out)
      case _                         => throw new InvalidClassException("Unknown serialized type")
    }
  }

  @throws[IOException]
  @throws[ClassNotFoundException]
  private[chrono] def read(in: ObjectInput): AnyRef = {
    val `type`: Byte = in.readByte
    readInternal(`type`, in)
  }

  @throws[IOException]
  @throws[ClassNotFoundException]
  private def readInternal(`type`: Byte, in: ObjectInput): AnyRef =
    `type` match {
      case JAPANESE_DATE_TYPE        => JapaneseDate.readExternal(in)
      case JAPANESE_ERA_TYPE         => JapaneseEra.readExternal(in)
      case HIJRAH_DATE_TYPE          => HijrahDate.readExternal(in)
      case HIJRAH_ERA_TYPE           => HijrahEra.readExternal(in)
      case MINGUO_DATE_TYPE          => MinguoDate.readExternal(in)
      case MINGUO_ERA_TYPE           => MinguoEra.readExternal(in)
      case THAIBUDDHIST_DATE_TYPE    => ThaiBuddhistDate.readExternal(in)
      case THAIBUDDHIST_ERA_TYPE     => ThaiBuddhistEra.readExternal(in)
      case CHRONO_TYPE               => Chronology.readExternal(in)
      case CHRONO_LOCALDATETIME_TYPE => ChronoLocalDateTimeImpl.readExternal(in)
      case CHRONO_ZONEDDATETIME_TYPE => ChronoZonedDateTimeImpl.readExternal(in)
      case _                         => throw new StreamCorruptedException("Unknown serialized type")
    }
}

/** The shared serialization delegate for this package.
  *
  * <h4>Implementation notes</h4>
  * This class wraps the object being serialized, and takes a byte representing the type of the class to
  * be serialized.  This byte can also be used for versioning the serialization format.  In this case another
  * byte flag would be used in order to specify an alternative version of the type format.
  * For example {@code JAPANESE_DATE_TYPE_VERSION_2 = 21}.
  *
  * In order to serialise the object it writes its byte and then calls back to the appropriate class where
  * the serialisation is performed.  In order to deserialise the object it read in the type byte, switching
  * in order to select which class to call back into.
  *
  * The serialisation format is determined on a per class basis.  In the case of field based classes each
  * of the fields is written out with an appropriate size format in descending order of the field's size.  For
  * example in the case of {@link LocalDate} year is written before month.  Composite classes, such as
  * {@link LocalDateTime} are serialised as one object.
  *
  * This class is mutable and should be created once per serialization.
  *
  * @constructor Creates an instance for serialization.
  *
  * @param type  the type being serialized
  * @param object  the object being serialized
  */
@SerialVersionUID(7857518227608961174L)
final class Ser private[chrono](private var `type`: Byte, private var `object`: AnyRef) extends Externalizable {

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
