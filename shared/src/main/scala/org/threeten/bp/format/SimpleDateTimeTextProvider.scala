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

import org.threeten.bp.temporal.ChronoField.AMPM_OF_DAY
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Comparator
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import org.threeten.bp.temporal.IsoFields
import org.threeten.bp.temporal.TemporalField

private object SimpleDateTimeTextProvider {
  /** Cache. */
  private val CACHE: ConcurrentMap[java.util.Map.Entry[TemporalField, Locale], AnyRef] =
    new ConcurrentHashMap[java.util.Map.Entry[TemporalField, Locale], AnyRef](16, 0.75f, 2)
  /** Comparator. */
  private val COMPARATOR: Comparator[java.util.Map.Entry[String, Long]] =
    new Comparator[java.util.Map.Entry[String, Long]] {
      override def compare(obj1: java.util.Map.Entry[String, Long], obj2: java.util.Map.Entry[String, Long]): Int =
        obj2.getKey.length - obj1.getKey.length
    }

  /** Helper method to create an immutable entry.
    *
    * @param text  the text, not null
    * @param field  the field, not null
    * @return the entry, not null
    */
  private def createEntry[A, B](text: A, field: B): java.util.Map.Entry[A, B] =
    new java.util.AbstractMap.SimpleImmutableEntry[A, B](text, field)

  private def createLocaleStore(valueTextMap: java.util.Map[TextStyle, java.util.Map[Long, String]]): SimpleDateTimeTextProvider.LocaleStore = {
    valueTextMap.put(TextStyle.FULL_STANDALONE, valueTextMap.get(TextStyle.FULL))
    valueTextMap.put(TextStyle.SHORT_STANDALONE, valueTextMap.get(TextStyle.SHORT))
    if (valueTextMap.containsKey(TextStyle.NARROW) && !valueTextMap.containsKey(TextStyle.NARROW_STANDALONE))
      valueTextMap.put(TextStyle.NARROW_STANDALONE, valueTextMap.get(TextStyle.NARROW))
    new SimpleDateTimeTextProvider.LocaleStore(valueTextMap)
  }

  /** Stores the text for a single locale.
    *
    * Some fields have a textual representation, such as day-of-week or month-of-year.
    * These textual representations can be captured in this class for printing
    * and parsing.
    *
    * This class is immutable and thread-safe.
    *
    * @constructor
    *
    * @param valueTextMap  the map of values to text to store, assigned and not altered, not null
    */
  private[format] final class LocaleStore private[format](private val valueTextMap: java.util.Map[TextStyle, java.util.Map[Long, String]]) {
    /** Parsable data. */
    private val parsable: java.util.Map[TextStyle, java.util.List[java.util.Map.Entry[String, Long]]] = {
      val map: java.util.Map[TextStyle, java.util.List[java.util.Map.Entry[String, Long]]] = new java.util.HashMap[TextStyle, java.util.List[java.util.Map.Entry[String, Long]]]
      val allList: java.util.List[java.util.Map.Entry[String, Long]] = new java.util.ArrayList[java.util.Map.Entry[String, Long]]
      val styles = valueTextMap.keySet.iterator
      while (styles.hasNext) {
        val style = styles.next()
        val reverse: java.util.Map[String, java.util.Map.Entry[String, Long]] = new java.util.HashMap[String, java.util.Map.Entry[String, Long]]
        var continue = true
        val entries = valueTextMap.get(style).entrySet.iterator()
        while (continue && entries.hasNext) {
          val entry = entries.next()
          if (reverse.put(entry.getValue, createEntry(entry.getValue, entry.getKey)) != null)
            continue = false
        }
        val list: java.util.List[java.util.Map.Entry[String, Long]] = new java.util.ArrayList[java.util.Map.Entry[String, Long]](reverse.values)
        java.util.Collections.sort(list, COMPARATOR)
        map.put(style, list)
        allList.addAll(list)
        map.put(null, allList)
      }
      java.util.Collections.sort(allList, COMPARATOR)
      map
    }


    /** Gets the text for the specified field value, locale and style
      * for the purpose of printing.
      *
      * @param value  the value to get text for, not null
      * @param style  the style to get text for, not null
      * @return the text for the field value, null if no text found
      */
    private[format] def getText(value: Long, style: TextStyle): String = {
      val map: java.util.Map[Long, String] = valueTextMap.get(style)
      if (map != null) map.get(value) else null
    }

    /** Gets an iterator of text to field for the specified style for the purpose of parsing.
      *
      * The iterator must be returned in order from the longest text to the shortest.
      *
      * @param style  the style to get text for, null for all parsable text
      * @return the iterator of text to field pairs, in order from longest text to shortest text,
      *         null if the style is not parsable
      */
    private[format] def getTextIterator(style: TextStyle): java.util.Iterator[java.util.Map.Entry[String, Long]] = {
      val list: java.util.List[java.util.Map.Entry[String, Long]] = parsable.get(style)
      if (list != null) list.iterator else null
    }
  }
}

/** The Service Provider Implementation to obtain date-time text for a field.
  *
  * This implementation is based on extraction of data from a {@link DateFormatSymbols}.
  *
  * <h3>Specification for implementors</h3>
  * This class is immutable and thread-safe.
  */
final class SimpleDateTimeTextProvider extends DateTimeTextProvider {
  /** {@inheritDoc} */
  override def getAvailableLocales: Array[Locale] = DateFormatSymbols.getAvailableLocales

  def getText(field: TemporalField, value: Long, style: TextStyle, locale: Locale): String = {
    val store: AnyRef = findStore(field, locale)
    if (store.isInstanceOf[SimpleDateTimeTextProvider.LocaleStore])
      store.asInstanceOf[SimpleDateTimeTextProvider.LocaleStore].getText(value, style)
    else
      null
  }

  def getTextIterator(field: TemporalField, style: TextStyle, locale: Locale): java.util.Iterator[java.util.Map.Entry[String, Long]] = {
    val store: AnyRef = findStore(field, locale)
    if (store.isInstanceOf[SimpleDateTimeTextProvider.LocaleStore])
      store.asInstanceOf[SimpleDateTimeTextProvider.LocaleStore].getTextIterator(style)
    else
      null
  }

  private def findStore(field: TemporalField, locale: Locale): AnyRef = {
    val key: java.util.Map.Entry[TemporalField, Locale] = SimpleDateTimeTextProvider.createEntry(field, locale)
    var store: AnyRef = SimpleDateTimeTextProvider.CACHE.get(key)
    if (store == null) {
      store = createStore(field, locale)
      SimpleDateTimeTextProvider.CACHE.putIfAbsent(key, store)
      store = SimpleDateTimeTextProvider.CACHE.get(key)
    }
    store
  }

  private def createStore(field: TemporalField, locale: Locale): AnyRef = {
    if (field eq MONTH_OF_YEAR) {
      val oldSymbols: DateFormatSymbols = DateFormatSymbols.getInstance(locale)
      val styleMap: java.util.Map[TextStyle, java.util.Map[Long, String]] = new java.util.HashMap[TextStyle, java.util.Map[Long, String]]
      val f1: Long = 1L
      val f2: Long = 2L
      val f3: Long = 3L
      val f4: Long = 4L
      val f5: Long = 5L
      val f6: Long = 6L
      val f7: Long = 7L
      val f8: Long = 8L
      val f9: Long = 9L
      val f10: Long = 10L
      val f11: Long = 11L
      val f12: Long = 12L
      var array: Array[String] = oldSymbols.getMonths
      var map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
      map.put(f1, array(Calendar.JANUARY))
      map.put(f2, array(Calendar.FEBRUARY))
      map.put(f3, array(Calendar.MARCH))
      map.put(f4, array(Calendar.APRIL))
      map.put(f5, array(Calendar.MAY))
      map.put(f6, array(Calendar.JUNE))
      map.put(f7, array(Calendar.JULY))
      map.put(f8, array(Calendar.AUGUST))
      map.put(f9, array(Calendar.SEPTEMBER))
      map.put(f10, array(Calendar.OCTOBER))
      map.put(f11, array(Calendar.NOVEMBER))
      map.put(f12, array(Calendar.DECEMBER))
      styleMap.put(TextStyle.FULL, map)
      map = new java.util.HashMap[Long, String]
      map.put(f1, array(Calendar.JANUARY).substring(0, 1))
      map.put(f2, array(Calendar.FEBRUARY).substring(0, 1))
      map.put(f3, array(Calendar.MARCH).substring(0, 1))
      map.put(f4, array(Calendar.APRIL).substring(0, 1))
      map.put(f5, array(Calendar.MAY).substring(0, 1))
      map.put(f6, array(Calendar.JUNE).substring(0, 1))
      map.put(f7, array(Calendar.JULY).substring(0, 1))
      map.put(f8, array(Calendar.AUGUST).substring(0, 1))
      map.put(f9, array(Calendar.SEPTEMBER).substring(0, 1))
      map.put(f10, array(Calendar.OCTOBER).substring(0, 1))
      map.put(f11, array(Calendar.NOVEMBER).substring(0, 1))
      map.put(f12, array(Calendar.DECEMBER).substring(0, 1))
      styleMap.put(TextStyle.NARROW, map)
      array = oldSymbols.getShortMonths
      map = new java.util.HashMap[Long, String]
      map.put(f1, array(Calendar.JANUARY))
      map.put(f2, array(Calendar.FEBRUARY))
      map.put(f3, array(Calendar.MARCH))
      map.put(f4, array(Calendar.APRIL))
      map.put(f5, array(Calendar.MAY))
      map.put(f6, array(Calendar.JUNE))
      map.put(f7, array(Calendar.JULY))
      map.put(f8, array(Calendar.AUGUST))
      map.put(f9, array(Calendar.SEPTEMBER))
      map.put(f10, array(Calendar.OCTOBER))
      map.put(f11, array(Calendar.NOVEMBER))
      map.put(f12, array(Calendar.DECEMBER))
      styleMap.put(TextStyle.SHORT, map)
      return SimpleDateTimeTextProvider.createLocaleStore(styleMap)
    }
    if (field eq DAY_OF_WEEK) {
      val oldSymbols: DateFormatSymbols = DateFormatSymbols.getInstance(locale)
      val styleMap: java.util.Map[TextStyle, java.util.Map[Long, String]] = new java.util.HashMap[TextStyle, java.util.Map[Long, String]]
      val f1: Long = 1L
      val f2: Long = 2L
      val f3: Long = 3L
      val f4: Long = 4L
      val f5: Long = 5L
      val f6: Long = 6L
      val f7: Long = 7L
      var array: Array[String] = oldSymbols.getWeekdays
      var map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
      map.put(f1, array(Calendar.MONDAY))
      map.put(f2, array(Calendar.TUESDAY))
      map.put(f3, array(Calendar.WEDNESDAY))
      map.put(f4, array(Calendar.THURSDAY))
      map.put(f5, array(Calendar.FRIDAY))
      map.put(f6, array(Calendar.SATURDAY))
      map.put(f7, array(Calendar.SUNDAY))
      styleMap.put(TextStyle.FULL, map)
      map = new java.util.HashMap[Long, String]
      map.put(f1, array(Calendar.MONDAY).substring(0, 1))
      map.put(f2, array(Calendar.TUESDAY).substring(0, 1))
      map.put(f3, array(Calendar.WEDNESDAY).substring(0, 1))
      map.put(f4, array(Calendar.THURSDAY).substring(0, 1))
      map.put(f5, array(Calendar.FRIDAY).substring(0, 1))
      map.put(f6, array(Calendar.SATURDAY).substring(0, 1))
      map.put(f7, array(Calendar.SUNDAY).substring(0, 1))
      styleMap.put(TextStyle.NARROW, map)
      array = oldSymbols.getShortWeekdays
      map = new java.util.HashMap[Long, String]
      map.put(f1, array(Calendar.MONDAY))
      map.put(f2, array(Calendar.TUESDAY))
      map.put(f3, array(Calendar.WEDNESDAY))
      map.put(f4, array(Calendar.THURSDAY))
      map.put(f5, array(Calendar.FRIDAY))
      map.put(f6, array(Calendar.SATURDAY))
      map.put(f7, array(Calendar.SUNDAY))
      styleMap.put(TextStyle.SHORT, map)
      return SimpleDateTimeTextProvider.createLocaleStore(styleMap)
    }
    if (field eq AMPM_OF_DAY) {
      val oldSymbols: DateFormatSymbols = DateFormatSymbols.getInstance(locale)
      val styleMap: java.util.Map[TextStyle, java.util.Map[Long, String]] = new java.util.HashMap[TextStyle, java.util.Map[Long, String]]
      val array: Array[String] = oldSymbols.getAmPmStrings
      val map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
      map.put(0L, array(Calendar.AM))
      map.put(1L, array(Calendar.PM))
      styleMap.put(TextStyle.FULL, map)
      styleMap.put(TextStyle.SHORT, map)
      return SimpleDateTimeTextProvider.createLocaleStore(styleMap)
    }
    if (field eq ERA) {
      val oldSymbols: DateFormatSymbols = DateFormatSymbols.getInstance(locale)
      val styleMap: java.util.Map[TextStyle, java.util.Map[Long, String]] = new java.util.HashMap[TextStyle, java.util.Map[Long, String]]
      val array: Array[String] = oldSymbols.getEras
      var map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
      map.put(0L, array(GregorianCalendar.BC))
      map.put(1L, array(GregorianCalendar.AD))
      styleMap.put(TextStyle.SHORT, map)
      if (locale.getLanguage == Locale.ENGLISH.getLanguage) {
        map = new java.util.HashMap[Long, String]
        map.put(0L, "Before Christ")
        map.put(1L, "Anno Domini")
        styleMap.put(TextStyle.FULL, map)
      }
      else {
        styleMap.put(TextStyle.FULL, map)
      }
      map = new java.util.HashMap[Long, String]
      map.put(0L, array(GregorianCalendar.BC).substring(0, 1))
      map.put(1L, array(GregorianCalendar.AD).substring(0, 1))
      styleMap.put(TextStyle.NARROW, map)
      return SimpleDateTimeTextProvider.createLocaleStore(styleMap)
    }
    if (field eq IsoFields.QUARTER_OF_YEAR) {
      val styleMap: java.util.Map[TextStyle, java.util.Map[Long, String]] = new java.util.HashMap[TextStyle, java.util.Map[Long, String]]
      var map: java.util.Map[Long, String] = new java.util.HashMap[Long, String]
      map.put(1L, "Q1")
      map.put(2L, "Q2")
      map.put(3L, "Q3")
      map.put(4L, "Q4")
      styleMap.put(TextStyle.SHORT, map)
      map = new java.util.HashMap[Long, String]
      map.put(1L, "1st quarter")
      map.put(2L, "2nd quarter")
      map.put(3L, "3rd quarter")
      map.put(4L, "4th quarter")
      styleMap.put(TextStyle.FULL, map)
      return SimpleDateTimeTextProvider.createLocaleStore(styleMap)
    }
    ""
  }
}
