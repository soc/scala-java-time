package java.util

import org.threeten.bp.Instant

@SerialVersionUID(-1807547505821590642L)
abstract class Calendar private[util](timezone: TimeZone = null, locale: Locale = null) extends Cloneable with Serializable with Ordered[Calendar] {

  if (timezone == null && locale == null) {
    init(TimeZone.getDefault, Locale.getDefault)
  } else if (locale == null) {
    setLenient(true)
    setTimeZone(timezone)
  } else {
    init(timezone, locale)
  }

  def init(timezone: TimeZone, locale: Locale): Unit = {
    setLenient(true)
    setTimeZone(timezone)
    // TODO: need to set firstDayOfWeekfirstDayOfWeek and minimalDaysInFirstWeek
  }


  protected var areFieldsSet: Boolean = false
  protected val fields: Array[Int] = new Array(Calendar.FIELD_COUNT)
  protected val isSet: Array[Boolean] = new Array(Calendar.FIELD_COUNT)
  protected var isTimeSet: Boolean = false
  protected var time: Long = -1
  private var lenient: Boolean = false
  private var firstDayOfWeek: Int = 0
  private var minimalDaysInFirstWeek: Int = 0
  private var zone: TimeZone = null

  protected def computeTime(): Unit
  protected def computeFields(): Unit

  def getMaximum(field: Int): Int
  def getMinimum(field: Int): Int
  def getLeastMaximum(field: Int): Int
  def getGreatestMinimum(field: Int): Int

  def compare(anotherCalendar: Calendar): Int = ???

  def getActualMaximum(field: Int): Int = ???
  def getActualMinimum(field: Int): Int = ???

  def getTimeInMillis(): Long = ???

  // Access  from DateTimeUtils
  /*protected[util]*/ def getTimeZone: TimeZone

  def getFirstDayOfWeek(): Int = firstDayOfWeek

  def getMinimalDaysInFirstWeek(): Int = minimalDaysInFirstWeek

  def set(field: Int, value: Int): Unit = ???

  final def set(year: Int, month: Int, day: Int): Unit = ???

  final def set(year: Int, month: Int, day: Int, hourOfDay: Int, minute: Int): Unit = ???

  final def set(year: Int, month: Int, day: Int, hourOfDay: Int, minute: Int, second: Int): Unit = ???

  def setLenient(value: Boolean): Unit = lenient = value;

  def setTimeZone(timezone: TimeZone): Unit = ???

  def setFirstDayOfWeek(value: Int): Unit = firstDayOfWeek = value

  def setMinimalDaysInFirstWeek(value: Int): Unit = minimalDaysInFirstWeek = value

  def setTimeInMillis(millis: Long): Unit = ???

  /** Converts a {@code Calendar} to an {@code Instant}.
    *
    * @return the instant, not null
    */
  def toInstant: Instant = Instant.ofEpochMilli(getTimeInMillis)
}

object Calendar {
  val JANUARY = 0;
  val FEBRUARY = 1;
  val MARCH = 2;
  val APRIL = 3;
  val MAY = 4;
  val JUNE = 5;
  val JULY = 6;
  val AUGUST = 7;
  val SEPTEMBER = 8;
  val OCTOBER = 9;
  val NOVEMBER = 10;
  val DECEMBER = 11;
  val UNDECIMBER = 12;

  val SUNDAY = 1;
  val MONDAY = 2;
  val TUESDAY = 3;
  val WEDNESDAY = 4;
  val THURSDAY = 5;
  val FRIDAY = 6;
  val SATURDAY = 7;

  val ERA = 0;
  val YEAR = 1;
  val MONTH = 2;
  val WEEK_OF_YEAR = 3;
  val WEEK_OF_MONTH = 4;
  val DATE = 5;
  val DAY_OF_MONTH = 5;
  val DAY_OF_YEAR = 6;
  val DAY_OF_WEEK = 7;
  val DAY_OF_WEEK_IN_MONTH = 8;
  val AM_PM = 9;
  val HOUR = 10;
  val HOUR_OF_DAY = 11;
  val MINUTE = 12;
  val SECOND = 13;
  val MILLISECOND = 14;
  val ZONE_OFFSET = 15;
  val DST_OFFSET = 16;
  val FIELD_COUNT = 17;

  val AM = 0;
  val PM = 1;

  val ALL_STYLES = 0;
  val SHORT = 1;
  val LONG = 2;

  def getInstance(): Calendar = synchronized { new GregorianCalendar() }

  def getInstance(locale: Locale): Calendar = synchronized { new GregorianCalendar(locale) }

  def getInstance(timezone: TimeZone): Calendar = synchronized { new GregorianCalendar(timezone) }

  def getInstance(timezone: TimeZone, locale: Locale): Calendar = synchronized { new GregorianCalendar(timezone, locale) }
}
