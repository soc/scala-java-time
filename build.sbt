import org.scalajs.sbtplugin.ScalaJSJUnitPlugin
import sbt._

val scalaVer = "2.11.8"
val crossScalaVer = Seq(scalaVer, "2.12.0-M5")

lazy val commonSettings = Seq(
  name         := "scala-java-time",
  version      := "2.0-M1",
  organization := "io.github.soc",
  homepage     := Some(url("https://github.com/soc/scala-java-time")),
  licenses     := Seq("BSD 3-Clause License" -> url("https://opensource.org/licenses/BSD-3-Clause")),

  scalacOptions ++= Seq("-Xexperimental"),
  scalaVersion       := scalaVer,
  crossScalaVersions := crossScalaVer,

  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
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

lazy val scalajavatimeRoot = project.in(file("."))
  .aggregate(scalajavatimeJVM, scalajavatimeJS)
  .settings(
    scalaVersion := scalaVer,
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    crossScalaVersions := crossScalaVer
  )

lazy val scalajavatimeCross = crossProject.crossType(CrossType.Full).in(file("."))
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .settings(commonSettings: _*)
  .settings(
    testOptions += Tests.Argument(TestFramework("com.novocode.junit.JUnitFramework"), "-v", "-a")
  ).jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest"       % "3.0.0-RC4" % "test",
      "com.novocode"  %   "junit-interface" % "0.9"       % "test",
      "org.testng"    %   "testng"          % "6.9.10"    % "test"
    ),
    // Fork the JVM test to ensure that the custom flags are set
    fork in Test := true,
    baseDirectory in Test := baseDirectory.value.getParentFile,
    // Use CLDR provider for locales
    // https://docs.oracle.com/javase/8/docs/technotes/guides/intl/enhancements.8.html#cldr
    javaOptions in Test ++= Seq("-Djava.locale.providers=CLDR")
  ).jsSettings(
    libraryDependencies += "com.github.cquiroz" %%% "scala-java-locales" % "0.1.1+29"
  )

lazy val scalajavatimeJVM = scalajavatimeCross.jvm.settings(commonSettings: _*)

lazy val scalajavatimeJS  = scalajavatimeCross.js.settings(commonSettings: _*)

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
      <role>Current developer (Scala port)</role>
    </developer>
    <developer>
      <id>jodastephen</id>
      <name>Stephen Colebourne</name>
      <url>https://github.com/jodastephen</url>
      <role>Original developer (Java version)</role>
   </developer>
  </developers>
  <contributors>
    <contributor>
      <id>cquiroz</id>
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
