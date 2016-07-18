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
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamConstants
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/** Base test class. */
object AbstractTest {
  private val SERIALISATION_DATA_FOLDER: String = "jvm/src/test/resources/"

  def isIsoLeap(year: Long): Boolean =
    if (year % 4 != 0)
      false
    else if (year % 100 == 0 && year % 400 != 0)
      false
    else
      true

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def assertSerializable(o: AnyRef): Unit = {
    val deserialisedObject: AnyRef = writeThenRead(o)
    assertEquals(deserialisedObject, o)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def assertSerializableAndSame(o: AnyRef): Unit = {
    val deserialisedObject: AnyRef = writeThenRead(o)
    assertSame(deserialisedObject, o)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def writeThenRead(o: AnyRef): AnyRef = {
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    var oos: ObjectOutputStream = null
    try {
      oos = new ObjectOutputStream(baos)
      oos.writeObject(o)
    } finally {
      if (oos != null) {
        oos.close()
      }
    }
    var ois: ObjectInputStream = null
    try {
      ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
      ois.readObject
    } finally {
      if (ois != null) {
        ois.close()
      }
    }
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def assertEqualsSerialisedForm(objectSerialised: AnyRef): Unit = {
    assertEqualsSerialisedForm(objectSerialised, objectSerialised.getClass)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def assertEqualsSerialisedForm(objectSerialised: AnyRef, cls: Class[_]): Unit = {
    val className: String = cls.getSimpleName
    var in: ObjectInputStream = null
    try {
      in = new ObjectInputStream(new FileInputStream(SERIALISATION_DATA_FOLDER + className + ".bin"))
      val objectFromFile: AnyRef = in.readObject
      assertEquals(objectFromFile, objectSerialised)
    } finally {
      if (in != null) {
        in.close()
      }
    }
  }

  def assertImmutable(cls: Class[_]): Unit = {
    assertTrue(Modifier.isPublic(cls.getModifiers))
    assertTrue(Modifier.isFinal(cls.getModifiers))
    val fields: Array[Field] = cls.getDeclaredFields
    for (field <- fields) {
      if (!field.getName.contains("$")) {
        if (Modifier.isStatic(field.getModifiers)) {
          assertTrue(Modifier.isFinal(field.getModifiers), "Field:" + field.getName)
        }
        else {
          assertTrue(Modifier.isPrivate(field.getModifiers), "Field:" + field.getName)
          assertTrue(Modifier.isFinal(field.getModifiers), "Field:" + field.getName)
        }
      }
    }
    val cons: Array[Constructor[_]] = cls.getDeclaredConstructors
    for (con <- cons) {
      assertTrue(Modifier.isPrivate(con.getModifiers))
    }
  }

  @throws(classOf[Exception])
  def assertSerializedBySer(`object`: AnyRef, expectedBytes: Array[Byte], matches: Array[Byte]*): Unit = {
    val serClass: String = `object`.getClass.getPackage.getName + ".Ser"
    val serCls: Class[_] = Class.forName(serClass)
    val field: Field = serCls.getDeclaredField("serialVersionUID")
    field.setAccessible(true)
    val serVer: Long = field.get(null).asInstanceOf[Long]
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    var oos: ObjectOutputStream = null
    try {
      oos = new ObjectOutputStream(baos)
      oos.writeObject(`object`)
    } finally {
      if (oos != null) {
        oos.close()
      }
    }
    val bytes: Array[Byte] = baos.toByteArray
    val bais: ByteArrayInputStream = new ByteArrayInputStream(bytes)
    var dis: DataInputStream = null
    try {
      dis = new DataInputStream(bais)
      assertEquals(dis.readShort, ObjectStreamConstants.STREAM_MAGIC)
      assertEquals(dis.readShort, ObjectStreamConstants.STREAM_VERSION)
      assertEquals(dis.readByte, ObjectStreamConstants.TC_OBJECT)
      assertEquals(dis.readByte, ObjectStreamConstants.TC_CLASSDESC)
      assertEquals(dis.readUTF, serClass)
      assertEquals(dis.readLong, serVer)
      assertEquals(dis.readByte, ObjectStreamConstants.SC_EXTERNALIZABLE | ObjectStreamConstants.SC_BLOCK_DATA)
      assertEquals(dis.readShort, 0)
      assertEquals(dis.readByte, ObjectStreamConstants.TC_ENDBLOCKDATA)
      assertEquals(dis.readByte, ObjectStreamConstants.TC_NULL)
      if (expectedBytes.length < 256) {
        assertEquals(dis.readByte, ObjectStreamConstants.TC_BLOCKDATA)
        assertEquals(dis.readUnsignedByte, expectedBytes.length)
      }
      else {
        assertEquals(dis.readByte, ObjectStreamConstants.TC_BLOCKDATALONG)
        assertEquals(dis.readInt, expectedBytes.length)
      }
      val input: Array[Byte] = new Array[Byte](expectedBytes.length)
      dis.readFully(input)
      assertEquals(input, expectedBytes)
      if (matches.nonEmpty) {
        for (mtch <- matches) {
          var matched: Boolean = false
          while (!matched) {
            try {
              dis.mark(1000)
              val possible: Array[Byte] = new Array[Byte](mtch.length)
              dis.readFully(possible)
              assertEquals(possible, mtch)
              matched = true
            }
            catch {
              case ex: AssertionError =>
                dis.reset()
                dis.readByte()
            }
          }
        }
      }
      else {
        assertEquals(dis.readByte(), ObjectStreamConstants.TC_ENDBLOCKDATA)
        assertEquals(dis.read(), -1)
      }
    } finally {
      if (dis != null) {
        dis.close()
      }
    }
  }
}
