package java.util

import org.threeten.bp.ZonedDateTime

@SerialVersionUID(-8125100834729963327L)
class GregorianCalendar(timezone: TimeZone = TimeZone.getDefault, locale: Locale = Locale.getDefault) extends Calendar(timezone, locale) {

  def this(year: Int, month: Int, day: Int) {
    this()
    set(year, month, day)
  }

  def this(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
    this()
    set(year, month, day, hour, minute)
  }

  def this(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) {
    this()
    set(year, month, day, hour, minute, second)
  }

  /*
  private[util] def this(milliseconds: Long) {
    this(false)
    setTimeInMillis(milliseconds)
  }
  */

  def this(locale: Locale) {
    this(TimeZone.getDefault, locale)
  }

  /*
  def this(timezone: TimeZone, locale: Locale) {
    super(timezone, locale)
    setTimeInMillis(System.currentTimeMillis())
  }
  */


  private[util] def this(ignored: Boolean) {
    this(TimeZone.getDefault)
    setFirstDayOfWeek(Calendar.SUNDAY)
    setMinimalDaysInFirstWeek(1)
  }

  protected[util] def computeTime(): Unit = ???
  protected[util] def computeFields(): Unit = ???
  // Access  from DateTimeUtils
  /*protected[util]*/ def getTimeZone: TimeZone = ???
  def toZonedDateTime: ZonedDateTime = ???
  def setGregorianChange(date: Date): Unit = ???
  def add(x$1: Int,x$2: Int): Unit = ???
  def getGreatestMinimum(x$1: Int): Int = ???
  def getLeastMaximum(x$1: Int): Int = ???
  def getMaximum(x$1: Int): Int = ???
  def getMinimum(x$1: Int): Int = ???
  def roll(x$1: Int,x$2: Boolean): Unit = ???
}

object GregorianCalendar {
  final val BC = 0
  final val AD = 1
}
