package org.threeten.bp.format

import java.util.Locale

private[format] object CasePlatformHelper {
  def toLocaleIndependentLowerCase(string: String) = string.toLowerCase(Locale.ENGLISH)
}
