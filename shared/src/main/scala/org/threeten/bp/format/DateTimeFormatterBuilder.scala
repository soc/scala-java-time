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

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.{Collections, Comparator, Locale, MissingResourceException, Objects, ResourceBundle, TimeZone}
import java.lang.StringBuilder

import org.threeten.bp.DateTimeException
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.chrono.ChronoLocalDate
import org.threeten.bp.chrono.Chronology
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.IsoFields
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries
import org.threeten.bp.temporal.TemporalQuery
import org.threeten.bp.temporal.ValueRange
import org.threeten.bp.temporal.WeekFields
import org.threeten.bp.zone.ZoneRulesProvider
import org.threeten.bp.format.DateTimeFormatterBuilder.ZoneIdPrinterParser.SubstringTree

import scala.annotation.tailrec

object DateTimeFormatterBuilder {
  /** Query for a time-zone that is region-only. */
  private val QUERY_REGION_ONLY: TemporalQuery[ZoneId] =
    (temporal: TemporalAccessor) => {
      val zone: ZoneId = temporal.query(TemporalQueries.zoneId)
      if (zone != null && !zone.isInstanceOf[ZoneOffset]) zone else null
    }

  /** Gets the formatting pattern for date and time styles for a locale and chronology.
    * The locale and chronology are used to lookup the locale specific format
    * for the requested dateStyle and/or timeStyle.
    *
    * @param dateStyle  the FormatStyle for the date
    * @param timeStyle  the FormatStyle for the time
    * @param chrono  the Chronology, non-null
    * @param locale  the locale, non-null
    * @return the locale and Chronology specific formatting pattern
    * @throws IllegalArgumentException if both dateStyle and timeStyle are null
    */
  def getLocalizedDateTimePattern(dateStyle: FormatStyle, timeStyle: FormatStyle, chrono: Chronology, locale: Locale): String = {
    Objects.requireNonNull(locale, "locale")
    Objects.requireNonNull(chrono, "chrono")
    if (dateStyle == null && timeStyle == null)
      throw new IllegalArgumentException("Either dateStyle or timeStyle must be non-null")
    var dateFormat: DateFormat = null
    if (dateStyle != null)
      if (timeStyle != null)
        dateFormat = DateFormat.getDateTimeInstance(dateStyle.ordinal, timeStyle.ordinal, locale)
      else
        dateFormat = DateFormat.getDateInstance(dateStyle.ordinal, locale)
    else
      dateFormat = DateFormat.getTimeInstance(timeStyle.ordinal, locale)
    if (dateFormat.isInstanceOf[SimpleDateFormat])
      dateFormat.asInstanceOf[SimpleDateFormat].toPattern
    else
      throw new IllegalArgumentException("Unable to determine pattern")
  }

  /** Map of letters to fields. */
  private val FIELD_MAP: java.util.Map[Character, TemporalField] = {
    val map = new java.util.HashMap[Character, TemporalField]
    map.put('G', ChronoField.ERA)
    map.put('y', ChronoField.YEAR_OF_ERA)
    map.put('u', ChronoField.YEAR)
    map.put('Q', IsoFields.QUARTER_OF_YEAR)
    map.put('q', IsoFields.QUARTER_OF_YEAR)
    map.put('M', ChronoField.MONTH_OF_YEAR)
    map.put('L', ChronoField.MONTH_OF_YEAR)
    map.put('D', ChronoField.DAY_OF_YEAR)
    map.put('d', ChronoField.DAY_OF_MONTH)
    map.put('F', ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)
    map.put('E', ChronoField.DAY_OF_WEEK)
    map.put('c', ChronoField.DAY_OF_WEEK)
    map.put('e', ChronoField.DAY_OF_WEEK)
    map.put('a', ChronoField.AMPM_OF_DAY)
    map.put('H', ChronoField.HOUR_OF_DAY)
    map.put('k', ChronoField.CLOCK_HOUR_OF_DAY)
    map.put('K', ChronoField.HOUR_OF_AMPM)
    map.put('h', ChronoField.CLOCK_HOUR_OF_AMPM)
    map.put('m', ChronoField.MINUTE_OF_HOUR)
    map.put('s', ChronoField.SECOND_OF_MINUTE)
    map.put('S', ChronoField.NANO_OF_SECOND)
    map.put('A', ChronoField.MILLI_OF_DAY)
    map.put('n', ChronoField.NANO_OF_SECOND)
    map.put('N', ChronoField.NANO_OF_DAY)
    map
  }

  /** Strategy for printing/parsing date-time information.
    *
    * The printer may print any part, or the whole, of the input date-time object.
    * Typically, a complete print is constructed from a number of smaller
    * units, each outputting a single field.
    *
    * The parser may parse any piece of text from the input, storing the result
    * in the context. Typically, each individual parser will just parse one
    * field, such as the day-of-month, storing the value in the context.
    * Once the parse is complete, the caller will then convert the context
    * to a {@link DateTimeBuilder} to merge the parsed values to create the
    * desired object, such as a {@code LocalDate}.
    *
    * The parse position will be updated during the parse. Parsing will start at
    * the specified index and the return value specifies the new parse position
    * for the next parser. If an error occurs, the returned index will be negative
    * and will have the error position encoded using the complement operator.
    *
    * <h3>Specification for implementors</h3>
    * This interface must be implemented with care to ensure other classes operate correctly.
    * All implementations that can be instantiated must be final, immutable and thread-safe.
    *
    * The context is not a thread-safe object and a new instance will be created
    * for each print that occurs. The context must not be stored in an instance
    * variable or shared with any other threads.
    */
  private[format] trait DateTimePrinterParser {
    /** Prints the date-time object to the buffer.
      *
      * The context holds information to use during the print.
      * It also contains the date-time information to be printed.
      *
      * The buffer must not be mutated beyond the content controlled by the implementation.
      *
      * @param context  the context to print using, not null
      * @param buf  the buffer to append to, not null
      * @return false if unable to query the value from the date-time, true otherwise
      * @throws DateTimeException if the date-time cannot be printed successfully
      */
    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean

    /** Parses text into date-time information.
      *
      * The context holds information to use during the parse.
      * It is also used to store the parsed date-time information.
      *
      * @param context  the context to use and parse into, not null
      * @param text  the input text to parse, not null
      * @param position  the position to start parsing at, from 0 to the text length
      * @return the new parse position, where negative means an error with the
      *         error position encoded using the complement ~ operator
      * @throws NullPointerException if the context or text is null
      * @throws IndexOutOfBoundsException if the position is invalid
      */
    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int
  }

  /** Composite printer and parser. */
  private[format] final class CompositePrinterParser private[format](private val printerParsers: Array[DateTimeFormatterBuilder.DateTimePrinterParser], private val optional: Boolean) extends DateTimePrinterParser {

    private[format] def this(printerParsers: java.util.List[DateTimeFormatterBuilder.DateTimePrinterParser], optional: Boolean) {
      this(printerParsers.toArray(new Array[DateTimeFormatterBuilder.DateTimePrinterParser](printerParsers.size)), optional)
    }


    /** Returns a copy of this printer-parser with the optional flag changed.
      *
      * @param optional  the optional flag to set in the copy
      * @return the new printer-parser, not null
      */
    def withOptional(optional: Boolean): DateTimeFormatterBuilder.CompositePrinterParser =
      if (optional == this.optional)
        this
      else
        new DateTimeFormatterBuilder.CompositePrinterParser(printerParsers, optional)

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val length: Int = buf.length
      if (optional)
        context.startOptional()
      try {
        for (pp <- printerParsers) {
          if (!pp.print(context, buf)) {
            buf.setLength(length)
            return true
          }
        }
      } finally {
        if (optional) {
          context.endOptional()
        }
      }
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      var _position = position

      if (optional) {
        context.startOptional()
        var pos: Int = _position
        for (pp <- printerParsers) {
          pos = pp.parse(context, text, pos)
          if (pos < 0) {
            context.endOptional(false)
            return _position
          }
        }
        context.endOptional(true)
        pos
      }
      else {
        scala.util.control.Breaks.breakable {
          for (pp <- printerParsers) {
            _position = pp.parse(context, text, _position)
            if (_position < 0) {
              scala.util.control.Breaks.break()
            }
          }
        }
        _position
      }
    }

    override def toString: String = {
      val buf: StringBuilder = new StringBuilder
      if (printerParsers != null) {
        buf.append(if (optional) "[" else "(")
        for (pp <- printerParsers) {
          buf.append(pp)
        }
        buf.append(if (optional) "]" else ")")
      }
      buf.toString
    }
  }

  /** Pads the output to a fixed width.
    *
    * @constructor
    *
    * @param printerParser  the printer, not null
    * @param padWidth  the width to pad to, 1 or greater
    * @param padChar  the pad character
    */
  private[format] final class PadPrinterParserDecorator private[format](private val printerParser: DateTimeFormatterBuilder.DateTimePrinterParser, private val padWidth: Int, private val padChar: Char) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val preLen: Int = buf.length
      if (!printerParser.print(context, buf))
        return false
      val len: Int = buf.length - preLen
      if (len > padWidth)
        throw new DateTimeException(s"Cannot print as output of $len characters exceeds pad width of $padWidth")
      var i: Int = 0
      while (i < padWidth - len) {
        buf.insert(preLen, padChar)
        i += 1
      }
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      var _text = text
      val strict: Boolean = context.isStrict
      val caseSensitive: Boolean = context.isCaseSensitive
      if (position > _text.length)
        throw new IndexOutOfBoundsException
      if (position == _text.length)
        return ~position
      var endPos: Int = position + padWidth
      if (endPos > _text.length) {
        if (strict)
          return ~position
        endPos = _text.length
      }
      var pos: Int = position
      while (pos < endPos && (if (caseSensitive) _text.charAt(pos) == padChar else context.charEquals(_text.charAt(pos), padChar))) {
        pos += 1
      }
      _text = _text.subSequence(0, endPos)
      val resultPos: Int = printerParser.parse(context, _text, pos)
      if (resultPos != endPos && strict)
        return ~(position + pos)
      resultPos
    }

    override def toString: String = s"Pad($printerParser,$padWidth${if (padChar == ' ') ")" else ",'" + padChar + "')"}"
  }

  /** Enumeration to apply simple parse settings. */
  private[format] object SettingsParser {
    val SENSITIVE   = new SettingsParser("SENSITIVE", 0)
    val INSENSITIVE = new SettingsParser("INSENSITIVE", 1)
    val STRICT      = new SettingsParser("STRICT", 2)
    val LENIENT     = new SettingsParser("LENIENT", 3)
  }

  private[format] final class SettingsParser private(name: String, ordinal: Int) extends Enum[SettingsParser](name, ordinal) with DateTimePrinterParser {
    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = true

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      ordinal match {
        case 0 => context.setCaseSensitive(true)
        case 1 => context.setCaseSensitive(false)
        case 2 => context.setStrict(true)
        case 3 => context.setStrict(false)
      }
      position
    }

    override def toString: String =
      ordinal match {
        case 0 => "ParseCaseSensitive(true)"
        case 1 => "ParseCaseSensitive(false)"
        case 2 => "ParseStrict(true)"
        case 3 => "ParseStrict(false)"
        case _ => throw new IllegalStateException("Unreachable")
      }
  }

  /** Used by parseDefaulting(). */
  private[format] class DefaultingParser private[format](private val field: TemporalField, private val value: Long) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = true

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      if (context.getParsed(field) == null)
        context.setParsedField(field, value, position, position)
      position
    }
  }

  /** Prints or parses a character literal. */
  private[format] final class CharLiteralPrinterParser private[format](private val literal: Char) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      buf.append(literal)
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val length: Int = text.length
      if (position == length)
        return ~position
      val ch: Char = text.charAt(position)
      if (!context.charEquals(literal, ch))
        return ~position
      position + 1
    }

    override def toString: String =
      if (literal == '\'') "''"
      else s"'$literal'"
  }

  /** Prints or parses a string literal. */
  private[format] final class StringLiteralPrinterParser private[format](private val literal: String) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      buf.append(literal)
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val length: Int = text.length
      if (position > length || position < 0)
        throw new IndexOutOfBoundsException
      else if (!context.subSequenceEquals(text, position, literal, 0, literal.length))
        ~position
      else
        position + literal.length
    }

    override def toString: String = {
      val converted: String = literal.replace("'", "''")
      s"'$converted'"
    }
  }

  /** Prints and parses a numeric date-time field with optional padding. */
  private[format] object NumberPrinterParser {
    /** Array of 10 to the power of n. */
    private[format] val EXCEED_POINTS: Array[Int] = Array[Int](0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000)
  }

    /** @constructor
      *
      * @param field  the field to print, not null
      * @param minWidth  the minimum field width, from 1 to 19
      * @param maxWidth  the maximum field width, from minWidth to 19
      * @param signStyle  the positive/negative sign style, not null
      * @param subsequentWidth  the width of subsequent non-negative numbers, 0 or greater,
      *                         -1 if fixed width due to active adjacent parsing
      */
  private[format] class NumberPrinterParser private[format](private[format] val field: TemporalField,
                                                            private[format] val minWidth: Int,
                                                            private[format] val maxWidth: Int,
                                                            private[format] val signStyle: SignStyle,
                                                            private[format] val subsequentWidth: Int) extends DateTimePrinterParser {

    /** @constructor
      *
      * @param field  the field to print, not null
      * @param minWidth  the minimum field width, from 1 to 19
      * @param maxWidth  the maximum field width, from minWidth to 19
      * @param signStyle  the positive/negative sign style, not null
      */
    private[format] def this(field: TemporalField, minWidth: Int, maxWidth: Int, signStyle: SignStyle) {
      this(field, minWidth, maxWidth, signStyle, 0)
    }

    /** Returns a new instance with fixed width flag set.
      *
      * @return a new updated printer-parser, not null
      */
    private[format] def withFixedWidth: NumberPrinterParser =
      if (subsequentWidth == -1)
        this
      else
        new NumberPrinterParser(field, minWidth, maxWidth, signStyle, -1)

    /** Returns a new instance with an updated subsequent width.
      *
      * @param subsequentWidth  the width of subsequent non-negative numbers, 0 or greater
      * @return a new updated printer-parser, not null
      */
    private[format] def withSubsequentWidth(subsequentWidth: Int): NumberPrinterParser =
      new NumberPrinterParser(field, minWidth, maxWidth, signStyle, this.subsequentWidth + subsequentWidth)

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val valueLong: java.lang.Long = context.getValue(field)
      if (valueLong == null)
        return false
      val value: Long = getValue(context, valueLong)
      val symbols: DecimalStyle = context.getSymbols
      var str: String = if (value == Long.MinValue) "9223372036854775808" else Math.abs(value).toString
      if (str.length > maxWidth)
        throw new DateTimeException(s"Field $field cannot be printed as the value $value exceeds the maximum print width of $maxWidth")
      str = symbols.convertNumberToI18N(str)

      import SignStyle._
      if (value >= 0) {
        signStyle match {
          case EXCEEDS_PAD =>
            if (minWidth < 19 && value >= NumberPrinterParser.EXCEED_POINTS(minWidth))
              buf.append(symbols.getPositiveSign)
          case ALWAYS      =>
            buf.append(symbols.getPositiveSign)
          case _           =>
        }
      }
      else {
        signStyle match {
          case NORMAL | EXCEEDS_PAD | ALWAYS =>
            buf.append(symbols.getNegativeSign)
          case NOT_NEGATIVE                  =>
            throw new DateTimeException(s"Field $field cannot be printed as the value $value cannot be negative according to the SignStyle")
          case _                             =>
        }
      }

       var i: Int = 0
       while (i < minWidth - str.length) {
         buf.append(symbols.getZeroDigit)
         i += 1
       }

      buf.append(str)
      true
    }

    /** Gets the value to output.
      *
      * @param context  the context
      * @param value  the value of the field, not null
      * @return the value
      */
    private[format] def getValue(context: DateTimePrintContext, value: Long): Long = value

    private[format] def isFixedWidth(context: DateTimeParseContext): Boolean =
      subsequentWidth == -1 || (subsequentWidth > 0 && minWidth == maxWidth && (signStyle eq SignStyle.NOT_NEGATIVE))

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      var _position = position

      val length: Int = text.length
      if (_position == length)
        return ~_position
      val sign: Char = text.charAt(_position)
      var negative: Boolean = false
      var positive: Boolean = false
      if (sign == context.getSymbols.getPositiveSign) {
        if (!signStyle.parse(true, context.isStrict, minWidth == maxWidth))
          return ~_position
        positive = true
        _position += 1
      }
      else if (sign == context.getSymbols.getNegativeSign) {
        if (!signStyle.parse(false, context.isStrict, minWidth == maxWidth))
          return ~_position
        negative = true
        _position += 1
      }
      else {
        if ((signStyle eq SignStyle.ALWAYS) && context.isStrict)
          return ~_position
      }
      val effMinWidth: Int = if (context.isStrict || isFixedWidth(context)) minWidth else 1
      val minEndPos: Int = _position + effMinWidth
      if (minEndPos > length)
        return ~_position
      var effMaxWidth: Int = (if (context.isStrict || isFixedWidth(context)) maxWidth else 9) + Math.max(subsequentWidth, 0)
      var total: Long = 0
      var totalBig: BigInteger = null
      var pos: Int = _position

      var pass: Int = 0
      scala.util.control.Breaks.breakable {
        while (pass < 2) {
          val maxEndPos: Int = Math.min(pos + effMaxWidth, length)
          scala.util.control.Breaks.breakable {
            while (pos < maxEndPos) {
              val ch: Char = text.charAt(pos)
              pos += 1
              val digit: Int = context.getSymbols.convertToDigit(ch)
              if (digit < 0) {
                pos -= 1
                if (pos < minEndPos) {
                  return ~_position
                }
                scala.util.control.Breaks.break()
              }
              if ((pos - _position) > 18) {
                if (totalBig == null) {
                  totalBig = BigInteger.valueOf(total)
                }
                totalBig = totalBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit))
              }
              else {
                total = total * 10 + digit
              }
            }
          }
          if (subsequentWidth > 0 && pass == 0) {
            val parseLen: Int = pos - _position
            effMaxWidth = Math.max(effMinWidth, parseLen - subsequentWidth)
            pos = _position
            total = 0
            totalBig = null
          }
          else {
            scala.util.control.Breaks.break()
          }
          pass += 1
        }
      }
      if (negative) {
        if (totalBig != null) {
          if ((totalBig == BigInteger.ZERO) && context.isStrict) {
            return ~(_position - 1)
          }
          totalBig = totalBig.negate
        }
        else {
          if (total == 0 && context.isStrict) {
            return ~(_position - 1)
          }
          total = -total
        }
      }
      else if ((signStyle eq SignStyle.EXCEEDS_PAD) && context.isStrict) {
        val parseLen: Int = pos - _position
        if (positive) {
          if (parseLen <= minWidth) {
            return ~(_position - 1)
          }
        }
        else {
          if (parseLen > minWidth) {
            return ~_position
          }
        }
      }
      if (totalBig != null) {
        if (totalBig.bitLength > 63) {
          totalBig = totalBig.divide(BigInteger.TEN)
          pos -= 1
        }
        return setValue(context, totalBig.longValue, _position, pos)
      }
      setValue(context, total, _position, pos)
    }

    /** Stores the value.
      *
      * @param context  the context to store into, not null
      * @param value  the value
      * @param errorPos  the position of the field being parsed
      * @param successPos  the position after the field being parsed
      * @return the new position
      */
    private[format] def setValue(context: DateTimeParseContext, value: Long, errorPos: Int, successPos: Int): Int =
      context.setParsedField(field, value, errorPos, successPos)

    override def toString: String =
      if (minWidth == 1 && maxWidth == 19 && (signStyle eq SignStyle.NORMAL))
        s"Value($field)"
      else if (minWidth == maxWidth && (signStyle eq SignStyle.NOT_NEGATIVE))
        s"Value($field,$minWidth)"
      else
        s"Value($field,$minWidth,$maxWidth,$signStyle)"
  }

  /** Prints and parses a reduced numeric date-time field. */
  private[format] object ReducedPrinterParser {
    private[format] val BASE_DATE: LocalDate = LocalDate.of(2000, 1, 1)
  }

    /** @constructor
      *
      * @param field  the field to print, validated not null
      * @param minWidth  the field width, from 1 to 10
      * @param maxWidth  the field max width, from 1 to 10
      * @param baseValue  the base value
      * @param baseDate  the base date
      */
  private[format] final class ReducedPrinterParser private[format](field: TemporalField,
                                                                   minWidth: Int,
                                                                   maxWidth: Int,
                                                                   private val baseValue: Int,
                                                                   private val baseDate: ChronoLocalDate,
                                                                   subsequentWidth: Int)
      extends NumberPrinterParser(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth) {

      if (minWidth < 1 || minWidth > 10)
        throw new IllegalArgumentException(s"The width must be from 1 to 10 inclusive but was $minWidth")
      if (maxWidth < 1 || maxWidth > 10)
        throw new IllegalArgumentException(s"The maxWidth must be from 1 to 10 inclusive but was $maxWidth")
      if (maxWidth < minWidth)
        throw new IllegalArgumentException("The maxWidth must be greater than the width")
      if (baseDate == null){
        if (!field.range.isValidValue(baseValue))
          throw new IllegalArgumentException("The base value must be within the range of the field")
        if ((baseValue.toLong + NumberPrinterParser.EXCEED_POINTS(minWidth)) > Int.MaxValue)
          throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int")
      }

    private[format] def this(field: TemporalField, minWidth: Int, maxWidth: Int, baseValue: Int, baseDate: ChronoLocalDate) {
      this(field, minWidth, maxWidth, baseValue, baseDate, 0)
    }

    private[format] override def getValue(context: DateTimePrintContext, value: Long): Long = {
      val absValue: Long = Math.abs(value)
      var baseValue: Int = this.baseValue
      if (baseDate != null) {
        val chrono: Chronology = Chronology.from(context.getTemporal)
        baseValue = chrono.date(baseDate).get(field)
      }
      if (value >= baseValue && value < baseValue + NumberPrinterParser.EXCEED_POINTS(minWidth)) {
        return absValue % NumberPrinterParser.EXCEED_POINTS(minWidth)
      }
      absValue % NumberPrinterParser.EXCEED_POINTS(maxWidth)
    }

    private[format] override def setValue(context: DateTimeParseContext, value: Long, errorPos: Int, successPos: Int): Int = {
      var _value = value

      var baseValue: Int = this.baseValue
      if (baseDate != null) {
        val chrono: Chronology = context.getEffectiveChronology
        baseValue = chrono.date(baseDate).get(field)
        context.addChronologyChangedParser(this, _value, errorPos, successPos)
      }
      val parseLen: Int = successPos - errorPos
      if (parseLen == minWidth && _value >= 0) {
        val range: Long = NumberPrinterParser.EXCEED_POINTS(minWidth)
        val lastPart: Long = baseValue % range
        val basePart: Long = baseValue - lastPart
        if (baseValue > 0) {
          _value = basePart + _value
        }
        else {
          _value = basePart - _value
        }
        if (_value < baseValue) {
          _value += range
        }
      }
      context.setParsedField(field, _value, errorPos, successPos)
    }

    private[format] override def withFixedWidth: NumberPrinterParser =
      if (subsequentWidth == -1)
        this
      else
        new ReducedPrinterParser(field, minWidth, maxWidth, baseValue, baseDate, -1)

    private[format] override def withSubsequentWidth(subsequentWidth: Int): ReducedPrinterParser =
      new ReducedPrinterParser(field, minWidth, maxWidth, baseValue, baseDate, this.subsequentWidth + subsequentWidth)

    private[format] override def isFixedWidth(context: DateTimeParseContext): Boolean = {
      if (!context.isStrict)
        false
      else
        super.isFixedWidth(context)
    }

    override def toString: String = {
      s"ReducedValue($field,$minWidth,$maxWidth,${if (baseDate != null) baseDate else baseValue})"
    }
  }

  /** Prints and parses a numeric date-time field with optional padding.
    *
    * @constructor
    *
    * @param field  the field to output, not null
    * @param minWidth  the minimum width to output, from 0 to 9
    * @param maxWidth  the maximum width to output, from 0 to 9
    * @param decimalPoint  whether to output the localized decimal point symbol
    */
  private[format] final class FractionPrinterParser private[format](private val field: TemporalField,
                                                                    private val minWidth: Int,
                                                                    private val maxWidth: Int,
                                                                    private val decimalPoint: Boolean)
    extends DateTimePrinterParser {
    Objects.requireNonNull(field, "field")
    if (!field.range.isFixed)
      throw new IllegalArgumentException(s"Field must have a fixed set of values: $field")
    if (minWidth < 0 || minWidth > 9)
      throw new IllegalArgumentException(s"Minimum width must be from 0 to 9 inclusive but was $minWidth")
    if (maxWidth < 1 || maxWidth > 9)
      throw new IllegalArgumentException(s"Maximum width must be from 1 to 9 inclusive but was $maxWidth")
    if (maxWidth < minWidth)
      throw new IllegalArgumentException(s"Maximum width must exceed or equal the minimum width but $maxWidth < $minWidth")

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val value: java.lang.Long = context.getValue(field)
      if (value == null) {
        return false
      }
      val symbols: DecimalStyle = context.getSymbols
      var fraction: BigDecimal = convertToFraction(value)
      if (fraction.scale == 0) {
        if (minWidth > 0) {
          if (decimalPoint)
            buf.append(symbols.getDecimalSeparator)
          var i: Int = 0
          while (i < minWidth) {
            buf.append(symbols.getZeroDigit)
            i += 1
          }
        }
      }
      else {
        val outputScale: Int = Math.min(Math.max(fraction.scale, minWidth), maxWidth)
        fraction = fraction.setScale(outputScale, RoundingMode.FLOOR)
        var str: String = fraction.toPlainString.substring(2)
        str = symbols.convertNumberToI18N(str)
        if (decimalPoint) {
          buf.append(symbols.getDecimalSeparator)
        }
        buf.append(str)
      }
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      var _position = position

      val effectiveMin: Int = if (context.isStrict) minWidth else 0
      val effectiveMax: Int = if (context.isStrict) maxWidth else 9
      val length: Int = text.length
      if (_position == length)
        return if (effectiveMin > 0) ~_position else _position
      if (decimalPoint) {
        if (text.charAt(_position) != context.getSymbols.getDecimalSeparator) {
          return if (effectiveMin > 0) ~_position else _position
        }
        _position += 1
      }
      val minEndPos: Int = _position + effectiveMin
      if (minEndPos > length) {
        return ~_position
      }
      val maxEndPos: Int = Math.min(_position + effectiveMax, length)
      var total: Int = 0
      var pos: Int = _position
      scala.util.control.Breaks.breakable {
        while (pos < maxEndPos) {
          val ch: Char = text.charAt(pos)
          pos += 1
          val digit: Int = context.getSymbols.convertToDigit(ch)
          if (digit < 0) {
            if (pos < minEndPos) {
              return ~_position
            }
            pos -= 1
            scala.util.control.Breaks.break()
          }
          total = total * 10 + digit
        }
      }
      val fraction: BigDecimal = new BigDecimal(total).movePointLeft(pos - _position)
      val value: Long = convertFromFraction(fraction)
      context.setParsedField(field, value, _position, pos)
    }

    /** Converts a value for this field to a fraction between 0 and 1.
      *
      * The fractional value is between 0 (inclusive) and 1 (exclusive).
      * It can only be returned if the {@link TemporalField#range() value range} is fixed.
      * The fraction is obtained by calculation from the field range using 9 decimal
      * places and a rounding mode of {@link RoundingMode#FLOOR FLOOR}.
      * The calculation is inaccurate if the values do not run continuously from smallest to largest.
      *
      * For example, the second-of-minute value of 15 would be returned as 0.25,
      * assuming the standard definition of 60 seconds in a minute.
      *
      * @param value  the value to convert, must be valid for this rule
      * @return the value as a fraction within the range, from 0 to 1, not null
      * @throws DateTimeException if the value cannot be converted to a fraction
      */
    private def convertToFraction(value: Long): BigDecimal = {
      val range: ValueRange = field.range
      range.checkValidValue(value, field)
      val minBD: BigDecimal = BigDecimal.valueOf(range.getMinimum)
      val rangeBD: BigDecimal = BigDecimal.valueOf(range.getMaximum).subtract(minBD).add(BigDecimal.ONE)
      val valueBD: BigDecimal = BigDecimal.valueOf(value).subtract(minBD)
      val fraction: BigDecimal = valueBD.divide(rangeBD, 9, RoundingMode.FLOOR)
      if (fraction.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else fraction.stripTrailingZeros
    }

    /** Converts a fraction from 0 to 1 for this field to a value.
      *
      * The fractional value must be between 0 (inclusive) and 1 (exclusive).
      * It can only be returned if the {@link TemporalField#range() value range} is fixed.
      * The value is obtained by calculation from the field range and a rounding
      * mode of {@link RoundingMode#FLOOR FLOOR}.
      * The calculation is inaccurate if the values do not run continuously from smallest to largest.
      *
      * For example, the fractional second-of-minute of 0.25 would be converted to 15,
      * assuming the standard definition of 60 seconds in a minute.
      *
      * @param fraction  the fraction to convert, not null
      * @return the value of the field, valid for this rule
      * @throws DateTimeException if the value cannot be converted
      */
    private def convertFromFraction(fraction: BigDecimal): Long = {
      val range: ValueRange = field.range
      val minBD: BigDecimal = BigDecimal.valueOf(range.getMinimum)
      val rangeBD: BigDecimal = BigDecimal.valueOf(range.getMaximum).subtract(minBD).add(BigDecimal.ONE)
      val valueBD: BigDecimal = fraction.multiply(rangeBD).setScale(0, RoundingMode.FLOOR).add(minBD)
      valueBD.longValueExact
    }

    override def toString: String = {
      val decimal: String = if (decimalPoint) ",DecimalPoint" else ""
      s"Fraction($field,$minWidth,$maxWidth$decimal)"
    }
  }

  /** Prints or parses field text.
    *
    * @constructor
    *
    * @param field  the field to output, not null
    * @param textStyle  the text style, not null
    * @param provider  the text provider, not null
    */
  private[format] final class TextPrinterParser private[format](private val field: TemporalField, private val textStyle: TextStyle, private val provider: DateTimeTextProvider) extends DateTimePrinterParser {
    /** The cached number printer parser.
      * Immutable and volatile, so no synchronization needed.
      */
    @volatile
    private var _numberPrinterParser: DateTimeFormatterBuilder.NumberPrinterParser = null



    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val value: java.lang.Long = context.getValue(field)
      if (value == null) {
        return false
      }
      val text: String = provider.getText(field, value, textStyle, context.getLocale)
      if (text == null) {
        return numberPrinterParser.print(context, buf)
      }
      buf.append(text)
      true
    }

    def parse(context: DateTimeParseContext, parseText: CharSequence, position: Int): Int = {
      val length: Int = parseText.length
      if (position < 0 || position > length) {
        throw new IndexOutOfBoundsException
      }
      val style: TextStyle = if (context.isStrict) textStyle else null
      val it: java.util.Iterator[java.util.Map.Entry[String, Long]] = provider.getTextIterator(field, style, context.getLocale)
      if (it != null) {
        while (it.hasNext) {
          val entry: java.util.Map.Entry[String, Long] = it.next
          val itText: String = entry.getKey
          if (context.subSequenceEquals(itText, 0, parseText, position, itText.length)) {
            return context.setParsedField(field, entry.getValue, position, position + itText.length)
          }
        }
        if (context.isStrict) {
          return ~position
        }
      }
      numberPrinterParser.parse(context, parseText, position)
    }

    /** Create and cache a number printer parser.
      * @return the number printer parser for this field, not null
      */
    private def numberPrinterParser: DateTimeFormatterBuilder.NumberPrinterParser = {
      if (_numberPrinterParser == null)
        _numberPrinterParser = new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL)
      _numberPrinterParser
    }

    override def toString: String =
      if (textStyle eq TextStyle.FULL)
        s"Text($field)"
      else
        s"Text($field,$textStyle)"
  }

  /** Prints or parses an ISO-8601 instant. */
  private[format] object InstantPrinterParser {
    private val SECONDS_PER_10000_YEARS: Long = 146097L * 25L * 86400L
    private val SECONDS_0000_TO_1970: Long = ((146097L * 5L) - (30L * 365L + 7L)) * 86400L
  }

  private[format] final class InstantPrinterParser private[format](private val fractionalDigits: Int) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val inSecs: java.lang.Long = context.getValue(ChronoField.INSTANT_SECONDS)
      var inNanos: Long = 0L
      if (context.getTemporal.isSupported(ChronoField.NANO_OF_SECOND))
        inNanos = context.getTemporal.getLong(ChronoField.NANO_OF_SECOND)
      if (inSecs == null)
        return false
      val inSec: Long = inSecs
      var inNano: Int = ChronoField.NANO_OF_SECOND.checkValidIntValue(inNanos)
      if (inSec >= -InstantPrinterParser.SECONDS_0000_TO_1970) {
        val zeroSecs: Long = inSec - InstantPrinterParser.SECONDS_PER_10000_YEARS + InstantPrinterParser.SECONDS_0000_TO_1970
        val hi: Long = Math.floorDiv(zeroSecs, InstantPrinterParser.SECONDS_PER_10000_YEARS) + 1
        val lo: Long = Math.floorMod(zeroSecs, InstantPrinterParser.SECONDS_PER_10000_YEARS)
        val ldt: LocalDateTime = LocalDateTime.ofEpochSecond(lo - InstantPrinterParser.SECONDS_0000_TO_1970, 0, ZoneOffset.UTC)
        if (hi > 0)
          buf.append('+').append(hi)
        buf.append(ldt)
        if (ldt.getSecond == 0)
          buf.append(":00")
      }
      else {
        val zeroSecs: Long = inSec + InstantPrinterParser.SECONDS_0000_TO_1970
        val hi: Long = zeroSecs / InstantPrinterParser.SECONDS_PER_10000_YEARS
        val lo: Long = zeroSecs % InstantPrinterParser.SECONDS_PER_10000_YEARS
        val ldt: LocalDateTime = LocalDateTime.ofEpochSecond(lo - InstantPrinterParser.SECONDS_0000_TO_1970, 0, ZoneOffset.UTC)
        val pos: Int = buf.length
        buf.append(ldt)
        if (ldt.getSecond == 0)
          buf.append(":00")
        if (hi < 0) {
          if (ldt.getYear == -10000)
            buf.replace(pos, pos + 2, java.lang.Long.toString(hi - 1))
          else if (lo == 0)
            buf.insert(pos, hi)
          else
            buf.insert(pos + 1, Math.abs(hi))
        }
      }
      if (fractionalDigits == -2) {
        if (inNano != 0) {
          buf.append('.')
          if (inNano % 1000000 == 0)
            buf.append(Integer.toString((inNano / 1000000) + 1000).substring(1))
          else if (inNano % 1000 == 0)
            buf.append(Integer.toString((inNano / 1000) + 1000000).substring(1))
          else
            buf.append(Integer.toString(inNano + 1000000000).substring(1))
        }
      }
      else if (fractionalDigits > 0 || (fractionalDigits == -1 && inNano > 0)) {
        buf.append('.')
        var div: Int = 100000000
          var i: Int = 0
          while ((fractionalDigits == -1 && inNano > 0) || i < fractionalDigits) {
            val digit: Int = inNano / div
            buf.append((digit + '0').toChar)
            inNano = inNano - (digit * div)
            div = div / 10
            i += 1
          }
      }
      buf.append('Z')
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val newContext: DateTimeParseContext = context.copy
      val minDigits: Int = if (fractionalDigits < 0) 0 else fractionalDigits
      val maxDigits: Int = if (fractionalDigits < 0) 9 else fractionalDigits
      val parser: DateTimeFormatterBuilder.CompositePrinterParser =
        new DateTimeFormatterBuilder()
          .append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T')
          .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2)
          .appendFraction(ChronoField.NANO_OF_SECOND, minDigits, maxDigits, true).appendLiteral('Z').toFormatter.toPrinterParser(false)
      val pos: Int = parser.parse(newContext, text, position)
      if (pos < 0)
        return pos
      val yearParsed: Long = newContext.getParsed(ChronoField.YEAR)
      val month: Int = newContext.getParsed(ChronoField.MONTH_OF_YEAR).intValue
      val day: Int = newContext.getParsed(ChronoField.DAY_OF_MONTH).intValue
      var hour: Int = newContext.getParsed(ChronoField.HOUR_OF_DAY).intValue
      val min: Int = newContext.getParsed(ChronoField.MINUTE_OF_HOUR).intValue
      val secVal: java.lang.Long = newContext.getParsed(ChronoField.SECOND_OF_MINUTE)
      val nanoVal: java.lang.Long = newContext.getParsed(ChronoField.NANO_OF_SECOND)
      var sec: Int = if (secVal != null) secVal.intValue else 0
      val nano: Int = if (nanoVal != null) nanoVal.intValue else 0
      val year: Int = yearParsed.toInt % 10000
      var days: Int = 0
      if (hour == 24 && min == 0 && sec == 0 && nano == 0) {
        hour = 0
        days = 1
      }
      else if (hour == 23 && min == 59 && sec == 60) {
        context.setParsedLeapSecond()
        sec = 59
      }
      var instantSecs: Long = 0L
      try {
        val ldt: LocalDateTime = LocalDateTime.of(year, month, day, hour, min, sec, 0).plusDays(days)
        instantSecs = ldt.toEpochSecond(ZoneOffset.UTC)
        instantSecs += Math.multiplyExact(yearParsed / 10000L, InstantPrinterParser.SECONDS_PER_10000_YEARS)
      }
      catch {
        case ex: RuntimeException => return ~position
      }
      var successPos: Int = pos
      successPos = context.setParsedField(ChronoField.INSTANT_SECONDS, instantSecs, position, successPos)
      context.setParsedField(ChronoField.NANO_OF_SECOND, nano, position, successPos)
    }

    override def toString: String = "Instant()"
  }

  /** Prints or parses an offset ID. */
  private[format] object OffsetIdPrinterParser {
    private[format] val PATTERNS: Array[String] = Array[String]("+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss", "+HH:MM:ss", "+HHMMSS", "+HH:MM:SS")
    private[format] val INSTANCE_ID: OffsetIdPrinterParser = new OffsetIdPrinterParser("Z", "+HH:MM:ss")
  }

    /** @constructor
      *
      * @param noOffsetText  the text to use for UTC, not null
      * @param pattern  the pattern
      */
  private[format] final class OffsetIdPrinterParser private[format](private val noOffsetText: String, pattern: String) extends DateTimePrinterParser {
    Objects.requireNonNull(noOffsetText, "noOffsetText")
    Objects.requireNonNull(pattern, "pattern")

    private val `type`: Int = checkPattern(pattern)

    private def checkPattern(pattern: String): Int = {
      var i: Int = 0
      while (i < OffsetIdPrinterParser.PATTERNS.length) {
        if (OffsetIdPrinterParser.PATTERNS(i) == pattern)
          return i
        i += 1
      }
      throw new IllegalArgumentException(s"Invalid zone offset pattern: $pattern")
    }

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val offsetSecs: java.lang.Long = context.getValue(ChronoField.OFFSET_SECONDS)
      if (offsetSecs == null) {
        return false
      }
      val totalSecs: Int = Math.toIntExact(offsetSecs)
      if (totalSecs == 0) {
        buf.append(noOffsetText)
      }
      else {
        val absHours: Int = Math.abs((totalSecs / 3600) % 100)
        val absMinutes: Int = Math.abs((totalSecs / 60) % 60)
        val absSeconds: Int = Math.abs(totalSecs % 60)
        val bufPos: Int = buf.length
        var output: Int = absHours
        buf.append(if (totalSecs < 0) "-" else "+").append((absHours / 10 + '0').toChar).append((absHours % 10 + '0').toChar)
        if (`type` >= 3 || (`type` >= 1 && absMinutes > 0)) {
          buf.append(if ((`type` % 2) == 0) ":" else "").append((absMinutes / 10 + '0').toChar).append((absMinutes % 10 + '0').toChar)
          output += absMinutes
          if (`type` >= 7 || (`type` >= 5 && absSeconds > 0)) {
            buf.append(if ((`type` % 2) == 0) ":" else "").append((absSeconds / 10 + '0').toChar).append((absSeconds % 10 + '0').toChar)
            output += absSeconds
          }
        }
        if (output == 0) {
          buf.setLength(bufPos)
          buf.append(noOffsetText)
        }
      }
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val length: Int = text.length
      val noOffsetLen: Int = noOffsetText.length
      if (noOffsetLen == 0) {
        if (position == length) {
          return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, position)
        }
      }
      else {
        if (position == length) {
          return ~position
        }
        if (context.subSequenceEquals(text, position, noOffsetText, 0, noOffsetLen)) {
          return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, position + noOffsetLen)
        }
      }
      val sign: Char = text.charAt(position)
      if (sign == '+' || sign == '-') {
        val negative: Int = if (sign == '-') -1 else 1
        val array: Array[Int] = new Array[Int](4)
        array(0) = position + 1
        if (!(parseNumber(array, 1, text, true) || parseNumber(array, 2, text, `type` >= 3) || parseNumber(array, 3, text, false))) {
          val offsetSecs: Long = negative * (array(1) * 3600L + array(2) * 60L + array(3))
          return context.setParsedField(ChronoField.OFFSET_SECONDS, offsetSecs, position, array(0))
        }
      }
      if (noOffsetLen == 0) {
        return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, position, position + noOffsetLen)
      }
      ~position
    }

    /** Parse a two digit zero-prefixed number.
      *
      * @param array  the array of parsed data, 0=pos,1=hours,2=mins,3=secs, not null
      * @param arrayIndex  the index to parse the value into
      * @param parseText  the offset ID, not null
      * @param required  whether this number is required
      * @return true if an error occurred
      */
    private def parseNumber(array: Array[Int], arrayIndex: Int, parseText: CharSequence, required: Boolean): Boolean = {
      if ((`type` + 3) / 2 < arrayIndex) {
        return false
      }
      var pos: Int = array(0)
      if ((`type` % 2) == 0 && arrayIndex > 1) {
        if (pos + 1 > parseText.length || parseText.charAt(pos) != ':') {
          return required
        }
        pos += 1
      }
      if (pos + 2 > parseText.length) {
        return required
      }
      val ch1: Char = parseText.charAt(pos)
      pos += 1
      val ch2: Char = parseText.charAt(pos)
      pos += 1
      if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
        return required
      }
      val value: Int = (ch1 - 48) * 10 + (ch2 - 48)
      if (value < 0 || value > 59) {
        return required
      }
      array(arrayIndex) = value
      array(0) = pos
      false
    }

    override def toString: String = {
      val converted: String = noOffsetText.replace("'", "''")
      s"Offset(${OffsetIdPrinterParser.PATTERNS(`type`)},'$converted')"
    }
  }

  /** Prints or parses a localized offset. */
  private[format] final class LocalizedOffsetPrinterParser(private val style: TextStyle) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val offsetSecs: java.lang.Long = context.getValue(ChronoField.OFFSET_SECONDS)
      if (offsetSecs == null) {
        return false
      }
      buf.append("GMT")
      if (style eq TextStyle.FULL) {
        return new OffsetIdPrinterParser("", "+HH:MM:ss").print(context, buf)
      }
      val totalSecs: Int = Math.toIntExact(offsetSecs)
      if (totalSecs != 0) {
        val absHours: Int = Math.abs((totalSecs / 3600) % 100)
        val absMinutes: Int = Math.abs((totalSecs / 60) % 60)
        val absSeconds: Int = Math.abs(totalSecs % 60)
        buf.append(if (totalSecs < 0) "-" else "+").append(absHours)
        if (absMinutes > 0 || absSeconds > 0) {
          buf.append(":").append((absMinutes / 10 + '0').toChar).append((absMinutes % 10 + '0').toChar)
          if (absSeconds > 0) {
            buf.append(":").append((absSeconds / 10 + '0').toChar).append((absSeconds % 10 + '0').toChar)
          }
        }
      }
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      var _position = position

      if (!context.subSequenceEquals(text, _position, "GMT", 0, 3))
        return ~_position
      _position += 3
      if (style eq TextStyle.FULL)
        return new OffsetIdPrinterParser("", "+HH:MM:ss").parse(context, text, _position)
      val end: Int = text.length
      if (_position == end)
        return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, _position, _position)
      val sign: Char = text.charAt(_position)
      if (sign != '+' && sign != '-')
        return context.setParsedField(ChronoField.OFFSET_SECONDS, 0, _position, _position)
      val negative: Int = if (sign == '-') -1 else 1
      if (_position == end)
        return ~_position
      _position += 1
      var ch: Char = text.charAt(_position)
      if (ch < '0' || ch > '9')
        return ~_position
      _position += 1
      var hour: Int = ch - 48
      if (_position != end) {
        ch = text.charAt(_position)
        if (ch >= '0' && ch <= '9') {
          hour = hour * 10 + (ch - 48)
          if (hour > 23)
            return ~_position
          _position += 1
        }
      }
      if (_position == end || text.charAt(_position) != ':') {
        val offset: Int = negative * 3600 * hour
        return context.setParsedField(ChronoField.OFFSET_SECONDS, offset, _position, _position)
      }
      _position += 1
      if (_position > end - 2)
        return ~_position
      ch = text.charAt(_position)
      if (ch < '0' || ch > '9')
        return ~_position
      _position += 1
      var min: Int = ch - 48
      ch = text.charAt(_position)
      if (ch < '0' || ch > '9')
        return ~_position
      _position += 1
      min = min * 10 + (ch - 48)
      if (min > 59)
        return ~_position
      if (_position == end || text.charAt(_position) != ':') {
        val offset: Int = negative * (3600 * hour + 60 * min)
        return context.setParsedField(ChronoField.OFFSET_SECONDS, offset, _position, _position)
      }
      _position += 1
      if (_position > end - 2)
        return ~_position
      ch = text.charAt(_position)
      if (ch < '0' || ch > '9')
        return ~_position
      _position += 1
      var sec: Int = ch - 48
      ch = text.charAt(_position)
      if (ch < '0' || ch > '9')
        return ~_position
      _position += 1
      sec = sec * 10 + (ch - 48)
      if (sec > 59)
        return ~_position
      val offset: Int = negative * (3600 * hour + 60 * min + sec)
      context.setParsedField(ChronoField.OFFSET_SECONDS, offset, _position, _position)
    }
  }

  /** Prints or parses a zone ID. */
  private[format] object ZoneTextPrinterParser {
    /** The text style to output. */
    private val LENGTH_COMPARATOR: Ordering[String] =
      (str1: String, str2: String) => {
        var cmp: Int = str2.length - str1.length
        if (cmp == 0)
          cmp = str1.compareTo(str2)
        cmp
      }
  }

  private[format] final class ZoneTextPrinterParser private[format](private val textStyle: TextStyle) extends DateTimePrinterParser {
    Objects.requireNonNull(textStyle, "textStyle")

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val zone: ZoneId = context.getValue(TemporalQueries.zoneId)
      if (zone == null)
        return false
      if (zone.normalized.isInstanceOf[ZoneOffset]) {
        buf.append(zone.getId)
        return true
      }
      val temporal = context.getTemporal
      var daylight: Boolean = false
      if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
        val instant: Instant = Instant.ofEpochSecond(temporal.getLong(ChronoField.INSTANT_SECONDS))
        daylight = zone.getRules.isDaylightSavings(instant)
      }
      val tz: TimeZone = TimeZone.getTimeZone(zone.getId)
      val tzstyle: Int = if (textStyle.asNormal eq TextStyle.FULL) TimeZone.LONG else TimeZone.SHORT
      val text: String = tz.getDisplayName(daylight, tzstyle, context.getLocale)
      buf.append(text)
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      // TODO:
      //    1. Consider whether we should keep the Java-based implementation on the JVM
      //       and provide this alternative implementation only on other platforms?
      //    2. We need to write tests for this. I flipped LENGTH_COMPARATOR and no test broke.
      import scala.collection.mutable.{ Map => MutableMap }
      import scala.collection.immutable.TreeMap
      val ids = MutableMap[String, String]()
      val idIterator = ZoneId.getAvailableZoneIds.iterator()
      while (idIterator.hasNext) {
        val id = idIterator.next()
        ids(id) = id
        val tz: TimeZone = TimeZone.getTimeZone(id)
        val tzstyle: Int = if (textStyle.asNormal eq TextStyle.FULL) TimeZone.LONG else TimeZone.SHORT
        ids(tz.getDisplayName(false, tzstyle, context.getLocale)) = id
        ids(tz.getDisplayName(true, tzstyle, context.getLocale)) = id
      }
      val orderedIds = TreeMap(ids.toArray: _*)(ZoneTextPrinterParser.LENGTH_COMPARATOR)
      orderedIds.foreach { case (key, value) =>
        val name: String = key
        if (context.subSequenceEquals(text, position, name, 0, name.length)) {
          context.setParsed(ZoneId.of(value))
          return position + name.length
        }
      }
      ~position
    }

    override def toString: String = s"ZoneText($textStyle)"
  }

  /** Prints or parses a zone ID. */
  private[format] object ZoneIdPrinterParser {
    /** The cached tree to speed up parsing. */
    @volatile
    private var cachedSubstringTree: java.util.Map.Entry[Integer, SubstringTree] = null

    /** Model a tree of substrings to make the parsing easier. Due to the nature
      * of time-zone names, it can be faster to parse based in unique substrings
      * rather than just a character by character match.
      *
      * For example, to parse America/Denver we can look at the first two
      * character "Am". We then notice that the shortest time-zone that starts
      * with Am is America/Nome which is 12 characters long. Checking the first
      * 12 characters of America/Denver gives America/Denv which is a substring
      * of only 1 time-zone: America/Denver. Thus, with just 3 comparisons that
      * match can be found.
      *
      * This structure maps substrings to substrings of a longer length. Each
      * node of the tree contains a length and a map of valid substrings to
      * sub-nodes. The parser gets the length from the root node. It then
      * extracts a substring of that length from the parseText. If the map
      * contains the substring, it is set as the possible time-zone and the
      * sub-node for that substring is retrieved. The process continues until the
      * substring is no longer found, at which point the matched text is checked
      * against the real time-zones.
      *
      * @constructor
      *
      * @param length  The length of the substring this node of the tree contains.
      *                Subtrees will have a longer length.
      */
    private final class SubstringTree private[format](private[format] val length: Int) {
      /** Map of a substring to a set of substrings that contain the key. */
      private val substringMap: java.util.Map[CharSequence, SubstringTree] = new java.util.HashMap[CharSequence, SubstringTree]
      /** Map of a substring to a set of substrings that contain the key. */
      private val substringMapCI: java.util.Map[String, SubstringTree] = new java.util.HashMap[String, SubstringTree]


      private[format] def get(substring2: CharSequence, caseSensitive: Boolean): SubstringTree =
        if (caseSensitive) substringMap.get(substring2)
        else substringMapCI.get(CasePlatformHelper.toLocaleIndependentLowerCase(substring2.toString))

      /** Values must be added from shortest to longest.
        *
        * @param newSubstring  the substring to add, not null
        */
      @tailrec
      private[format] def add(newSubstring: String): Unit = {
        val idLen: Int = newSubstring.length
        if (idLen == length) {
          substringMap.put(newSubstring, null)
          substringMapCI.put(CasePlatformHelper.toLocaleIndependentLowerCase(newSubstring), null)
        }
        else if (idLen > length) {
          val substring: String = newSubstring.substring(0, length)
          var parserTree: SubstringTree = substringMap.get(substring)
          if (parserTree == null) {
            parserTree = new SubstringTree(idLen)
            substringMap.put(substring, parserTree)
            substringMapCI.put(CasePlatformHelper.toLocaleIndependentLowerCase(substring), parserTree)
          }
          parserTree.add(newSubstring)
        }
      }
    }

    /** Builds an optimized parsing tree.
      *
      * @param availableIDs  the available IDs, not null, not empty
      * @return the tree, not null
      */
    private def prepareParser(availableIDs: java.util.Set[String]): SubstringTree = {
      val ids: java.util.List[String] = new java.util.ArrayList[String](availableIDs)
      Collections.sort(ids, LENGTH_SORT)
      val tree: SubstringTree = new SubstringTree(ids.get(0).length)
      val idsIterator = ids.iterator
      while (idsIterator.hasNext) {
        val id = idsIterator.next()
        tree.add(id)
      }
      tree
    }
  }

  private[format] final class ZoneIdPrinterParser private[format](private val query: TemporalQuery[ZoneId], private val description: String) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val zone: ZoneId = context.getValue(query)
      if (zone == null)
        false
      else {
        buf.append(zone.getId)
        true
      }
    }

    /** This implementation looks for the longest matching string.
      * For example, parsing Etc/GMT-2 will return Etc/GMC-2 rather than just
      * Etc/GMC although both are valid.
      *
      * This implementation uses a tree to search for valid time-zone names in
      * the parseText. The top level node of the tree has a length equal to the
      * length of the shortest time-zone as well as the beginning characters of
      * all other time-zones.
      */
    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val length: Int = text.length
      if (position > length)
        throw new IndexOutOfBoundsException
      if (position == length)
        return ~position
      val nextChar: Char = text.charAt(position)
      if (nextChar == '+' || nextChar == '-') {
        val newContext: DateTimeParseContext = context.copy
        val endPos: Int = OffsetIdPrinterParser.INSTANCE_ID.parse(newContext, text, position)
        if (endPos < 0)
          return endPos
        val offset: Int = newContext.getParsed(ChronoField.OFFSET_SECONDS).longValue.toInt
        val zone: ZoneId = ZoneOffset.ofTotalSeconds(offset)
        context.setParsed(zone)
        return endPos
      }
      else if (length >= position + 2) {
        val nextNextChar: Char = text.charAt(position + 1)
        if (context.charEquals(nextChar, 'U') && context.charEquals(nextNextChar, 'T')) {
          if (length >= position + 3 && context.charEquals(text.charAt(position + 2), 'C'))
            return parsePrefixedOffset(context, text, position, position + 3)
          return parsePrefixedOffset(context, text, position, position + 2)
        }
        else if (context.charEquals(nextChar, 'G') && length >= position + 3 && context.charEquals(nextNextChar, 'M') && context.charEquals(text.charAt(position + 2), 'T'))
          return parsePrefixedOffset(context, text, position, position + 3)
      }
      val regionIds: java.util.Set[String] = ZoneRulesProvider.getAvailableZoneIds
      val regionIdsSize: Int = regionIds.size
      var cached: java.util.Map.Entry[Integer, SubstringTree] = ZoneIdPrinterParser.cachedSubstringTree
      if (cached == null || (cached.getKey != regionIdsSize)) {
        this synchronized {
          cached = ZoneIdPrinterParser.cachedSubstringTree
          if (cached == null || (cached.getKey != regionIdsSize)) {
            ZoneIdPrinterParser.cachedSubstringTree = {
              cached = new java.util.AbstractMap.SimpleImmutableEntry[Integer, SubstringTree](regionIdsSize, ZoneIdPrinterParser.prepareParser(regionIds))
              cached
            }
          }
        }
      }
      var tree: SubstringTree = cached.getValue
      var parsedZoneId: String = null
      var lastZoneId: String = null
      scala.util.control.Breaks.breakable {
        while (tree != null) {
          val nodeLength: Int = tree.length
          if (position + nodeLength > length)
            scala.util.control.Breaks.break()
          lastZoneId = parsedZoneId
          parsedZoneId = text.subSequence(position, position + nodeLength).toString
          tree = tree.get(parsedZoneId, context.isCaseSensitive)
        }
      }
      var zone: ZoneId = convertToZone(regionIds, parsedZoneId, context.isCaseSensitive)
      if (zone == null) {
        zone = convertToZone(regionIds, lastZoneId, context.isCaseSensitive)
        if (zone == null) {
          if (context.charEquals(nextChar, 'Z')) {
            context.setParsed(ZoneOffset.UTC)
            return position + 1
          }
          return ~position
        }
        parsedZoneId = lastZoneId
      }
      context.setParsed(zone)
      position + parsedZoneId.length
    }

    private def convertToZone(regionIds: java.util.Set[String], parsedZoneId: String, caseSensitive: Boolean): ZoneId =
      if (parsedZoneId == null)
        null
      else if (caseSensitive)
        if (regionIds.contains(parsedZoneId)) ZoneId.of(parsedZoneId) else null
      else {
        val regionIdsIterator = regionIds.iterator
        while (regionIdsIterator.hasNext) {
          val regionId = regionIdsIterator.next()
          if (regionId.equalsIgnoreCase(parsedZoneId))
            return ZoneId.of(regionId)
        }
        null
      }

    private def parsePrefixedOffset(context: DateTimeParseContext, text: CharSequence, prefixPos: Int, position: Int): Int = {
      val prefix: String = text.subSequence(prefixPos, position).toString.toUpperCase
      val newContext: DateTimeParseContext = context.copy
      if (position < text.length && context.charEquals(text.charAt(position), 'Z')) {
        context.setParsed(ZoneId.ofOffset(prefix, ZoneOffset.UTC))
        return position
      }
      val endPos: Int = OffsetIdPrinterParser.INSTANCE_ID.parse(newContext, text, position)
      if (endPos < 0) {
        context.setParsed(ZoneId.ofOffset(prefix, ZoneOffset.UTC))
        return position
      }
      val offsetSecs: Int = newContext.getParsed(ChronoField.OFFSET_SECONDS).longValue.toInt
      val offset: ZoneOffset = ZoneOffset.ofTotalSeconds(offsetSecs)
      context.setParsed(ZoneId.ofOffset(prefix, offset))
      endPos
    }

    override def toString: String = description
  }

  /** Prints or parses a chronology.
    *
    * @param textStyle The text style to output, null means the ID.
    */
  private[format] final class ChronoPrinterParser private[format](private val textStyle: TextStyle) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val chrono: Chronology = context.getValue(TemporalQueries.chronology)
      if (chrono == null)
        return false
      if (textStyle == null)
        buf.append(chrono.getId)
      else {
        val bundle: ResourceBundle = ResourceBundle.getBundle("org.threeten.bp.format.ChronologyText", context.getLocale, classOf[DateTimeFormatterBuilder].getClassLoader)
        try {
          val text: String = bundle.getString(chrono.getId)
          buf.append(text)
        }
        catch {
          case ex: MissingResourceException => buf.append(chrono.getId)
        }
      }
      true
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      if (position < 0 || position > text.length)
        throw new IndexOutOfBoundsException
      val chronos: java.util.Iterator[Chronology] = Chronology.getAvailableChronologies.iterator
      var bestMatch: Chronology = null
      var matchLen: Int = -1
      while (chronos.hasNext) {
        val chrono = chronos.next()
        val id: String = chrono.getId
        val idLen: Int = id.length
        if (idLen > matchLen && context.subSequenceEquals(text, position, id, 0, idLen)) {
          bestMatch = chrono
          matchLen = idLen
        }
      }
      if (bestMatch == null)
        ~position
      else {
        context.setParsed(bestMatch)
        position + matchLen
      }
    }
  }

  /** Prints or parses a localized pattern.
    *
    * @constructor
    *
    * @param dateStyle  the date style to use, may be null
    * @param timeStyle  the time style to use, may be null
    */
  private[format] final class LocalizedPrinterParser private[format](private val dateStyle: FormatStyle, private val timeStyle: FormatStyle) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val chrono: Chronology = Chronology.from(context.getTemporal)
      formatter(context.getLocale, chrono).toPrinterParser(false).print(context, buf)
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val chrono: Chronology = context.getEffectiveChronology
      formatter(context.getLocale, chrono).toPrinterParser(false).parse(context, text, position)
    }

    /** Gets the formatter to use.
      *
      * @param locale  the locale to use, not null
      * @return the formatter, not null
      * @throws IllegalArgumentException if the formatter cannot be found
      */
    private def formatter(locale: Locale, chrono: Chronology): DateTimeFormatter =
      DateTimeFormatStyleProvider.getInstance.getFormatter(dateStyle, timeStyle, chrono, locale)

    override def toString: String =
      s"Localized(${if (dateStyle != null) dateStyle else ""},${if (timeStyle != null) timeStyle else ""})"
  }

  /** Prints or parses a localized pattern. */
  private[format] final class WeekFieldsPrinterParser(private val letter: Char, private val count: Int) extends DateTimePrinterParser {

    def print(context: DateTimePrintContext, buf: StringBuilder): Boolean = {
      val weekFields: WeekFields = WeekFields.of(context.getLocale)
      val pp: DateTimeFormatterBuilder.DateTimePrinterParser = evaluate(weekFields)
      pp.print(context, buf)
    }

    def parse(context: DateTimeParseContext, text: CharSequence, position: Int): Int = {
      val weekFields: WeekFields = WeekFields.of(context.getLocale)
      val pp: DateTimeFormatterBuilder.DateTimePrinterParser = evaluate(weekFields)
      pp.parse(context, text, position)
    }

    private def evaluate(weekFields: WeekFields): DateTimeFormatterBuilder.DateTimePrinterParser = {
      var pp: DateTimeFormatterBuilder.DateTimePrinterParser = null
      letter match {
        case 'e' =>
          pp = new NumberPrinterParser(weekFields.dayOfWeek, count, 2, SignStyle.NOT_NEGATIVE)
        case 'c' =>
          pp = new NumberPrinterParser(weekFields.dayOfWeek, count, 2, SignStyle.NOT_NEGATIVE)
        case 'w' =>
          pp = new NumberPrinterParser(weekFields.weekOfWeekBasedYear, count, 2, SignStyle.NOT_NEGATIVE)
        case 'W' =>
          pp = new NumberPrinterParser(weekFields.weekOfMonth, 1, 2, SignStyle.NOT_NEGATIVE)
        case 'Y' =>
          if (count == 2)
            pp = new ReducedPrinterParser(weekFields.weekBasedYear, 2, 2, 0, ReducedPrinterParser.BASE_DATE)
          else
            pp = new NumberPrinterParser(weekFields.weekBasedYear, count, 19, if (count < 4) SignStyle.NORMAL else SignStyle.EXCEEDS_PAD, -1)
      }
      pp
    }

    override def toString: String = {
      val sb: StringBuilder = new StringBuilder(30)
      sb.append("Localized(")
      if (letter == 'Y') {
        if (count == 1)
          sb.append("WeekBasedYear")
        else if (count == 2)
          sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)")
        else
          sb.append("WeekBasedYear,").append(count).append(",").append(19).append(",").append(if (count < 4) SignStyle.NORMAL else SignStyle.EXCEEDS_PAD)
      }
      else {
        if (letter == 'c' || letter == 'e')
          sb.append("DayOfWeek")
        else if (letter == 'w')
          sb.append("WeekOfWeekBasedYear")
        else if (letter == 'W')
          sb.append("WeekOfMonth")
        sb.append(",")
        sb.append(count)
      }
      sb.append(")")
      sb.toString
    }
  }

  /** Length comparator. */
  private[format] val LENGTH_SORT: Comparator[String] = (str1: String, str2: String) => if (str1.length == str2.length) str1.compareTo(str2) else str1.length - str2.length
}

/** Builder to create date-time formatters.
  *
  * This allows a {@code DateTimeFormatter} to be created.
  * All date-time formatters are created ultimately using this builder.
  *
  * The basic elements of date-time can all be added:
  *<ul>
  * <li>Value - a numeric value</li>
  * <li>Fraction - a fractional value including the decimal place. Always use this when
  * outputting fractions to ensure that the fraction is parsed correctly</li>
  * <li>Text - the textual equivalent for the value</li>
  * <li>OffsetId/Offset - the {@linkplain ZoneOffset zone offset}</li>
  * <li>ZoneId - the {@linkplain ZoneId time-zone} id</li>
  * <li>ZoneText - the name of the time-zone</li>
  * <li>Literal - a text literal</li>
  * <li>Nested and Optional - formats can be nested or made optional</li>
  * <li>Other - the printer and parser interfaces can be used to add user supplied formatting</li>
  * </ul><p>
  * In addition, any of the elements may be decorated by padding, either with spaces or any other character.
  *
  * Finally, a shorthand pattern, mostly compatible with {@code java.text.SimpleDateFormat SimpleDateFormat}
  * can be used, see {@link #appendPattern(String)}.
  * In practice, this simply parses the pattern and calls other methods on the builder.
  *
  * <h3>Specification for implementors</h3>
  * This class is a mutable builder intended for use from a single thread.
  *
  * @constructor Constructs a new instance of the builder.
  *
  * @param parent  the parent builder, not null
  * @param optional  whether the formatter is optional, not null
  */
final class DateTimeFormatterBuilder private(private val parent: DateTimeFormatterBuilder, private val optional: Boolean) {
  /** Constructs a new instance of the builder. */
  def this() {
    this(null, false)
  }

  /** The currently active builder, used by the outermost builder. */
  private var active: DateTimeFormatterBuilder = this
  /** The list of printers that will be used. */
  private val printerParsers: java.util.List[DateTimeFormatterBuilder.DateTimePrinterParser] = new java.util.ArrayList[DateTimeFormatterBuilder.DateTimePrinterParser]
  /** The width to pad the next field to. */
  private var padNextWidth: Int = 0
  /** The character to pad the next field with. */
  private var padNextChar: Char = 0
  /** The index of the last variable width value parser. */
  private var valueParserIndex: Int = -1

  /** Changes the parse style to be case sensitive for the remainder of the formatter.
    *
    * Parsing can be case sensitive or insensitive - by default it is case sensitive.
    * This method allows the case sensitivity setting of parsing to be changed.
    *
    * Calling this method changes the state of the builder such that all
    * subsequent builder method calls will parse text in case sensitive mode.
    * See {@link #parseCaseInsensitive} for the opposite setting.
    * The parse case sensitive/insensitive methods may be called at any point
    * in the builder, thus the parser can swap between case parsing modes
    * multiple times during the parse.
    *
    * Since the default is case sensitive, this method should only be used after
    * a previous call to {@code #parseCaseInsensitive}.
    *
    * @return this, for chaining, not null
    */
  def parseCaseSensitive: DateTimeFormatterBuilder = {
    appendInternal(DateTimeFormatterBuilder.SettingsParser.SENSITIVE)
    this
  }

  /** Changes the parse style to be case insensitive for the remainder of the formatter.
    *
    * Parsing can be case sensitive or insensitive - by default it is case sensitive.
    * This method allows the case sensitivity setting of parsing to be changed.
    *
    * Calling this method changes the state of the builder such that all
    * subsequent builder method calls will parse text in case sensitive mode.
    * See {@link #parseCaseSensitive()} for the opposite setting.
    * The parse case sensitive/insensitive methods may be called at any point
    * in the builder, thus the parser can swap between case parsing modes
    * multiple times during the parse.
    *
    * @return this, for chaining, not null
    */
  def parseCaseInsensitive: DateTimeFormatterBuilder = {
    appendInternal(DateTimeFormatterBuilder.SettingsParser.INSENSITIVE)
    this
  }

  /** Changes the parse style to be strict for the remainder of the formatter.
    *
    * Parsing can be strict or lenient - by default its strict.
    * This controls the degree of flexibility in matching the text and sign styles.
    *
    * When used, this method changes the parsing to be strict from this point onwards.
    * As strict is the default, this is normally only needed after calling {@link #parseLenient()}.
    * The change will remain in force until the end of the formatter that is eventually
    * constructed or until {@code parseLenient} is called.
    *
    * @return this, for chaining, not null
    */
  def parseStrict: DateTimeFormatterBuilder = {
    appendInternal(DateTimeFormatterBuilder.SettingsParser.STRICT)
    this
  }

  /** Changes the parse style to be lenient for the remainder of the formatter.
    * Note that case sensitivity is set separately to this method.
    *
    * Parsing can be strict or lenient - by default its strict.
    * This controls the degree of flexibility in matching the text and sign styles.
    * Applications calling this method should typically also call {@link #parseCaseInsensitive()}.
    *
    * When used, this method changes the parsing to be strict from this point onwards.
    * The change will remain in force until the end of the formatter that is eventually
    * constructed or until {@code parseStrict} is called.
    *
    * @return this, for chaining, not null
    */
  def parseLenient: DateTimeFormatterBuilder = {
    appendInternal(DateTimeFormatterBuilder.SettingsParser.LENIENT)
    this
  }

  /** Appends a default value for a field to the formatter for use in parsing.
    *
    * This appends an instruction to the builder to inject a default value
    * into the parsed result. This is especially useful in conjunction with
    * optional parts of the formatter.
    *
    * For example, consider a formatter that parses the year, followed by
    * an optional month, with a further optional day-of-month. Using such a
    * formatter would require the calling code to check whether a full date,
    * year-month or just a year had been parsed. This method can be used to
    * default the month and day-of-month to a sensible value, such as the
    * first of the month, allowing the calling code to always get a date.
    *
    * During formatting, this method has no effect.
    *
    * During parsing, the current state of the parse is inspected.
    * If the specified field has no associated value, because it has not been
    * parsed successfully at that point, then the specified value is injected
    * into the parse result. Injection is immediate, thus the field-value pair
    * will be visible to any subsequent elements in the formatter.
    * As such, this method is normally called at the end of the builder.
    *
    * @param field  the field to default the value of, not null
    * @param value  the value to default the field to
    * @return this, for chaining, not null
    */
  def parseDefaulting(field: TemporalField, value: Long): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    appendInternal(new DateTimeFormatterBuilder.DefaultingParser(field, value))
    this
  }

  /** Appends the value of a date-time field to the formatter using a normal
    * output style.
    *
    * The value of the field will be output during a print.
    * If the value cannot be obtained then an exception will be thrown.
    *
    * The value will be printed as per the normal print of an integer value.
    * Only negative numbers will be signed. No padding will be added.
    *
    * The parser for a variable width value such as this normally behaves greedily,
    * requiring one digit, but accepting as many digits as possible.
    * This behavior can be affected by 'adjacent value parsing'.
    * See {@link #appendValue(TemporalField, int)} for full details.
    *
    * @param field  the field to append, not null
    * @return this, for chaining, not null
    */
  def appendValue(field: TemporalField): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    appendValue(new DateTimeFormatterBuilder.NumberPrinterParser(field, 1, 19, SignStyle.NORMAL))
    this
  }

  /** Appends the value of a date-time field to the formatter using a fixed
    * width, zero-padded approach.
    *
    * The value of the field will be output during a print.
    * If the value cannot be obtained then an exception will be thrown.
    *
    * The value will be zero-padded on the left. If the size of the value
    * means that it cannot be printed within the width then an exception is thrown.
    * If the value of the field is negative then an exception is thrown during printing.
    *
    * This method supports a special technique of parsing known as 'adjacent value parsing'.
    * This technique solves the problem where a variable length value is followed by one or more
    * fixed length values. The standard parser is greedy, and thus it would normally
    * steal the digits that are needed by the fixed width value parsers that follow the
    * variable width one.
    *
    * No action is required to initiate 'adjacent value parsing'.
    * When a call to {@code appendValue} with a variable width is made, the builder
    * enters adjacent value parsing setup mode. If the immediately subsequent method
    * call or calls on the same builder are to this method, then the parser will reserve
    * space so that the fixed width values can be parsed.
    *
    * For example, consider {@code builder.appendValue(YEAR).appendValue(MONTH_OF_YEAR, 2);}
    * The year is a variable width parse of between 1 and 19 digits.
    * The month is a fixed width parse of 2 digits.
    * Because these were appended to the same builder immediately after one another,
    * the year parser will reserve two digits for the month to parse.
    * Thus, the text '201106' will correctly parse to a year of 2011 and a month of 6.
    * Without adjacent value parsing, the year would greedily parse all six digits and leave
    * nothing for the month.
    *
    * Adjacent value parsing applies to each set of fixed width not-negative values in the parser
    * that immediately follow any kind of variable width value.
    * Calling any other append method will end the setup of adjacent value parsing.
    * Thus, in the unlikely event that you need to avoid adjacent value parsing behavior,
    * simply add the {@code appendValue} to another {@code DateTimeFormatterBuilder}
    * and add that to this builder.
    *
    * If adjacent parsing is active, then parsing must match exactly the specified
    * number of digits in both strict and lenient modes.
    * In addition, no positive or negative sign is permitted.
    *
    * @param field  the field to append, not null
    * @param width  the width of the printed field, from 1 to 19
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if the width is invalid
    */
  def appendValue(field: TemporalField, width: Int): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    if (width < 1 || width > 19)
      throw new IllegalArgumentException(s"The width must be from 1 to 19 inclusive but was $width")
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(field, width, width, SignStyle.NOT_NEGATIVE)
    appendValue(pp)
    this
  }

  /** Appends the value of a date-time field to the formatter providing full
    * control over printing.
    *
    * The value of the field will be output during a print.
    * If the value cannot be obtained then an exception will be thrown.
    *
    * This method provides full control of the numeric formatting, including
    * zero-padding and the positive/negative sign.
    *
    * The parser for a variable width value such as this normally behaves greedily,
    * accepting as many digits as possible.
    * This behavior can be affected by 'adjacent value parsing'.
    * See {@link #appendValue(TemporalField, int)} for full details.
    *
    * In strict parsing mode, the minimum number of parsed digits is {@code minWidth}.
    * In lenient parsing mode, the minimum number of parsed digits is one.
    *
    * If this method is invoked with equal minimum and maximum widths and a sign style of
    * {@code NOT_NEGATIVE} then it delegates to {@code appendValue(TemporalField,int)}.
    * In this scenario, the printing and parsing behavior described there occur.
    *
    * @param field  the field to append, not null
    * @param minWidth  the minimum field width of the printed field, from 1 to 19
    * @param maxWidth  the maximum field width of the printed field, from 1 to 19
    * @param signStyle  the positive/negative output style, not null
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if the widths are invalid
    */
  def appendValue(field: TemporalField, minWidth: Int, maxWidth: Int, signStyle: SignStyle): DateTimeFormatterBuilder = {
    if (minWidth == maxWidth && (signStyle eq SignStyle.NOT_NEGATIVE))
      return appendValue(field, maxWidth)
    Objects.requireNonNull(field, "field")
    Objects.requireNonNull(signStyle, "signStyle")
    if (minWidth < 1 || minWidth > 19)
      throw new IllegalArgumentException(s"The minimum width must be from 1 to 19 inclusive but was $minWidth")
    if (maxWidth < 1 || maxWidth > 19)
      throw new IllegalArgumentException(s"The maximum width must be from 1 to 19 inclusive but was $maxWidth")
    if (maxWidth < minWidth)
      throw new IllegalArgumentException(s"The maximum width must exceed or equal the minimum width but $maxWidth < $minWidth")
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(field, minWidth, maxWidth, signStyle)
    appendValue(pp)
    this
  }

  /** Appends the reduced value of a date-time field to the formatter.
    *
    * Since fields such as year vary by chronology, it is recommended to use the
    * {@link #appendValueReduced(TemporalField, int, int, ChronoLocalDate)} date}
    * variant of this method in most cases. This variant is suitable for
    * simple fields or working with only the ISO chronology.
    *
    * For formatting, the {@code width} and {@code maxWidth} are used to
    * determine the number of characters to format.
    * If they are equal then the format is fixed width.
    * If the value of the field is within the range of the {@code baseValue} using
    * {@code width} characters then the reduced value is formatted otherwise the value is
    * truncated to fit {@code maxWidth}.
    * The rightmost characters are output to match the width, left padding with zero.
    *
    * For strict parsing, the number of characters allowed by {@code width} to {@code maxWidth} are parsed.
    * For lenient parsing, the number of characters must be at least 1 and less than 10.
    * If the number of digits parsed is equal to {@code width} and the value is positive,
    * the value of the field is computed to be the first number greater than
    * or equal to the {@code baseValue} with the same least significant characters,
    * otherwise the value parsed is the field value.
    * This allows a reduced value to be entered for values in range of the baseValue
    * and width and absolute values can be entered for values outside the range.
    *
    * For example, a base value of {@code 1980} and a width of {@code 2} will have
    * valid values from {@code 1980} to {@code 2079}.
    * During parsing, the text {@code "12"} will result in the value {@code 2012} as that
    * is the value within the range where the last two characters are "12".
    * By contrast, parsing the text {@code "1915"} will result in the value {@code 1915}.
    *
    * @param field  the field to append, not null
    * @param width  the field width of the printed and parsed field, from 1 to 10
    * @param maxWidth  the maximum field width of the printed field, from 1 to 10
    * @param baseValue  the base value of the range of valid values
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if the width or base value is invalid
    */
  def appendValueReduced(field: TemporalField, width: Int, maxWidth: Int, baseValue: Int): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(field, width, maxWidth, baseValue, null)
    appendValue(pp)
    this
  }

  /** Appends the reduced value of a date-time field to the formatter.
    *
    * This is typically used for formatting and parsing a two digit year.
    *
    * The base date is used to calculate the full value during parsing.
    * For example, if the base date is 1950-01-01 then parsed values for
    * a two digit year parse will be in the range 1950-01-01 to 2049-12-31.
    * Only the year would be extracted from the date, thus a base date of
    * 1950-08-25 would also parse to the range 1950-01-01 to 2049-12-31.
    * This behavior is necessary to support fields such as week-based-year
    * or other calendar systems where the parsed value does not align with
    * standard ISO years.
    *
    * The exact behavior is as follows. Parse the full set of fields and
    * determine the effective chronology using the last chronology if
    * it appears more than once. Then convert the base date to the
    * effective chronology. Then extract the specified field from the
    * chronology-specific base date and use it to determine the
    * {@code baseValue} used below.
    *
    * For formatting, the {@code width} and {@code maxWidth} are used to
    * determine the number of characters to format.
    * If they are equal then the format is fixed width.
    * If the value of the field is within the range of the {@code baseValue} using
    * {@code width} characters then the reduced value is formatted otherwise the value is
    * truncated to fit {@code maxWidth}.
    * The rightmost characters are output to match the width, left padding with zero.
    *
    * For strict parsing, the number of characters allowed by {@code width} to {@code maxWidth} are parsed.
    * For lenient parsing, the number of characters must be at least 1 and less than 10.
    * If the number of digits parsed is equal to {@code width} and the value is positive,
    * the value of the field is computed to be the first number greater than
    * or equal to the {@code baseValue} with the same least significant characters,
    * otherwise the value parsed is the field value.
    * This allows a reduced value to be entered for values in range of the baseValue
    * and width and absolute values can be entered for values outside the range.
    *
    * For example, a base value of {@code 1980} and a width of {@code 2} will have
    * valid values from {@code 1980} to {@code 2079}.
    * During parsing, the text {@code "12"} will result in the value {@code 2012} as that
    * is the value within the range where the last two characters are "12".
    * By contrast, parsing the text {@code "1915"} will result in the value {@code 1915}.
    *
    * @param field  the field to append, not null
    * @param width  the field width of the printed and parsed field, from 1 to 10
    * @param maxWidth  the maximum field width of the printed field, from 1 to 10
    * @param baseDate  the base date used to calculate the base value for the range
    *                  of valid values in the parsed chronology, not null
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if the width or base value is invalid
    */
  def appendValueReduced(field: TemporalField, width: Int, maxWidth: Int, baseDate: ChronoLocalDate): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    Objects.requireNonNull(baseDate, "baseDate")
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(field, width, maxWidth, 0, baseDate)
    appendValue(pp)
    this
  }

  /** Appends a fixed width printer-parser.
    *
    * @param pp  the printer-parser, not null
    * @return this, for chaining, not null
    */
  private def appendValue(pp: DateTimeFormatterBuilder.NumberPrinterParser): DateTimeFormatterBuilder = {
    if (active.valueParserIndex >= 0 && active.printerParsers.get(active.valueParserIndex).isInstanceOf[DateTimeFormatterBuilder.NumberPrinterParser]) {
      val activeValueParser: Int = active.valueParserIndex
      var basePP: DateTimeFormatterBuilder.NumberPrinterParser = active.printerParsers.get(activeValueParser).asInstanceOf[DateTimeFormatterBuilder.NumberPrinterParser]
      if (pp.minWidth == pp.maxWidth && (pp.signStyle eq SignStyle.NOT_NEGATIVE)) {
        basePP = basePP.withSubsequentWidth(pp.maxWidth)
        appendInternal(pp.withFixedWidth)
        active.valueParserIndex = activeValueParser
      }
      else {
        basePP = basePP.withFixedWidth
        active.valueParserIndex = appendInternal(pp)
      }
      active.printerParsers.set(activeValueParser, basePP)
    }
    else {
      active.valueParserIndex = appendInternal(pp)
    }
    this
  }

  /** Appends the fractional value of a date-time field to the formatter.
    *
    * The fractional value of the field will be output including the
    * preceding decimal point. The preceding value is not output.
    * For example, the second-of-minute value of 15 would be output as {@code .25}.
    *
    * The width of the printed fraction can be controlled. Setting the
    * minimum width to zero will cause no output to be generated.
    * The printed fraction will have the minimum width necessary between
    * the minimum and maximum widths - trailing zeroes are omitted.
    * No rounding occurs due to the maximum width - digits are simply dropped.
    *
    * When parsing in strict mode, the number of parsed digits must be between
    * the minimum and maximum width. When parsing in lenient mode, the minimum
    * width is considered to be zero and the maximum is nine.
    *
    * If the value cannot be obtained then an exception will be thrown.
    * If the value is negative an exception will be thrown.
    * If the field does not have a fixed set of valid values then an
    * exception will be thrown.
    * If the field value in the date-time to be printed is invalid it
    * cannot be printed and an exception will be thrown.
    *
    * @param field  the field to append, not null
    * @param minWidth  the minimum width of the field excluding the decimal point, from 0 to 9
    * @param maxWidth  the maximum width of the field excluding the decimal point, from 1 to 9
    * @param decimalPoint  whether to output the localized decimal point symbol
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if the field has a variable set of valid values or
    *                                  either width is invalid
    */
  def appendFraction(field: TemporalField, minWidth: Int, maxWidth: Int, decimalPoint: Boolean): DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.FractionPrinterParser(field, minWidth, maxWidth, decimalPoint))
    this
  }

  /** Appends the text of a date-time field to the formatter using the full
    * text style.
    *
    * The text of the field will be output during a print.
    * The value must be within the valid range of the field.
    * If the value cannot be obtained then an exception will be thrown.
    * If the field has no textual representation, then the numeric value will be used.
    *
    * The value will be printed as per the normal print of an integer value.
    * Only negative numbers will be signed. No padding will be added.
    *
    * @param field  the field to append, not null
    * @return this, for chaining, not null
    */
  def appendText(field: TemporalField): DateTimeFormatterBuilder = appendText(field, TextStyle.FULL)

  /** Appends the text of a date-time field to the formatter.
    *
    * The text of the field will be output during a print.
    * The value must be within the valid range of the field.
    * If the value cannot be obtained then an exception will be thrown.
    * If the field has no textual representation, then the numeric value will be used.
    *
    * The value will be printed as per the normal print of an integer value.
    * Only negative numbers will be signed. No padding will be added.
    *
    * @param field  the field to append, not null
    * @param textStyle  the text style to use, not null
    * @return this, for chaining, not null
    */
  def appendText(field: TemporalField, textStyle: TextStyle): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    Objects.requireNonNull(textStyle, "textStyle")
    appendInternal(new DateTimeFormatterBuilder.TextPrinterParser(field, textStyle, DateTimeTextProvider.getInstance))
    this
  }

  /** Appends the text of a date-time field to the formatter using the specified
    * map to supply the text.
    *
    * The standard text outputting methods use the localized text in the JDK.
    * This method allows that text to be specified directly.
    * The supplied map is not validated by the builder to ensure that printing or
    * parsing is possible, thus an invalid map may throw an error during later use.
    *
    * Supplying the map of text provides considerable flexibility in printing and parsing.
    * For example, a legacy application might require or supply the months of the
    * year as "JNY", "FBY", "MCH" etc. These do not match the standard set of text
    * for localized month names. Using this method, a map can be created which
    * defines the connection between each value and the text:
    * <pre>
    * Map&lt;Long, String&gt; map = new HashMap&lt;&gt;();
    * map.put(1, "JNY");
    * map.put(2, "FBY");
    * map.put(3, "MCH");
    * ...
    * builder.appendText(MONTH_OF_YEAR, map);
    * </pre>
    *
    * Other uses might be to output the value with a suffix, such as "1st", "2nd", "3rd",
    * or as Roman numerals "I", "II", "III", "IV".
    *
    * During printing, the value is obtained and checked that it is in the valid range.
    * If text is not available for the value then it is output as a number.
    * During parsing, the parser will match against the map of text and numeric values.
    *
    * @param field  the field to append, not null
    * @param textLookup  the map from the value to the text
    * @return this, for chaining, not null
    */
  def appendText(field: TemporalField, textLookup: java.util.Map[Long, String]): DateTimeFormatterBuilder = {
    Objects.requireNonNull(field, "field")
    Objects.requireNonNull(textLookup, "textLookup")
    val copy: java.util.Map[Long, String] = new java.util.LinkedHashMap[Long, String](textLookup)
    val map: java.util.Map[TextStyle, java.util.Map[Long, String]] = Collections.singletonMap(TextStyle.FULL, copy)
    val store: SimpleDateTimeTextProvider.LocaleStore = new SimpleDateTimeTextProvider.LocaleStore(map)
    val provider: DateTimeTextProvider = new DateTimeTextProvider() {
      def getText(field: TemporalField, value: Long, style: TextStyle, locale: Locale): String = {
        store.getText(value, style)
      }

      def getTextIterator(field: TemporalField, style: TextStyle, locale: Locale): java.util.Iterator[java.util.Map.Entry[String, Long]] = {
        store.getTextIterator(style)
      }
    }
    appendInternal(new DateTimeFormatterBuilder.TextPrinterParser(field, TextStyle.FULL, provider))
    this
  }

  /** Appends an instant using ISO-8601 to the formatter, formatting fractional
    * digits in groups of three.
    *
    * Instants have a fixed output format.
    * They are converted to a date-time with a zone-offset of UTC and formatted
    * using the standard ISO-8601 format.
    * With this method, formatting nano-of-second outputs zero, three, six
    * or nine digits digits as necessary.
    * The localized decimal style is not used.
    *
    * The instant is obtained using {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS}
    * and optionally (@code NANO_OF_SECOND). The value of {@code INSTANT_SECONDS}
    * may be outside the maximum range of {@code LocalDateTime}.
    *
    * The {@linkplain ResolverStyle resolver style} has no effect on instant parsing.
    * The end-of-day time of '24:00' is handled as midnight at the start of the following day.
    * The leap-second time of '23:59:59' is handled to some degree, see
    * {@link DateTimeFormatter#parsedLeapSecond()} for full details.
    *
    * An alternative to this method is to format/parse the instant as a single
    * epoch-seconds value. That is achieved using {@code appendValue(INSTANT_SECONDS)}.
    *
    * @return this, for chaining, not null
    */
  def appendInstant: DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.InstantPrinterParser(-2))
    this
  }

  /** Appends an instant using ISO-8601 to the formatter with control over
    * the number of fractional digits.
    *
    * Instants have a fixed output format, although this method provides some
    * control over the fractional digits. They are converted to a date-time
    * with a zone-offset of UTC and printed using the standard ISO-8601 format.
    * The localized decimal style is not used.
    *
    * The {@code fractionalDigits} parameter allows the output of the fractional
    * second to be controlled. Specifying zero will cause no fractional digits
    * to be output. From 1 to 9 will output an increasing number of digits, using
    * zero right-padding if necessary. The special value -1 is used to output as
    * many digits as necessary to avoid any trailing zeroes.
    *
    * When parsing in strict mode, the number of parsed digits must match the
    * fractional digits. When parsing in lenient mode, any number of fractional
    * digits from zero to nine are accepted.
    *
    * The instant is obtained using {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS}
    * and optionally (@code NANO_OF_SECOND). The value of {@code INSTANT_SECONDS}
    * may be outside the maximum range of {@code LocalDateTime}.
    *
    * The {@linkplain ResolverStyle resolver style} has no effect on instant parsing.
    * The end-of-day time of '24:00' is handled as midnight at the start of the following day.
    * The leap-second time of '23:59:59' is handled to some degree, see
    * {@link DateTimeFormatter#parsedLeapSecond()} for full details.
    *
    * An alternative to this method is to format/parse the instant as a single
    * epoch-seconds value. That is achieved using {@code appendValue(INSTANT_SECONDS)}.
    *
    * @param fractionalDigits  the number of fractional second digits to format with,
    *                          from 0 to 9, or -1 to use as many digits as necessary
    * @return this, for chaining, not null
    */
  def appendInstant(fractionalDigits: Int): DateTimeFormatterBuilder =
    if (fractionalDigits < -1 || fractionalDigits > 9)
      throw new IllegalArgumentException(s"Invalid fractional digits: $fractionalDigits")
    else {
      appendInternal(new DateTimeFormatterBuilder.InstantPrinterParser(fractionalDigits))
      this
    }

  /** Appends the zone offset, such as '+01:00', to the formatter.
    *
    * This appends an instruction to print/parse the offset ID to the builder.
    * This is equivalent to calling {@code appendOffset("HH:MM:ss", "Z")}.
    *
    * @return this, for chaining, not null
    */
  def appendOffsetId: DateTimeFormatterBuilder = {
    appendInternal(DateTimeFormatterBuilder.OffsetIdPrinterParser.INSTANCE_ID)
    this
  }

  /** Appends the zone offset, such as '+01:00', to the formatter.
    *
    * This appends an instruction to print/parse the offset ID to the builder.
    *
    * During printing, the offset is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#offset()}.
    * It will be printed using the format defined below.
    * If the offset cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * During parsing, the offset is parsed using the format defined below.
    * If the offset cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * The format of the offset is controlled by a pattern which must be one
    * of the following:
    *<ul>
    * <li>{@code +HH} - hour only, ignoring minute and second
    * <li>{@code +HHmm} - hour, with minute if non-zero, ignoring second, no colon
    * <li>{@code +HH:mm} - hour, with minute if non-zero, ignoring second, with colon
    * <li>{@code +HHMM} - hour and minute, ignoring second, no colon
    * <li>{@code +HH:MM} - hour and minute, ignoring second, with colon
    * <li>{@code +HHMMss} - hour and minute, with second if non-zero, no colon
    * <li>{@code +HH:MM:ss} - hour and minute, with second if non-zero, with colon
    * <li>{@code +HHMMSS} - hour, minute and second, no colon
    * <li>{@code +HH:MM:SS} - hour, minute and second, with colon
    * </ul><p>
    * The "no offset" text controls what text is printed when the total amount of
    * the offset fields to be output is zero.
    * Example values would be 'Z', '+00:00', 'UTC' or 'GMT'.
    * Three formats are accepted for parsing UTC - the "no offset" text, and the
    * plus and minus versions of zero defined by the pattern.
    *
    * @param pattern  the pattern to use, not null
    * @param noOffsetText  the text to use when the offset is zero, not null
    * @return this, for chaining, not null
    */
  def appendOffset(pattern: String, noOffsetText: String): DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.OffsetIdPrinterParser(noOffsetText, pattern))
    this
  }

  /** Appends the localized zone offset, such as 'GMT+01:00', to the formatter.
    *
    * This appends a localized zone offset to the builder, the format of the
    * localized offset is controlled by the specified {@link FormatStyle style}
    * to this method:
    * <ul>
    * <li>{@link TextStyle#FULL full} - formats with localized offset text, such
    * as 'GMT, 2-digit hour and minute field, optional second field if non-zero,
    * and colon.
    * <li>{@link TextStyle#SHORT short} - formats with localized offset text,
    * such as 'GMT, hour without leading zero, optional 2-digit minute and
    * second if non-zero, and colon.
    * </ul>
    *
    * During formatting, the offset is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#offset()}.
    * If the offset cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * During parsing, the offset is parsed using the format defined above.
    * If the offset cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * @param style  the format style to use, not null
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if style is neither { @link TextStyle#FULL
     * full} nor { @link TextStyle#SHORT short}
    */
  def appendLocalizedOffset(style: TextStyle): DateTimeFormatterBuilder = {
    Objects.requireNonNull(style, "style")
    if ((style ne TextStyle.FULL) && (style ne TextStyle.SHORT))
      throw new IllegalArgumentException("Style must be either full or short")
    appendInternal(new DateTimeFormatterBuilder.LocalizedOffsetPrinterParser(style))
    this
  }

  /** Appends the time-zone ID, such as 'Europe/Paris' or '+02:00', to the formatter.
    *
    * This appends an instruction to print/parse the zone ID to the builder.
    * The zone ID is obtained in a strict manner suitable for {@code ZonedDateTime}.
    * By contrast, {@code OffsetDateTime} does not have a zone ID suitable
    * for use with this method, see {@link #appendZoneOrOffsetId()}.
    *
    * During printing, the zone is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#zoneId()}.
    * It will be printed using the result of {@link ZoneId#getId()}.
    * If the zone cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * During parsing, the zone is parsed and must match a known zone or offset.
    * If the zone cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * @return this, for chaining, not null
    * @see #appendZoneRegionId()
    */
  def appendZoneId: DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zoneId, "ZoneId()"))
    this
  }

  /** Appends the time-zone region ID, such as 'Europe/Paris', to the formatter,
    * rejecting the zone ID if it is a {@code ZoneOffset}.
    *
    * This appends an instruction to print/parse the zone ID to the builder
    * only if it is a region-based ID.
    *
    * During printing, the zone is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#zoneId()}.
    * If the zone is a {@code ZoneOffset} or it cannot be obtained then
    * an exception is thrown unless the section of the formatter is optional.
    * If the zone is not an offset, then the zone will be printed using
    * the zone ID from {@link ZoneId#getId()}.
    *
    * During parsing, the zone is parsed and must match a known zone or offset.
    * If the zone cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    * Note that parsing accepts offsets, whereas printing will never produce
    * one, thus parsing is equivalent to {@code appendZoneId}.
    *
    * @return this, for chaining, not null
    * @see #appendZoneId()
    */
  def appendZoneRegionId: DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.ZoneIdPrinterParser(DateTimeFormatterBuilder.QUERY_REGION_ONLY, "ZoneRegionId()"))
    this
  }

  /** Appends the time-zone ID, such as 'Europe/Paris' or '+02:00', to
    * the formatter, using the best available zone ID.
    *
    * This appends an instruction to print/parse the best available
    * zone or offset ID to the builder.
    * The zone ID is obtained in a lenient manner that first attempts to
    * find a true zone ID, such as that on {@code ZonedDateTime}, and
    * then attempts to find an offset, such as that on {@code OffsetDateTime}.
    *
    * During printing, the zone is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#zone()}.
    * It will be printed using the result of {@link ZoneId#getId()}.
    * If the zone cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * During parsing, the zone is parsed and must match a known zone or offset.
    * If the zone cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * This method is is identical to {@code appendZoneId()} except in the
    * mechanism used to obtain the zone.
    *
    * @return this, for chaining, not null
    * @see #appendZoneId()
    */
  def appendZoneOrOffsetId: DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.ZoneIdPrinterParser(TemporalQueries.zone, "ZoneOrOffsetId()"))
    this
  }

  /** Appends the time-zone name, such as 'British Summer Time', to the formatter.
    *
    * This appends an instruction to print the textual name of the zone to the builder.
    *
    * During printing, the zone is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#zoneId()}.
    * If the zone is a {@code ZoneOffset} it will be printed using the
    * result of {@link ZoneOffset#getId()}.
    * If the zone is not an offset, the textual name will be looked up
    * for the locale set in the {@link DateTimeFormatter}.
    * If the temporal object being printed represents an instant, then the text
    * will be the summer or winter time text as appropriate.
    * If the lookup for text does not find any suitable reuslt, then the
    * {@link ZoneId#getId() ID} will be printed instead.
    * If the zone cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * Parsing is not currently supported.
    *
    * @param textStyle  the text style to use, not null
    * @return this, for chaining, not null
    */
  def appendZoneText(textStyle: TextStyle): DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.ZoneTextPrinterParser(textStyle))
    this
  }

  /** Appends the time-zone name, such as 'British Summer Time', to the formatter.
    *
    * This appends an instruction to format/parse the textual name of the zone to
    * the builder.
    *
    * During formatting, the zone is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#zoneId()}.
    * If the zone is a {@code ZoneOffset} it will be printed using the
    * result of {@link ZoneOffset#getId()}.
    * If the zone is not an offset, the textual name will be looked up
    * for the locale set in the {@link DateTimeFormatter}.
    * If the temporal object being printed represents an instant, then the text
    * will be the summer or winter time text as appropriate.
    * If the lookup for text does not find any suitable result, then the
    * {@link ZoneId#getId() ID} will be printed instead.
    * If the zone cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * During parsing, either the textual zone name, the zone ID or the offset
    * is accepted. Many textual zone names are not unique, such as CST can be
    * for both "Central Standard Time" and "China Standard Time". In this
    * situation, the zone id will be determined by the region information from
    * formatter's  {@link DateTimeFormatter#getLocale() locale} and the standard
    * zone id for that area, for example, America/New_York for the America Eastern
    * zone. This method also allows a set of preferred {@link ZoneId} to be
    * specified for parsing. The matched preferred zone id will be used if the
    * textual zone name being parsed is not unique.
    *
    * If the zone cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * @param textStyle  the text style to use, not null
    * @param preferredZones  the set of preferred zone ids, not null
    * @return this, for chaining, not null
    */
  def appendZoneText(textStyle: TextStyle, preferredZones: java.util.Set[ZoneId]): DateTimeFormatterBuilder = {
    Objects.requireNonNull(preferredZones, "preferredZones")
    appendInternal(new DateTimeFormatterBuilder.ZoneTextPrinterParser(textStyle))
    this
  }

  /** Appends the chronology ID to the formatter.
    *
    * The chronology ID will be output during a print.
    * If the chronology cannot be obtained then an exception will be thrown.
    *
    * @return this, for chaining, not null
    */
  def appendChronologyId: DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.ChronoPrinterParser(null))
    this
  }

  /** Appends the chronology ID, such as 'ISO' or 'ThaiBuddhist', to the formatter.
    *
    * This appends an instruction to format/parse the chronology ID to the builder.
    *
    * During printing, the chronology is obtained using a mechanism equivalent
    * to querying the temporal with {@link TemporalQueries#chronology()}.
    * It will be printed using the result of {@link Chronology#getId()}.
    * If the chronology cannot be obtained then an exception is thrown unless the
    * section of the formatter is optional.
    *
    * During parsing, the chronology is parsed and must match one of the chronologies
    * in {@link Chronology#getAvailableChronologies()}.
    * If the chronology cannot be parsed then an exception is thrown unless the
    * section of the formatter is optional.
    * The parser uses the {@linkplain #parseCaseInsensitive() case sensitive} setting.
    *
    * @return this, for chaining, not null
    */
  def appendChronologyText(textStyle: TextStyle): DateTimeFormatterBuilder = {
    Objects.requireNonNull(textStyle, "textStyle")
    appendInternal(new DateTimeFormatterBuilder.ChronoPrinterParser(textStyle))
    this
  }

  /** Appends a localized date-time pattern to the formatter.
    *
    * This appends a localized section to the builder, suitable for outputting
    * a date, time or date-time combination. The format of the localized
    * section is lazily looked up based on four items:
    *<ul>
    * <li>the {@code dateStyle} specified to this method
    * <li>the {@code timeStyle} specified to this method
    * <li>the {@code Locale} of the {@code DateTimeFormatter}
    * <li>the {@code Chronology}, selecting the best available
    * </ul><p>
    * During formatting, the chronology is obtained from the temporal object
    * being formatted, which may have been overridden by
    * {@link DateTimeFormatter#withChronology(Chronology)}.
    *
    * During parsing, if a chronology has already been parsed, then it is used.
    * Otherwise the default from {@code DateTimeFormatter.withChronology(Chronology)}
    * is used, with {@code IsoChronology} as the fallback.
    *
    * Note that this method provides similar functionality to methods on
    * {@code DateFormat} such as {@link DateFormat#getDateTimeInstance(int, int)}.
    *
    * @param dateStyle  the date style to use, null means no date required
    * @param timeStyle  the time style to use, null means no time required
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if both the date and time styles are null
    */
  def appendLocalized(dateStyle: FormatStyle, timeStyle: FormatStyle): DateTimeFormatterBuilder = {
    if (dateStyle == null && timeStyle == null)
      throw new IllegalArgumentException("Either the date or time style must be non-null")
    appendInternal(new DateTimeFormatterBuilder.LocalizedPrinterParser(dateStyle, timeStyle))
    this
  }

  /** Appends a character literal to the formatter.
    *
    * This character will be output during a print.
    *
    * @param literal  the literal to append, not null
    * @return this, for chaining, not null
    */
  def appendLiteral(literal: Char): DateTimeFormatterBuilder = {
    appendInternal(new DateTimeFormatterBuilder.CharLiteralPrinterParser(literal))
    this
  }

  /** Appends a string literal to the formatter.
    *
    * This string will be output during a print.
    *
    * If the literal is empty, nothing is added to the formatter.
    *
    * @param literal  the literal to append, not null
    * @return this, for chaining, not null
    */
  def appendLiteral(literal: String): DateTimeFormatterBuilder = {
    Objects.requireNonNull(literal, "literal")
    if (literal.length > 0)
      if (literal.length == 1)
        appendInternal(new DateTimeFormatterBuilder.CharLiteralPrinterParser(literal.charAt(0)))
      else
        appendInternal(new DateTimeFormatterBuilder.StringLiteralPrinterParser(literal))
    this
  }

  /** Appends all the elements of a formatter to the builder.
    *
    * This method has the same effect as appending each of the constituent
    * parts of the formatter directly to this builder.
    *
    * @param formatter  the formatter to add, not null
    * @return this, for chaining, not null
    */
  def append(formatter: DateTimeFormatter): DateTimeFormatterBuilder = {
    Objects.requireNonNull(formatter, "formatter")
    appendInternal(formatter.toPrinterParser(false))
    this
  }

  /** Appends a formatter to the builder which will optionally print/parse.
    *
    * This method has the same effect as appending each of the constituent
    * parts directly to this builder surrounded by an {@link #optionalStart()} and
    * {@link #optionalEnd()}.
    *
    * The formatter will print if data is available for all the fields contained within it.
    * The formatter will parse if the string matches, otherwise no error is returned.
    *
    * @param formatter  the formatter to add, not null
    * @return this, for chaining, not null
    */
  def appendOptional(formatter: DateTimeFormatter): DateTimeFormatterBuilder = {
    Objects.requireNonNull(formatter, "formatter")
    appendInternal(formatter.toPrinterParser(true))
    this
  }

  /** Appends the elements defined by the specified pattern to the builder.
    *
    * All letters 'A' to 'Z' and 'a' to 'z' are reserved as pattern letters.
    * The characters '{' and '}' are reserved for future use.
    * The characters '[' and ']' indicate optional patterns.
    * The following pattern letters are defined:
    * <pre>
    * Symbol  Meaning                     Presentation      Examples
    * ------  -------                     ------------      -------
    * G       era                         number/text       1; 01; AD; Anno Domini
    * y       year                        year              2004; 04
    * D       day-of-year                 number            189
    * M       month-of-year               number/text       7; 07; Jul; July; J
    * d       day-of-month                number            10
    *
    * Q       quarter-of-year             number/text       3; 03; Q3
    * Y       week-based-year             year              1996; 96
    * w       week-of-year                number            27
    * W       week-of-month               number            27
    * e       localized day-of-week       number            2; Tue; Tuesday; T
    * E       day-of-week                 number/text       2; Tue; Tuesday; T
    * F       week-of-month               number            3
    *
    * a       am-pm-of-day                text              PM
    * h       clock-hour-of-am-pm (1-12)  number            12
    * K       hour-of-am-pm (0-11)        number            0
    * k       clock-hour-of-am-pm (1-24)  number            0
    *
    * H       hour-of-day (0-23)          number            0
    * m       minute-of-hour              number            30
    * s       second-of-minute            number            55
    * S       fraction-of-second          fraction          978
    * A       milli-of-day                number            1234
    * n       nano-of-second              number            987654321
    * N       nano-of-day                 number            1234000000
    *
    * V       time-zone ID                zone-id           America/Los_Angeles; Z; -08:30
    * z       time-zone name              zone-name         Pacific Standard Time; PST
    * X       zone-offset 'Z' for zero    offset-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
    * x       zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
    * Z       zone-offset                 offset-Z          +0000; -0800; -08:00;
    *
    * p       pad next                    pad modifier      1
    *
    * '       escape for text             delimiter
    * ''      single quote                literal           '
    * [       optional section start
    * ]       optional section end
    * {}      reserved for future use
    * </pre>
    *
    * The count of pattern letters determine the format.
    *
    * <b>Text</b>: The text style is determined based on the number of pattern letters used.
    * Less than 4 pattern letters will use the {@link TextStyle#SHORT short form}.
    * Exactly 4 pattern letters will use the {@link TextStyle#FULL full form}.
    * Exactly 5 pattern letters will use the {@link TextStyle#NARROW narrow form}.
    *
    * <b>Number</b>: If the count of letters is one, then the value is printed using the minimum number
    * of digits and without padding as per {@link #appendValue(TemporalField)}. Otherwise, the
    * count of digits is used as the width of the output field as per {@link #appendValue(TemporalField, int)}.
    *
    * <b>Number/Text</b>: If the count of pattern letters is 3 or greater, use the Text rules above.
    * Otherwise use the Number rules above.
    *
    * <b>Fraction</b>: Outputs the nano-of-second field as a fraction-of-second.
    * The nano-of-second value has nine digits, thus the count of pattern letters is from 1 to 9.
    * If it is less than 9, then the nano-of-second value is truncated, with only the most
    * significant digits being output.
    * When parsing in strict mode, the number of parsed digits must match the count of pattern letters.
    * When parsing in lenient mode, the number of parsed digits must be at least the count of pattern
    * letters, up to 9 digits.
    *
    * <b>Year</b>: The count of letters determines the minimum field width below which padding is used.
    * If the count of letters is two, then a {@link #appendValueReduced reduced} two digit form is used.
    * For printing, this outputs the rightmost two digits. For parsing, this will parse using the
    * base value of 2000, resulting in a year within the range 2000 to 2099 inclusive.
    * If the count of letters is less than four (but not two), then the sign is only output for negative
    * years as per {@link SignStyle#NORMAL}.
    * Otherwise, the sign is output if the pad width is exceeded, as per {@link SignStyle#EXCEEDS_PAD}
    *
    * <b>ZoneId</b>: This outputs the time-zone ID, such as 'Europe/Paris'.
    * If the count of letters is two, then the time-zone ID is output.
    * Any other count of letters throws {@code IllegalArgumentException}.
    * <pre>
    * Pattern     Equivalent builder methods
    * VV          appendZoneId()
    * </pre>
    *
    * <b>Zone names</b>: This outputs the display name of the time-zone ID.
    * If the count of letters is one, two or three, then the short name is output.
    * If the count of letters is four, then the full name is output.
    * Five or more letters throws {@code IllegalArgumentException}.
    * <pre>
    * Pattern     Equivalent builder methods
    * z           appendZoneText(TextStyle.SHORT)
    * zz          appendZoneText(TextStyle.SHORT)
    * zzz         appendZoneText(TextStyle.SHORT)
    * zzzz        appendZoneText(TextStyle.FULL)
    * </pre>
    *
    * <b>Offset X and x</b>: This formats the offset based on the number of pattern letters.
    * One letter outputs just the hour', such as '+01', unless the minute is non-zero
    * in which case the minute is also output, such as '+0130'.
    * Two letters outputs the hour and minute, without a colon, such as '+0130'.
    * Three letters outputs the hour and minute, with a colon, such as '+01:30'.
    * Four letters outputs the hour and minute and optional second, without a colon, such as '+013015'.
    * Five letters outputs the hour and minute and optional second, with a colon, such as '+01:30:15'.
    * Six or more letters throws {@code IllegalArgumentException}.
    * Pattern letter 'X' (upper case) will output 'Z' when the offset to be output would be zero,
    * whereas pattern letter 'x' (lower case) will output '+00', '+0000', or '+00:00'.
    * <pre>
    * Pattern     Equivalent builder methods
    * X           appendOffset("+HHmm","Z")
    * XX          appendOffset("+HHMM","Z")
    * XXX         appendOffset("+HH:MM","Z")
    * XXXX        appendOffset("+HHMMss","Z")
    * XXXXX       appendOffset("+HH:MM:ss","Z")
    * x           appendOffset("+HHmm","+00")
    * xx          appendOffset("+HHMM","+0000")
    * xxx         appendOffset("+HH:MM","+00:00")
    * xxxx        appendOffset("+HHMMss","+0000")
    * xxxxx       appendOffset("+HH:MM:ss","+00:00")
    * </pre>
    *
    * <b>Offset Z</b>: This formats the offset based on the number of pattern letters.
    * One, two or three letters outputs the hour and minute, without a colon, such as '+0130'.
    * Four or more letters throws {@code IllegalArgumentException}.
    * The output will be '+0000' when the offset is zero.
    * <pre>
    * Pattern     Equivalent builder methods
    * Z           appendOffset("+HHMM","+0000")
    * ZZ          appendOffset("+HHMM","+0000")
    * ZZZ         appendOffset("+HHMM","+0000")
    * </pre>
    *
    * <b>Optional section</b>: The optional section markers work exactly like calling {@link #optionalStart()}
    * and {@link #optionalEnd()}.
    *
    * <b>Pad modifier</b>: Modifies the pattern that immediately follows to be padded with spaces.
    * The pad width is determined by the number of pattern letters.
    * This is the same as calling {@link #padNext(int)}.
    *
    * For example, 'ppH' outputs the hour-of-day padded on the left with spaces to a width of 2.
    *
    * Any unrecognized letter is an error.
    * Any non-letter character, other than '[', ']', '{', '}' and the single quote will be output directly.
    * Despite this, it is recommended to use single quotes around all characters that you want to
    * output directly to ensure that future changes do not break your application.
    *
    * Note that the pattern string is similar, but not identical, to
    * {@link java.text.SimpleDateFormat SimpleDateFormat}.
    * The pattern string is also similar, but not identical, to that defined by the
    * Unicode Common Locale Data Repository (CLDR/LDML).
    * Pattern letters 'E' and 'u' are merged, which changes the meaning of "E" and "EE" to be numeric.
    * Pattern letters 'X' is aligned with Unicode CLDR/LDML, which affects pattern 'X'.
    * Pattern letter 'y' and 'Y' parse years of two digits and more than 4 digits differently.
    * Pattern letters 'n', 'A', 'N', 'I' and 'p' are added.
    * Number types will reject large numbers.
    *
    * @param pattern  the pattern to add, not null
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if the pattern is invalid
    */
  def appendPattern(pattern: String): DateTimeFormatterBuilder = {
    Objects.requireNonNull(pattern, "pattern")
    parsePattern(pattern)
    this
  }

  private def parsePattern(pattern: String): Unit = {
    var pos: Int = 0
    while (pos < pattern.length) {
      var cur: Char = pattern.charAt(pos)
      if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
        var start: Int = pos
        pos += 1
        while (pos < pattern.length && pattern.charAt(pos) == cur) {
          pos += 1
        }
        var count: Int = pos - start
        if (cur == 'p') {
          var pad: Int = 0
          if (pos < pattern.length) {
            cur = pattern.charAt(pos)
            if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
              pad = count
              start = pos
              pos += 1
              while (pos < pattern.length && pattern.charAt(pos) == cur) {
                pos += 1
              }
              count = pos - start
            }
          }
          if (pad == 0)
            throw new IllegalArgumentException(s"Pad letter 'p' must be followed by valid pad pattern: $pattern")
          padNext(pad)
        }
        val field: TemporalField = DateTimeFormatterBuilder.FIELD_MAP.get(cur)
        if (field != null)
          parseField(cur, count, field)
        else if (cur == 'z') {
          if (count > 4)
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
          else if (count == 4)
            appendZoneText(TextStyle.FULL)
          else
            appendZoneText(TextStyle.SHORT)
        }
        else if (cur == 'V') {
          if (count != 2)
            throw new IllegalArgumentException(s"Pattern letter count must be 2: $cur")
          appendZoneId
        }
        else if (cur == 'Z') {
          if (count < 4)
            appendOffset("+HHMM", "+0000")
          else if (count == 4)
            appendLocalizedOffset(TextStyle.FULL)
          else if (count == 5)
            appendOffset("+HH:MM:ss", "Z")
          else
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
        }
        else if (cur == 'O') {
          if (count == 1)
            appendLocalizedOffset(TextStyle.SHORT)
          else if (count == 4)
            appendLocalizedOffset(TextStyle.FULL)
          else
            throw new IllegalArgumentException(s"Pattern letter count must be 1 or 4: $cur")
        }
        else if (cur == 'X') {
          if (count > 5)
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
          appendOffset(DateTimeFormatterBuilder.OffsetIdPrinterParser.PATTERNS(count + (if (count == 1) 0 else 1)), "Z")
        }
        else if (cur == 'x') {
          if (count > 5)
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
          val zero: String = if (count == 1) "+00" else if (count % 2 == 0) "+0000" else "+00:00"
          appendOffset(DateTimeFormatterBuilder.OffsetIdPrinterParser.PATTERNS(count + (if (count == 1) 0 else 1)), zero)
        }
        else if (cur == 'W') {
          if (count > 1)
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
          appendInternal(new DateTimeFormatterBuilder.WeekFieldsPrinterParser('W', count))
        }
        else if (cur == 'w') {
          if (count > 2)
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
          appendInternal(new DateTimeFormatterBuilder.WeekFieldsPrinterParser('w', count))
        }
        else if (cur == 'Y') {
          appendInternal(new DateTimeFormatterBuilder.WeekFieldsPrinterParser('Y', count))
        }
        else {
          throw new IllegalArgumentException(s"Unknown pattern letter: $cur")
        }
        pos -= 1
      }
      else if (cur == '\'') {
        val start: Int = pos
        pos += 1
        scala.util.control.Breaks.breakable {
          while (pos < pattern.length) {
            if (pattern.charAt(pos) == '\'') {
              if (pos + 1 < pattern.length && pattern.charAt(pos + 1) == '\'')
                pos += 1
              else
                scala.util.control.Breaks.break()
            }
            pos += 1
          }
        }
        if (pos >= pattern.length)
          throw new IllegalArgumentException(s"Pattern ends with an incomplete string literal: $pattern")
        val str: String = pattern.substring(start + 1, pos)
        if (str.length == 0)
          appendLiteral('\'')
        else
          appendLiteral(str.replace("''", "'"))
      }
      else if (cur == '[')
        optionalStart()
      else if (cur == ']') {
        if (active.parent == null)
          throw new IllegalArgumentException("Pattern invalid as it contains ] without previous [")
        optionalEnd()
      }
      else if (cur == '{' || cur == '}' || cur == '#')
        throw new IllegalArgumentException(s"Pattern includes reserved character: '$cur'")
      else
        appendLiteral(cur)
      pos += 1
    }
  }

  private def parseField(cur: Char, count: Int, field: TemporalField): Unit = {
    cur match {
      case 'u' | 'y' =>
        if (count == 2)
          appendValueReduced(field, 2, 2, DateTimeFormatterBuilder.ReducedPrinterParser.BASE_DATE)
        else if (count < 4)
          appendValue(field, count, 19, SignStyle.NORMAL)
        else
          appendValue(field, count, 19, SignStyle.EXCEEDS_PAD)
      case 'M' | 'Q' =>
        count match {
          case 1 =>
            appendValue(field)
          case 2 =>
            appendValue(field, 2)
          case 3 =>
            appendText(field, TextStyle.SHORT)
          case 4 =>
            appendText(field, TextStyle.FULL)
          case 5 =>
            appendText(field, TextStyle.NARROW)
          case _ =>
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
        }
      case 'L' | 'q' =>
        count match {
          case 1 =>
            appendValue(field)
          case 2 =>
            appendValue(field, 2)
          case 3 =>
            appendText(field, TextStyle.SHORT_STANDALONE)
          case 4 =>
            appendText(field, TextStyle.FULL_STANDALONE)
          case 5 =>
            appendText(field, TextStyle.NARROW_STANDALONE)
          case _ =>
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
        }
      case 'e' =>
        count match {
          case 1 | 2 =>
            appendInternal(new DateTimeFormatterBuilder.WeekFieldsPrinterParser('e', count))
          case 3 =>
            appendText(field, TextStyle.SHORT)
          case 4 =>
            appendText(field, TextStyle.FULL)
          case 5 =>
            appendText(field, TextStyle.NARROW)
          case _ =>
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
        }
      case 'c' =>
        count match {
          case 1 =>
            appendInternal(new DateTimeFormatterBuilder.WeekFieldsPrinterParser('c', count))
          case 2 =>
            throw new IllegalArgumentException(s"Invalid number of pattern letters: $cur")
          case 3 =>
            appendText(field, TextStyle.SHORT_STANDALONE)
          case 4 =>
            appendText(field, TextStyle.FULL_STANDALONE)
          case 5 =>
            appendText(field, TextStyle.NARROW_STANDALONE)
          case _ =>
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
        }
      case 'a' =>
        if (count == 1)
          appendText(field, TextStyle.SHORT)
        else
          throw new IllegalArgumentException(s"Too many pattern letters: $cur")
      case 'E' | 'G' =>
        count match {
          case 1 | 2 | 3 =>
            appendText(field, TextStyle.SHORT)
          case 4 =>
            appendText(field, TextStyle.FULL)
          case 5 =>
            appendText(field, TextStyle.NARROW)
          case _ =>
            throw new IllegalArgumentException(s"Too many pattern letters: $cur")
        }
      case 'S' =>
        appendFraction(ChronoField.NANO_OF_SECOND, count, count, false)
      case 'F' =>
        if (count == 1)
          appendValue(field)
        else
          throw new IllegalArgumentException(s"Too many pattern letters: $cur")
      case 'd' | 'h' | 'H' | 'k' | 'K' | 'm' | 's' =>
        if (count == 1)
          appendValue(field)
        else if (count == 2)
          appendValue(field, count)
        else
          throw new IllegalArgumentException(s"Too many pattern letters: $cur")
      case 'D' =>
        if (count == 1)
          appendValue(field)
        else if (count <= 3)
          appendValue(field, count)
        else
          throw new IllegalArgumentException(s"Too many pattern letters: $cur")
      case _ =>
        if (count == 1)
          appendValue(field)
        else
          appendValue(field, count)
    }
  }

  /** Causes the next added printer/parser to pad to a fixed width using a space.
    *
    * This padding will pad to a fixed width using spaces.
    *
    * During formatting, the decorated element will be output and then padded
    * to the specified width. An exception will be thrown during printing if
    * the pad width is exceeded.
    *
    * During parsing, the padding and decorated element are parsed.
    * If parsing is lenient, then the pad width is treated as a maximum.
    * If parsing is case insensitive, then the pad character is matched ignoring case.
    * The padding is parsed greedily. Thus, if the decorated element starts with
    * the pad character, it will not be parsed.
    *
    * @param padWidth  the pad width, 1 or greater
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if pad width is too small
    */
  def padNext(padWidth: Int): DateTimeFormatterBuilder = padNext(padWidth, ' ')

  /** Causes the next added printer/parser to pad to a fixed width.
    *
    * This padding is intended for padding other than zero-padding.
    * Zero-padding should be achieved using the appendValue methods.
    *
    * During formatting, the decorated element will be output and then padded
    * to the specified width. An exception will be thrown during printing if
    * the pad width is exceeded.
    *
    * During parsing, the padding and decorated element are parsed.
    * If parsing is lenient, then the pad width is treated as a maximum.
    * If parsing is case insensitive, then the pad character is matched ignoring case.
    * The padding is parsed greedily. Thus, if the decorated element starts with
    * the pad character, it will not be parsed.
    *
    * @param padWidth  the pad width, 1 or greater
    * @param padChar  the pad character
    * @return this, for chaining, not null
    * @throws IllegalArgumentException if pad width is too small
    */
  def padNext(padWidth: Int, padChar: Char): DateTimeFormatterBuilder = {
    if (padWidth < 1)
      throw new IllegalArgumentException(s"The pad width must be at least one but was $padWidth")
    active.padNextWidth = padWidth
    active.padNextChar = padChar
    active.valueParserIndex = -1
    this
  }

  /** Mark the start of an optional section.
    *
    * The output of printing can include optional sections, which may be nested.
    * An optional section is started by calling this method and ended by calling
    * {@link #optionalEnd()} or by ending the build process.
    *
    * All elements in the optional section are treated as optional.
    * During printing, the section is only output if data is available in the
    * {@code TemporalAccessor} for all the elements in the section.
    * During parsing, the whole section may be missing from the parsed string.
    *
    * For example, consider a builder setup as
    * {@code builder.appendValue(HOUR_OF_DAY,2).optionalStart().appendValue(MINUTE_OF_HOUR,2)}.
    * The optional section ends automatically at the end of the builder.
    * During printing, the minute will only be output if its value can be obtained from the date-time.
    * During parsing, the input will be successfully parsed whether the minute is present or not.
    *
    * @return this, for chaining, not null
    */
  def optionalStart(): DateTimeFormatterBuilder = {
    active.valueParserIndex = -1
    active = new DateTimeFormatterBuilder(active, true)
    this
  }

  /** Ends an optional section.
    *
    * The output of printing can include optional sections, which may be nested.
    * An optional section is started by calling {@link #optionalStart()} and ended
    * using this method (or at the end of the builder).
    *
    * Calling this method without having previously called {@code optionalStart}
    * will throw an exception.
    * Calling this method immediately after calling {@code optionalStart} has no effect
    * on the formatter other than ending the (empty) optional section.
    *
    * All elements in the optional section are treated as optional.
    * During printing, the section is only output if data is available in the
    * {@code TemporalAccessor} for all the elements in the section.
    * During parsing, the whole section may be missing from the parsed string.
    *
    * For example, consider a builder setup as
    * {@code builder.appendValue(HOUR_OF_DAY,2).optionalStart().appendValue(MINUTE_OF_HOUR,2).optionalEnd()}.
    * During printing, the minute will only be output if its value can be obtained from the date-time.
    * During parsing, the input will be successfully parsed whether the minute is present or not.
    *
    * @return this, for chaining, not null
    * @throws IllegalStateException if there was no previous call to { @code optionalStart}
    */
  def optionalEnd(): DateTimeFormatterBuilder = {
    if (active.parent == null)
      throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()")
    if (active.printerParsers.size > 0) {
      val cpp: DateTimeFormatterBuilder.CompositePrinterParser = new DateTimeFormatterBuilder.CompositePrinterParser(active.printerParsers, active.optional)
      active = active.parent
      appendInternal(cpp)
    }
    else
      active = active.parent
    this
  }

  /** Appends a printer and/or parser to the internal list handling padding.
    *
    * @param pp  the printer-parser to add, not null
    * @return the index into the active parsers list
    */
  private def appendInternal(pp: DateTimeFormatterBuilder.DateTimePrinterParser): Int = {
    var _pp = pp
    Objects.requireNonNull(_pp, "pp")
    if (active.padNextWidth > 0) {
      if (_pp != null)
        _pp = new DateTimeFormatterBuilder.PadPrinterParserDecorator(_pp, active.padNextWidth, active.padNextChar)
      active.padNextWidth = 0
      active.padNextChar = 0
    }
    active.printerParsers.add(_pp)
    active.valueParserIndex = -1
    active.printerParsers.size - 1
  }

  /** Completes this builder by creating the DateTimeFormatter using the default locale.
    *
    * This will create a formatter with the default locale.
    * Numbers will be printed and parsed using the standard non-localized set of symbols.
    *
    * Calling this method will end any open optional sections by repeatedly
    * calling {@link #optionalEnd()} before creating the formatter.
    *
    * This builder can still be used after creating the formatter if desired,
    * although the state may have been changed by calls to {@code optionalEnd}.
    *
    * @return the created formatter, not null
    */
  def toFormatter: DateTimeFormatter = toFormatter(Locale.getDefault)

  /** Completes this builder by creating the DateTimeFormatter using the specified locale.
    *
    * This will create a formatter with the specified locale.
    * Numbers will be printed and parsed using the standard non-localized set of symbols.
    *
    * Calling this method will end any open optional sections by repeatedly
    * calling {@link #optionalEnd()} before creating the formatter.
    *
    * This builder can still be used after creating the formatter if desired,
    * although the state may have been changed by calls to {@code optionalEnd}.
    *
    * @param locale  the locale to use for formatting, not null
    * @return the created formatter, not null
    */
  def toFormatter(locale: Locale): DateTimeFormatter = {
    Objects.requireNonNull(locale, "locale")
    while (active.parent != null) optionalEnd()
    val pp: DateTimeFormatterBuilder.CompositePrinterParser = new DateTimeFormatterBuilder.CompositePrinterParser(printerParsers, false)
    new DateTimeFormatter(pp, locale, DecimalStyle.STANDARD, ResolverStyle.SMART, null, null, null)
  }

  private[format] def toFormatter(style: ResolverStyle): DateTimeFormatter = toFormatter.withResolverStyle(style)
}
