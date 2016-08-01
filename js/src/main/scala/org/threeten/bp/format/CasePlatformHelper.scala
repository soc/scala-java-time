package org.threeten.bp.format

private[format] object CasePlatformHelper {
  // Scala.js forwards the call to the native JavaScript API in which toLowerCase is locale-independent.
  def toLocaleIndependentLowerCase(string: String) = string.toLowerCase
}
