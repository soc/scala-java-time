
## Scala Java-Time

[![Build Status](https://travis-ci.org/soc/scala-java-time.svg?branch=master)](https://travis-ci.org/soc/scala-java-time)

This project provides an implementation of the `java.time` package, a date and time library that was added in Java 8.
The implementation is based on the original BSD-licensed reference implementation (before it was contributed to OpenJDK).

#### Building
This project builds using sbt.
Run `sbt scalajavatimeCrossJVM/test` to run the test suite on the JVM and
`sbt scalajavatimeCrossJS/test` to run the test suite in JavaScript.

#### Status

Most parts of this library work perfectly fine with Scala.js in the browser.
Locale, formatting and timezone support is limited and providing these missing pieces are the current focus of this project.
@cquiroz is currently working on implementing locale support.

#### Contributing

We welcome all contributions, including ideas, suggestions, bug reports, bug fixes and code!
We are especially interested in contributions that tackle the following issues:

 - *Support for formatting:* Formatting uses a lot of JDK classes, which we might not want to reimplement in Scala.js.
   We might be able to use the new `Intl` Web API.
 - *Support for timezones:* The timezone information is read from a binary blob, which won't work in the browser.
   We will have a look at other projects like moment.js and decide whether we want to use the same format, or come up with our own.

Have a look at the [issues](https://github.com/soc/scala-java-time/issues) to find something to work on! Let us know if you need help!

#### Plans

##### 2.0

We will keep releasing milestone builds while work on the remaining bits and pieces to support 100% of this library on Scala.js is ongoing (most parts work fine already).

The last milestone will rename the package name from `org.threeten.bp` to `java.time`.

A stable release of 2.0 will be published after a (hopefully) short RC phase.

##### 3.0

As soon as Scala-Native provides cross-compilation capabilities we will investigate what's necessary to compile this library to native code.

#### Time-zone data
The time-zone database is stored as a pre-compiled dat file that is included in the built jar.
The version of the time-zone data used is stored within the dat file (near the start).
Updating the time-zone database involves using the `TzdbZoneRulesCompiler` class
and re-compiling the jar file.
Pull requests with later versions of the dat file will be accepted.

#### FAQs

##### Is this project derived from OpenJDK?

No. This project is derived from the Reference Implementation previously hosted on GitHub.
That project had a BSD license, which has been preserved here.
Thus, this project is a fork of the original code before entry to OpenJDK.
