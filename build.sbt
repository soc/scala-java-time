import sbt._

import io.github.soc.testng.{TestNGPlugin, TestNGScalaJSPlugin}

val scalaVer = "2.11.8"
val crossScalaVer = Seq(scalaVer, "2.12.0-M5")

lazy val commonSettings = Seq(
  name         := "scala-java-time",
  version      := "2.0.0-M3-SNAPSHOT",
  organization := "io.github.soc",
  homepage     := Some(url("https://github.com/soc/scala-java-time")),
  licenses     := Seq("BSD 3-Clause License" -> url("https://opensource.org/licenses/BSD-3-Clause")),

  scalacOptions ++= Seq("-Xexperimental"),
  scalaVersion       := scalaVer,
  crossScalaVersions := crossScalaVer,

  publishArtifact in Test := false,
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
  pomExtra := pomData,
  pomIncludeRepository := { _ => false }
)

lazy val root = project.in(file("."))
  .aggregate(scalajavatimeJVM, scalajavatimeJS)
  .settings(
    scalaVersion := scalaVer,
    crossScalaVersions := crossScalaVer,
    // No, SBT, we don't want any artifacts for root.
    // No, not even an empty jar.
    // Invoking Cthulhu:
    packageBin in Global := file(""),
    packagedArtifacts    := Map(),
    publish              := {},
    publishLocal         := {},
    publishArtifact      := false,
    Keys.`package`       := file(""))

lazy val scalajavatime = crossProject.crossType(CrossType.Full).in(file("."))
  .jvmConfigure(_.enablePlugins(TestNGPlugin))
  .jsConfigure(_.enablePlugins(TestNGScalaJSPlugin))
  .settings(commonSettings: _*)
  .jvmSettings(
    resolvers += Resolver.sbtPluginRepo("releases"),
    // Fork the JVM test to ensure that the custom flags are set
    fork in Test := true,
    baseDirectory in Test := baseDirectory.value.getParentFile,
    // Use CLDR provider for locales
    // https://docs.oracle.com/javase/8/docs/technotes/guides/intl/enhancements.8.html#cldr
    javaOptions in Test ++= Seq("-Djava.locale.providers=CLDR"),
    TestNGPlugin.testNGSuites := Seq(((resourceDirectory in Test).value / "testng.xml").absolutePath)
  ).jsSettings(
    libraryDependencies ++= Seq(
      "com.github.cquiroz" %%% "scala-java-locales" % "0.3.0+29"
    )
  )

lazy val scalajavatimeJVM = scalajavatime.jvm
lazy val scalajavatimeJS  = scalajavatime.js

lazy val pomData =
  <scm>
    <url>git@github.com:soc/scala-java-time.git</url>
    <connection>scm:git:git@github.com:soc/scala-java-time.git</connection>
  </scm>
  <developers>
    <developer>
      <id>soc</id>
      <name>Simon Ochsenreither</name>
      <url>https://github.com/soc</url>
      <roles>
        <role>Project Lead (current Scala version)</role>
      </roles>
    </developer>
    <developer>
      <id>jodastephen</id>
      <name>Stephen Colebourne</name>
      <url>https://github.com/jodastephen</url>
      <roles>
        <role>Project Lead (original Java implementation)</role>
      </roles>
   </developer>
  </developers>
  <contributors>
    <contributor>
      <name>Carlos Quiroz</name>
      <url>https://github.com/cquiroz/</url>
    </contributor>
    <contributor>
      <name>Keith Harris</name>
      <url>https://github.com/keithharris</url>
    </contributor>
    <contributor>
      <name>Ludovic Hochet</name>
      <url>https://github.com/lhochet</url>
    </contributor>
    <contributor>
      <name>Pap Lorinc</name>
      <url>https://github.com/paplorinc</url>
    </contributor>
    <contributor>
      <name>Michael Nascimento Santos</name>
      <url>https://github.com/sjmisterm</url>
    </contributor>
    <contributor>
      <name>Roger Riggs</name>
      <url>https://github.com/RogerRiggs</url>
    </contributor>
    <contributor>
      <name>Siebe Schaap</name>
      <url>https://github.com/sschaap</url>
    </contributor>
    <contributor>
      <name>Sherman Shen</name>
    </contributor>
  </contributors>
