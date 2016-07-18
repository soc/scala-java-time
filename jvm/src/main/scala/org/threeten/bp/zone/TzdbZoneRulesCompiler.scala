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

import java.io.{BufferedReader, ByteArrayOutputStream, DataOutputStream, File, FileOutputStream, FileReader, IOException, OutputStream}
import java.text.ParsePosition
import java.util.{Arrays, StringTokenizer}
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import org.threeten.bp.{DayOfWeek, LocalDate, LocalDateTime, LocalTime, Month, Year, ZoneOffset}
import org.threeten.bp.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import org.threeten.bp.temporal.ChronoField.{HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE}
import org.threeten.bp.temporal.{TemporalAccessor, TemporalAdjusters}
import org.threeten.bp.zone.ZoneOffsetTransitionRule.TimeDefinition

object TzdbZoneRulesCompiler {
  /** Time parser. */
  private val TIME_PARSER: DateTimeFormatter =  new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY).optionalStart().appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).toFormatter

  /** Reads a set of TZDB files and builds a single combined data file.
    *
    * @param args  the arguments
    */
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      outputHelp()
      return
    }
    var version: String = null
    var baseSrcDir: File = null
    var dstDir: File = null
    var unpacked: Boolean = false
    var verbose: Boolean = false
    var i: Int = 0
    val outer = new scala.util.control.Breaks()
    outer.breakable {
      while (i < args.length) {
        val inner = new scala.util.control.Breaks()
        inner.breakable {
          val arg: String = args(i)
          if (!arg.startsWith("-")) {
            outer.break()
          }
          if ("-srcdir" == arg) {
            if (baseSrcDir == null && {i += 1; i} < args.length) {
              baseSrcDir = new File(args(i))
              inner.break()
            }
          }
          else if ("-dstdir" == arg) {
            if (dstDir == null && {i += 1; i} < args.length) {
              dstDir = new File(args(i))
              inner.break()
            }
          }
          else if ("-version" == arg) {
            if (version == null && {i += 1; i} < args.length) {
              version = args(i)
              inner.break()
            }
          }
          else if ("-unpacked" == arg) {
            if (!unpacked) {
              unpacked = true
              i += 1
              inner.break()
            }
          }
          else if ("-verbose" == arg) {
            if (!verbose) {
              verbose = true
              i += 1
              inner.break()
            }
          }
          else if ("-help" != arg) {
            i += 1
            System.out.println(s"Unrecognised option: $arg")
          }
          outputHelp()
          return
        }
      }
    }
    if (baseSrcDir == null) {
      System.out.println(s"Source directory must be specified using -srcdir: $baseSrcDir")
      return
    }
    if (!baseSrcDir.isDirectory) {
      System.out.println(s"Source does not exist or is not a directory: $baseSrcDir")
      return
    }
    dstDir = if (dstDir != null) dstDir else baseSrcDir
    var srcFileNames: java.util.List[String] = Arrays.asList(Arrays.copyOfRange(args, i, args.length): _*)
    if (srcFileNames.isEmpty) {
      System.out.println("Source filenames not specified, using default set")
      System.out.println("(africa antarctica asia australasia backward etcetera europe northamerica southamerica)")
      srcFileNames = Arrays.asList("africa", "antarctica", "asia", "australasia", "backward", "etcetera", "europe", "northamerica", "southamerica")
    }
    val srcDirs: java.util.List[File] = new java.util.ArrayList[File]
    if (version != null) {
      val srcDir: File = new File(baseSrcDir, version)
      if (!srcDir.isDirectory) {
        System.out.println(s"Version does not represent a valid source directory : $srcDir")
        return
      }
      srcDirs.add(srcDir)
    }
    else {
      val dirs: Array[File] = baseSrcDir.listFiles
      for (dir <- dirs) {
        if (dir.isDirectory && dir.getName.matches("[12][0-9][0-9][0-9][A-Za-z0-9._-]+"))
          srcDirs.add(dir)
      }
    }
    if (srcDirs.isEmpty) {
      System.out.println(s"Source directory contains no valid source folders: $baseSrcDir")
      return
    }
    if (!dstDir.exists && !dstDir.mkdirs) {
      System.out.println(s"Destination directory could not be created: $dstDir")
      return
    }
    if (!dstDir.isDirectory) {
      System.out.println(s"Destination is not a directory: $dstDir")
      return
    }
    process(srcDirs, srcFileNames, dstDir, unpacked, verbose)
  }

  /** Output usage text for the command line. */
  private def outputHelp(): Unit = {
    System.out.println("Usage: TzdbZoneRulesCompiler <options> <tzdb source filenames>")
    System.out.println("where options include:")
    System.out.println("   -srcdir <directory>   Where to find source directories (required)")
    System.out.println("   -dstdir <directory>   Where to output generated files (default srcdir)")
    System.out.println("   -version <version>    Specify the version, such as 2009a (optional)")
    System.out.println("   -unpacked             Generate dat files without jar files")
    System.out.println("   -help                 Print this usage message")
    System.out.println("   -verbose              Output verbose information during compilation")
    System.out.println(" There must be one directory for each version in srcdir")
    System.out.println(" Each directory must have the name of the version, such as 2009a")
    System.out.println(" Each directory must contain the unpacked tzdb files, such as asia or europe")
    System.out.println(" Directories must match the regex [12][0-9][0-9][0-9][A-Za-z0-9._-]+")
    System.out.println(" There will be one jar file for each version and one combined jar in dstdir")
    System.out.println(" If the version is specified, only that version is processed")
  }

  /** Process to create the jar files. */
  private def process(srcDirs: java.util.List[File], srcFileNames: java.util.List[String], dstDir: File, unpacked: Boolean, verbose: Boolean): Unit = {
    val deduplicateMap: java.util.Map[AnyRef, AnyRef] = new java.util.HashMap[AnyRef, AnyRef]
    val allBuiltZones: java.util.Map[String, java.util.SortedMap[String, ZoneRules]] = new java.util.TreeMap[String, java.util.SortedMap[String, ZoneRules]]
    val allRegionIds: java.util.Set[String] = new java.util.TreeSet[String]
    val allRules: java.util.Set[ZoneRules] = new java.util.HashSet[ZoneRules]
    var bestLeapSeconds: java.util.SortedMap[LocalDate, Byte] = null
    val srcDirsIterator = srcDirs.iterator
    while (srcDirsIterator.hasNext) {
      val srcDir = srcDirsIterator.next()
      scala.util.control.Breaks.breakable {
        val srcFiles: java.util.List[File] = new java.util.ArrayList[File]
        val srcFileNamesIterator = srcFileNames.iterator
        while (srcFileNamesIterator.hasNext) {
          val srcFileName = srcFileNamesIterator.next()
          val file: File = new File(srcDir, srcFileName)
          if (file.exists)
            srcFiles.add(file)
        }
        if (srcFiles.isEmpty)
          scala.util.control.Breaks.break()
        var leapSecondsFile: File = new File(srcDir, "leapseconds")
        if (!leapSecondsFile.exists) {
          System.out.println(s"Version ${srcDir.getName} does not include leap seconds information.")
          leapSecondsFile = null
        }
        val loopVersion: String = srcDir.getName
        val compiler: TzdbZoneRulesCompiler = new TzdbZoneRulesCompiler(loopVersion, srcFiles, leapSecondsFile, verbose)
        compiler.setDeduplicateMap(deduplicateMap)
        try {
          compiler.compile()
          val builtZones: java.util.SortedMap[String, ZoneRules] = compiler.getZones
          val parsedLeapSeconds: java.util.SortedMap[LocalDate, Byte] = compiler.getLeapSeconds
          if (!unpacked) {
            val dstFile: File = new File(dstDir, s"threeten-TZDB-$loopVersion.jar")
            if (verbose)
              System.out.println(s"Outputting file: $dstFile")
            outputFile(dstFile, loopVersion, builtZones, parsedLeapSeconds)
          }
          allBuiltZones.put(loopVersion, builtZones)
          allRegionIds.addAll(builtZones.keySet)
          allRules.addAll(builtZones.values)
          if (compiler.getMostRecentLeapSecond != null) {
            if (bestLeapSeconds == null || compiler.getMostRecentLeapSecond.compareTo(bestLeapSeconds.lastKey) > 0)
              bestLeapSeconds = parsedLeapSeconds
          }
        }
        catch {
          case ex: Exception =>
            System.out.println(s"Failed: ${ex.toString}")
            ex.printStackTrace()
            System.exit(1)
        }
      }
    }
    if (unpacked) {
      if (verbose)
        System.out.println(s"Outputting combined files: $dstDir")
      outputFilesDat(dstDir, allBuiltZones, allRegionIds, allRules, bestLeapSeconds)
    }
    else {
      val dstFile: File = new File(dstDir, "threeten-TZDB-all.jar")
      if (verbose)
        System.out.println(s"Outputting combined file: $dstFile")
      outputFile(dstFile, allBuiltZones, allRegionIds, allRules, bestLeapSeconds)
    }
  }

  /** Outputs the DAT files. */
  private def outputFilesDat(dstDir: File, allBuiltZones: java.util.Map[String, java.util.SortedMap[String, ZoneRules]], allRegionIds: java.util.Set[String], allRules: java.util.Set[ZoneRules], leapSeconds: java.util.SortedMap[LocalDate, Byte]): Unit = {
    val tzdbFile: File = new File(dstDir, "TZDB.dat")
    tzdbFile.delete
    try {
      var fos: FileOutputStream = null
      try {
        fos = new FileOutputStream(tzdbFile)
        outputTzdbDat(fos, allBuiltZones, allRegionIds, allRules)
      } finally {
        if (fos != null)
          fos.close()
      }
    }
    catch {
      case ex: Exception =>
        System.out.println(s"Failed: ${ex.toString}")
        ex.printStackTrace()
        System.exit(1)
    }
  }

  /** Outputs the file. */
  private def outputFile(dstFile: File, version: String, builtZones: java.util.SortedMap[String, ZoneRules], leapSeconds: java.util.SortedMap[LocalDate, Byte]): Unit = {
    val loopAllBuiltZones: java.util.Map[String, java.util.SortedMap[String, ZoneRules]] = new java.util.TreeMap[String, java.util.SortedMap[String, ZoneRules]]
    loopAllBuiltZones.put(version, builtZones)
    val loopAllRegionIds: java.util.Set[String] = new java.util.TreeSet[String](builtZones.keySet)
    val loopAllRules: java.util.Set[ZoneRules] = new java.util.HashSet[ZoneRules](builtZones.values)
    outputFile(dstFile, loopAllBuiltZones, loopAllRegionIds, loopAllRules, leapSeconds)
  }

  /** Outputs the file. */
  private def outputFile(dstFile: File, allBuiltZones: java.util.Map[String, java.util.SortedMap[String, ZoneRules]], allRegionIds: java.util.Set[String], allRules: java.util.Set[ZoneRules], leapSeconds: java.util.SortedMap[LocalDate, Byte]): Unit = {
    var jos: JarOutputStream = null
    try {
      jos = new JarOutputStream(new FileOutputStream(dstFile))
      outputTzdbEntry(jos, allBuiltZones, allRegionIds, allRules)
    }
    catch {
      case ex: Exception =>
        System.out.println(s"Failed: ${ex.toString}")
        ex.printStackTrace()
        System.exit(1)
    } finally {
      if (jos != null) {
        try jos.close()
        catch {
          case ex: IOException =>
        }
      }
    }
  }

  /** Outputs the timezone entry in the JAR file. */
  private def outputTzdbEntry(jos: JarOutputStream, allBuiltZones: java.util.Map[String, java.util.SortedMap[String, ZoneRules]], allRegionIds: java.util.Set[String], allRules: java.util.Set[ZoneRules]): Unit = {
    try {
      jos.putNextEntry(new ZipEntry("org/threeten/bp/TZDB.dat"))
      outputTzdbDat(jos, allBuiltZones, allRegionIds, allRules)
      jos.closeEntry()
    }
    catch {
      case ex: Exception =>
        System.out.println(s"Failed: ${ex.toString}")
        ex.printStackTrace()
        System.exit(1)
    }
  }

  /** Outputs the timezone DAT file. */
  @throws(classOf[IOException])
  private def outputTzdbDat(jos: OutputStream, allBuiltZones: java.util.Map[String, java.util.SortedMap[String, ZoneRules]], allRegionIds: java.util.Set[String], allRules: java.util.Set[ZoneRules]): Unit = {
    val out: DataOutputStream = new DataOutputStream(jos)
    out.writeByte(1)
    out.writeUTF("TZDB")
    val versionArray: Array[String] = allBuiltZones.keySet.toArray(new Array[String](allBuiltZones.size))
    out.writeShort(versionArray.length)
    for (version <- versionArray) {
      out.writeUTF(version)
    }
    val regionArray: Array[String] = allRegionIds.toArray(new Array[String](allRegionIds.size))
    out.writeShort(regionArray.length)
    for (regionId <- regionArray) {
      out.writeUTF(regionId)
    }
    val rulesList: java.util.List[ZoneRules] = new java.util.ArrayList[ZoneRules](allRules)
    out.writeShort(rulesList.size)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream(1024)
    val rulesIterator = rulesList.iterator
    while (rulesIterator.hasNext) {
      val rules = rulesIterator.next()
      baos.reset()
      val dataos: DataOutputStream = new DataOutputStream(baos)
      Ser.write(rules, dataos)
      dataos.close()
      val bytes: Array[Byte] = baos.toByteArray
      out.writeShort(bytes.length)
      out.write(bytes)
    }
    val versions = allBuiltZones.keySet.iterator
    while (versions.hasNext) {
      val version = versions.next()
      out.writeShort(allBuiltZones.get(version).size)
      val entries = allBuiltZones.get(version).entrySet.iterator()
      while (entries.hasNext) {
        val entry = entries.next()
        val regionIndex: Int = Arrays.binarySearch(regionArray.asInstanceOf[Array[AnyRef]], entry.getKey)
        val rulesIndex: Int = rulesList.indexOf(entry.getValue)
        out.writeShort(regionIndex)
        out.writeShort(rulesIndex)
      }
    }
    out.flush()
  }

  /** Class representing a rule line in the TZDB file.
    *
    * Constructs a rule using fields.
    * @param leapDate Date which has gets leap second adjustment (at the end)
    * @param secondAdjustment +1 or -1 for inserting or dropping a second
    */
  private[zone] final class LeapSecondRule(private[zone] val leapDate: LocalDate, private[zone] var secondAdjustment: Byte)
}

/** A builder that can read the TZDB time-zone files and build {@code ZoneRules} instances.
  *
  * <h3>Specification for implementors</h3>
  * This class is a mutable builder. A new instance must be created for each compile.
  *
  * @constructor Creates an instance if you want to invoke the compiler manually.
  *
  * @param version  the version, such as 2009a, not null
  * @param sourceFiles  the list of source files, not empty, not null
  * @param verbose  whether to output verbose messages
  */
final class TzdbZoneRulesCompiler(private val version: String, private val sourceFiles: java.util.List[File], private val leapSecondsFile: File, private val verbose: Boolean) {
  /** The TZDB rules. */
  private val rules: java.util.Map[String, java.util.List[TzdbZoneRulesCompiler#TZDBRule]] = new java.util.HashMap[String, java.util.List[TzdbZoneRulesCompiler#TZDBRule]]
  /** The TZDB zones. */
  private val zones: java.util.Map[String, java.util.List[TzdbZoneRulesCompiler#TZDBZone]] = new java.util.HashMap[String, java.util.List[TzdbZoneRulesCompiler#TZDBZone]]
  /** The TZDB links. */
  private val links: java.util.Map[String, String] = new java.util.HashMap[String, String]
  /** The built zones. */
  private val builtZones: java.util.SortedMap[String, ZoneRules] = new java.util.TreeMap[String, ZoneRules]
  /** A map to deduplicate object instances. */
  private var deduplicateMap: java.util.Map[AnyRef, AnyRef] = new java.util.HashMap[AnyRef, AnyRef]
  /** Sorted collection of LeapSecondRules. */
  private val leapSeconds: java.util.SortedMap[LocalDate, Byte] = new java.util.TreeMap[LocalDate, Byte]

  /** Compile the rules file.
    *
    * Use {@link #getZones()} and {@link #getLeapSeconds()} to retrieve the parsed data.
    *
    * @throws Exception if an error occurs
    */
  @throws[Exception]
  def compile(): Unit = {
    printVerbose(s"Compiling TZDB version $version")
    parseFiles()
    parseLeapSecondsFile()
    buildZoneRules()
    printVerbose(s"Compiled TZDB version $version")
  }

  /** Gets the parsed zone rules.
    *
    * @return the parsed zone rules, not null
    */
  def getZones: java.util.SortedMap[String, ZoneRules] = builtZones

  /** Gets the parsed leap seconds.
    *
    * @return the parsed and sorted leap seconds, not null
    */
  def getLeapSeconds: java.util.SortedMap[LocalDate, Byte] = leapSeconds

  /** Gets the most recent leap second.
    *
    * @return the most recent leap second, null if none
    */
  private def getMostRecentLeapSecond: LocalDate = if (leapSeconds.isEmpty) null else leapSeconds.lastKey

  /** Sets the deduplication map.
    *
    * @param deduplicateMap  the map to deduplicate items
    */
  private[zone] def setDeduplicateMap(deduplicateMap: java.util.Map[AnyRef, AnyRef]): Unit =
    this.deduplicateMap = deduplicateMap

  /** Parses the source files.
    *
    * @throws Exception if an error occurs
    */
  @throws[Exception]
  private def parseFiles(): Unit = {
    val files = sourceFiles.iterator
    while (files.hasNext) {
      val file = files.next()
      printVerbose(s"Parsing file: $file")
      parseFile(file)
    }
  }

  /** Parses the leap seconds file.
    *
    * @throws Exception if an error occurs
    */
  @throws[Exception]
  private def parseLeapSecondsFile(): Unit = {
    printVerbose(s"Parsing leap second file: $leapSecondsFile")
    var lineNumber: Int = 1
    var line: String = null
    var in: BufferedReader = null
    try {
      in = new BufferedReader(new FileReader(leapSecondsFile))
      while ({line = in.readLine; line} != null) {
        scala.util.control.Breaks.breakable {
          val index: Int = line.indexOf('#')
          if (index >= 0)
            line = line.substring(0, index)
          if (line.trim.length == 0)
            scala.util.control.Breaks.break()
          val secondRule: TzdbZoneRulesCompiler.LeapSecondRule = parseLeapSecondRule(line)
          leapSeconds.put(secondRule.leapDate, secondRule.secondAdjustment)
          lineNumber += 1
        }
      }
    } catch {
      case ex: Exception =>
        throw new Exception(s"Failed while processing file '$leapSecondsFile' on line $lineNumber '$line'", ex)
    } finally {
      try {
        if (in != null)
          in.close()
      }
      catch {
        case ex: Exception =>
      }
    }
  }

  private def parseLeapSecondRule(line: String): TzdbZoneRulesCompiler.LeapSecondRule = {
    val st: StringTokenizer = new StringTokenizer(line, " \t")
    val first: String = st.nextToken
    if (first == "Leap") {
      if (st.countTokens < 6) {
        printVerbose(s"Invalid leap second line in file: $leapSecondsFile, line: $line")
        throw new IllegalArgumentException("Invalid leap second line")
      }
    }
    else
      throw new IllegalArgumentException("Unknown line")
    val year: Int = st.nextToken.toInt
    val month: Month = parseMonth(st.nextToken)
    val dayOfMonth: Int = st.nextToken.toInt
    val leapDate: LocalDate = LocalDate.of(year, month, dayOfMonth)
    val timeOfLeapSecond: String = st.nextToken
    var adjustmentByte: Byte = 0
    val adjustment: String = st.nextToken
    if (adjustment == "+") {
      if ("23:59:60" != timeOfLeapSecond)
        throw new IllegalArgumentException(s"Leap seconds can only be inserted at 23:59:60 - Date:$leapDate")
      adjustmentByte = +1
    }
    else if (adjustment == "-") {
      if ("23:59:59" != timeOfLeapSecond)
        throw new IllegalArgumentException(s"Leap seconds can only be removed at 23:59:59 - Date:$leapDate")
      adjustmentByte = -1
    }
    else
      throw new IllegalArgumentException(s"Invalid adjustment '$adjustment' in leap second rule for $leapDate")
    val rollingOrStationary: String = st.nextToken
    if (!"S".equalsIgnoreCase(rollingOrStationary))
      throw new IllegalArgumentException(s"Only stationary ('S') leap seconds are supported, not '$rollingOrStationary'")
    new TzdbZoneRulesCompiler.LeapSecondRule(leapDate, adjustmentByte)
  }

  /** Parses a source file.
    *
    * @param file  the file being read, not null
    * @throws Exception if an error occurs
    */
  @throws[Exception]
  private def parseFile(file: File): Unit = {
    var lineNumber: Int = 1
    var line: String = null
    var in: BufferedReader = null
    try {
      in = new BufferedReader(new FileReader(file))
      var openZone: java.util.List[TzdbZoneRulesCompiler#TZDBZone] = null
      while ({line = in.readLine; line} != null) {
        scala.util.control.Breaks.breakable {
          {
            val index: Int = line.indexOf('#')
            if (index >= 0)
              line = line.substring(0, index)
            if (line.trim.length == 0)
              scala.util.control.Breaks.break()
            val st: StringTokenizer = new StringTokenizer(line, " \t")
            if (openZone != null && Character.isWhitespace(line.charAt(0)) && st.hasMoreTokens) {
              if (parseZoneLine(st, openZone))
                openZone = null
            }
            else {
              if (st.hasMoreTokens) {
                val first: String = st.nextToken
                if (first == "Zone") {
                  if (st.countTokens < 3) {
                    printVerbose(s"Invalid Zone line in file: $file, line: $line")
                    throw new IllegalArgumentException("Invalid Zone line")
                  }
                  openZone = new java.util.ArrayList[TzdbZoneRulesCompiler#TZDBZone]
                  zones.put(st.nextToken, openZone)
                  if (parseZoneLine(st, openZone))
                    openZone = null
                }
                else {
                  openZone = null
                  if (first == "Rule") {
                    if (st.countTokens < 9) {
                      printVerbose(s"Invalid Rule line in file: $file, line: $line")
                      throw new IllegalArgumentException("Invalid Rule line")
                    }
                    parseRuleLine(st)
                  }
                  else if (first == "Link") {
                    if (st.countTokens < 2) {
                      printVerbose(s"Invalid Link line in file: $file, line: $line")
                      throw new IllegalArgumentException("Invalid Link line")
                    }
                    val realId: String = st.nextToken
                    val aliasId: String = st.nextToken
                    links.put(aliasId, realId)
                  }
                  else
                    throw new IllegalArgumentException("Unknown line")
                }
              }
            }
          }
          lineNumber += 1
        }
      }
    }
    catch {
      case ex: Exception =>
        throw new Exception(s"Failed while processing file '$file' on line $lineNumber '$line'", ex)
    } finally {
      if (in != null)
        in.close()
    }
  }

  /** Parses a Rule line.
    *
    * @param st  the tokenizer, not null
    */
  private def parseRuleLine(st: StringTokenizer): Unit = {
    val rule: TzdbZoneRulesCompiler#TZDBRule = new TZDBRule
    val name: String = st.nextToken
    if (!rules.containsKey(name))
      rules.put(name, new java.util.ArrayList[TzdbZoneRulesCompiler#TZDBRule])
    rules.get(name).add(rule)
    rule.startYear = parseYear(st.nextToken, 0)
    rule.endYear = parseYear(st.nextToken, rule.startYear)
    if (rule.startYear > rule.endYear)
      throw new IllegalArgumentException(s"Year order invalid: ${rule.startYear} > ${rule.endYear}")
    parseOptional(st.nextToken)
    parseMonthDayTime(st, rule)
    rule.savingsAmount = parsePeriod(st.nextToken)
    rule.text = parseOptional(st.nextToken)
  }

  /** Parses a Zone line.
    *
    * @param st  the tokenizer, not null
    * @return true if the zone is complete
    */
  private def parseZoneLine(st: StringTokenizer, zoneList: java.util.List[TzdbZoneRulesCompiler#TZDBZone]): Boolean = {
    val zone: TzdbZoneRulesCompiler#TZDBZone = new TZDBZone
    zoneList.add(zone)
    zone.standardOffset = parseOffset(st.nextToken)
    val savingsRule: String = parseOptional(st.nextToken)
    if (savingsRule == null) {
      zone.fixedSavingsSecs = 0
      zone.savingsRule = null
    }
    else {
      try {
        zone.fixedSavingsSecs = parsePeriod(savingsRule)
        zone.savingsRule = null
      }
      catch {
        case ex: Exception =>
          zone.fixedSavingsSecs = null
          zone.savingsRule = savingsRule
      }
    }
    zone.text = st.nextToken
    if (st.hasMoreTokens) {
      zone.year = Year.of(st.nextToken.toInt)
      if (st.hasMoreTokens) {
        parseMonthDayTime(st, zone)
      }
      false
    }
    else {
      true
    }
  }

  /** Parses a Rule line.
    *
    * @param st  the tokenizer, not null
    * @param mdt  the object to parse into, not null
    */
  private def parseMonthDayTime(st: StringTokenizer, mdt: TzdbZoneRulesCompiler#TZDBMonthDayTime): Unit = {
    mdt.month = parseMonth(st.nextToken)
    if (st.hasMoreTokens) {
      var dayRule: String = st.nextToken
      if (dayRule.startsWith("last")) {
        mdt.dayOfMonth = -1
        mdt.dayOfWeek = parseDayOfWeek(dayRule.substring(4))
        mdt.adjustForwards = false
      }
      else {
        var index: Int = dayRule.indexOf(">=")
        if (index > 0) {
          mdt.dayOfWeek = parseDayOfWeek(dayRule.substring(0, index))
          dayRule = dayRule.substring(index + 2)
        }
        else {
          index = dayRule.indexOf("<=")
          if (index > 0) {
            mdt.dayOfWeek = parseDayOfWeek(dayRule.substring(0, index))
            mdt.adjustForwards = false
            dayRule = dayRule.substring(index + 2)
          }
        }
        mdt.dayOfMonth = dayRule.toInt
      }
      if (st.hasMoreTokens) {
        val timeStr: String = st.nextToken
        var secsOfDay: Int = parseSecs(timeStr)
        if (secsOfDay == 86400) {
          mdt.endOfDay = true
          secsOfDay = 0
        }
        val time: LocalTime = deduplicate(LocalTime.ofSecondOfDay(secsOfDay))
        mdt.time = time
        mdt.timeDefinition = parseTimeDefinition(timeStr.charAt(timeStr.length - 1))
      }
    }
  }

  private def parseYear(str: String, defaultYear: Int): Int = {
    val _str = str.toLowerCase
    if (matches(_str, "minimum"))
      Year.MIN_VALUE
    else if (matches(_str, "maximum"))
      Year.MAX_VALUE
    else if (_str == "only")
      defaultYear
    else
      _str.toInt
  }

  private def parseMonth(str: String): Month = {
    val _str = str.toLowerCase
    for (moy <- Month.values) {
      if (matches(_str, moy.name.toLowerCase))
        return moy
    }
    throw new IllegalArgumentException(s"Unknown month: ${_str}")
  }

  private def parseDayOfWeek(str: String): DayOfWeek = {
    val _str = str.toLowerCase
    for (dow <- DayOfWeek.values) {
      if (matches(_str, dow.name.toLowerCase))
        return dow
    }
    throw new IllegalArgumentException(s"Unknown day-of-week: ${_str}")
  }

  private def matches(str: String, search: String): Boolean =
    str.startsWith(search.substring(0, 3)) && search.startsWith(str) && str.length <= search.length

  private def parseOptional(str: String): String = if (str == "-") null else str

  private def parseSecs(str: String): Int = {
    if (str == "-") {
      return 0
    }
    var pos: Int = 0
    if (str.startsWith("-")) {
      pos = 1
    }
    val pp: ParsePosition = new ParsePosition(pos)
    val parsed: TemporalAccessor = TzdbZoneRulesCompiler.TIME_PARSER.parseUnresolved(str, pp)
    if (parsed == null || pp.getErrorIndex >= 0) {
      throw new IllegalArgumentException(str)
    }
    val hour: Long = parsed.getLong(HOUR_OF_DAY)
    val min: java.lang.Long = if (parsed.isSupported(MINUTE_OF_HOUR)) parsed.getLong(MINUTE_OF_HOUR) else null
    val sec: java.lang.Long = if (parsed.isSupported(SECOND_OF_MINUTE)) parsed.getLong(SECOND_OF_MINUTE) else null
    var secs: Int = (hour * 60 * 60 + (if (min != null) min.toInt else 0) * 60 + (if (sec != null) sec.toInt else 0)).toInt
    if (pos == 1) {
      secs = -secs
    }
    secs
  }

  private def parseOffset(str: String): ZoneOffset = {
    val secs: Int = parseSecs(str)
    ZoneOffset.ofTotalSeconds(secs)
  }

  private def parsePeriod(str: String): Int = parseSecs(str)

  private def parseTimeDefinition(c: Char): ZoneOffsetTransitionRule.TimeDefinition =
    c match {
      case 's' | 'S'                         => TimeDefinition.STANDARD
      case 'u' | 'U' | 'g' | 'G' | 'z' | 'Z' => TimeDefinition.UTC
      case 'w' | 'W' | _                     => TimeDefinition.WALL
    }

  /** Build the rules, zones and links into real zones.
    *
    * @throws Exception if an error occurs
    */
  @throws[Exception]
  private def buildZoneRules(): Unit = {
    val zonesIterator = zones.keySet.iterator
    while (zonesIterator.hasNext) {
      var zoneId = zonesIterator.next()
      printVerbose(s"Building zone ${zoneId}")
      zoneId = deduplicate(zoneId)
      val tzdbZones: java.util.Iterator[TzdbZoneRulesCompiler#TZDBZone] = zones.get(zoneId).iterator
      var bld: ZoneRulesBuilder = new ZoneRulesBuilder

      while (tzdbZones.hasNext) {
        val tzdbZone = tzdbZones.next()
        bld = tzdbZone.addToBuilder(bld, rules)
      }
      val buildRules: ZoneRules = bld.toRules(zoneId, deduplicateMap)
      builtZones.put(zoneId, deduplicate(buildRules))
    }
    val aliasesIterator = links.keySet.iterator
    while (aliasesIterator.hasNext) {
      var aliasId = aliasesIterator.next()
      aliasId = deduplicate(aliasId)
      var realId: String = links.get(aliasId)
      printVerbose(s"Linking alias ${aliasId} to $realId")
      var realRules: ZoneRules = builtZones.get(realId)
      if (realRules == null) {
        realId = links.get(realId)
        printVerbose(s"Relinking alias ${aliasId} to $realId")
        realRules = builtZones.get(realId)
        if (realRules == null) {
          throw new IllegalArgumentException(s"Alias '${aliasId}' links to invalid zone '$realId' for '$version'")
        }
      }
      builtZones.put(aliasId, realRules)
    }
    builtZones.remove("UTC")
    builtZones.remove("GMT")
    builtZones.remove("GMT0")
    builtZones.remove("GMT+0")
    builtZones.remove("GMT-0")
  }

  /** Deduplicates an object instance.
    *
    * @tparam T the generic type
    * @param obj  the object to deduplicate
    * @return the deduplicated object
    */
  private[zone] def deduplicate[T <: AnyRef](obj: T): T = {
    if (!deduplicateMap.containsKey(obj))
      deduplicateMap.put(obj, obj)
    deduplicateMap.get(obj).asInstanceOf[T]
  }

  /** Prints a verbose message.
    *
    * @param message  the message, not null
    */
  private def printVerbose(message: String): Unit =
    if (verbose)
      System.out.println(message)

  /** Class representing a month-day-time in the TZDB file. */
  private[zone] abstract class TZDBMonthDayTime {
    /** The month of the cutover. */
    private[zone] var month: Month = Month.JANUARY
    /** The day-of-month of the cutover. */
    private[zone] var dayOfMonth: Int = 1
    /** Whether to adjust forwards. */
    private[zone] var adjustForwards: Boolean = true
    /** The day-of-week of the cutover. */
    private[zone] var dayOfWeek: DayOfWeek = null
    /** The time of the cutover. */
    private[zone] var time: LocalTime = LocalTime.MIDNIGHT
    /** Whether this is midnight end of day. */
    private[zone] var endOfDay: Boolean = false
    /** The time of the cutover. */
    private[zone] var timeDefinition: ZoneOffsetTransitionRule.TimeDefinition = TimeDefinition.WALL

    private[zone] def adjustToFowards(year: Int): Unit = {
      if (!adjustForwards && dayOfMonth > 0) {
        val adjustedDate: LocalDate = LocalDate.of(year, month, dayOfMonth).minusDays(6)
        dayOfMonth = adjustedDate.getDayOfMonth
        month = adjustedDate.getMonth
        adjustForwards = true
      }
    }
  }

  /** Class representing a rule line in the TZDB file. */
  private[zone] final class TZDBRule extends TZDBMonthDayTime {
    /** The start year. */
    private[zone] var startYear: Int = 0
    /** The end year. */
    private[zone] var endYear: Int = 0
    /** The amount of savings. */
    private[zone] var savingsAmount: Int = 0
    /** The text name of the zone. */
    private[zone] var text: String = null

    private[zone] def addToBuilder(bld: ZoneRulesBuilder): Unit = {
      adjustToFowards(2004)
      bld.addRuleToWindow(startYear, endYear, month, dayOfMonth, dayOfWeek, time, endOfDay, timeDefinition, savingsAmount)
    }
  }

  /** Class representing a linked set of zone lines in the TZDB file. */
  private[zone] final class TZDBZone extends TZDBMonthDayTime {
    /** The standard offset. */
    private[zone] var standardOffset: ZoneOffset = null
    /** The fixed savings amount. */
    private[zone] var fixedSavingsSecs: Integer = null
    /** The savings rule. */
    private[zone] var savingsRule: String = null
    /** The text name of the zone. */
    private[zone] var text: String = null
    /** The year of the cutover. */
    private[zone] var year: Year = null

    private[zone] def addToBuilder(bld: ZoneRulesBuilder, rules: java.util.Map[String, java.util.List[TzdbZoneRulesCompiler#TZDBRule]]): ZoneRulesBuilder = {
      if (year != null)
        bld.addWindow(standardOffset, toDateTime(year.getValue), timeDefinition)
      else
        bld.addWindowForever(standardOffset)
      if (fixedSavingsSecs != null)
        bld.setFixedSavingsToWindow(fixedSavingsSecs)
      else {
        val tzdbRules: java.util.List[TzdbZoneRulesCompiler#TZDBRule] = rules.get(savingsRule)
        if (tzdbRules == null)
          throw new IllegalArgumentException(s"Rule not found: $savingsRule")
        val tzdbRulesIterator = tzdbRules.iterator
        while (tzdbRulesIterator.hasNext) {
          val tzdbRule = tzdbRulesIterator.next()
          tzdbRule.addToBuilder(bld)
        }
      }
      bld
    }

    private def toDateTime(year: Int): LocalDateTime = {
      adjustToFowards(year)
      var date: LocalDate = null
      if (dayOfMonth == -1) {
        dayOfMonth = month.length(Year.isLeap(year))
        date = LocalDate.of(year, month, dayOfMonth)
        if (dayOfWeek != null)
          date = date.`with`(TemporalAdjusters.previousOrSame(dayOfWeek))
      }
      else {
        date = LocalDate.of(year, month, dayOfMonth)
        if (dayOfWeek != null)
          date = date.`with`(TemporalAdjusters.nextOrSame(dayOfWeek))
      }
      date = deduplicate(date)
      var ldt: LocalDateTime = LocalDateTime.of(date, time)
      if (endOfDay)
        ldt = ldt.plusDays(1)
      ldt
    }
  }
}
