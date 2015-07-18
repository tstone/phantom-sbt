  
import sbt._
import Keys._
import com.twitter.sbt._

object Build extends Build {
  
  val PhantomVersion = "1.10.0"
  val CassandraUnitVersion = "2.1.3.2"

  val publishSettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintray.BintrayKeys.bintrayOrganization := Some("websudos"),
    bintray.BintrayKeys.bintrayRepository := "oss-releases",
    bintray.BintrayKeys.bintrayReleaseOnPublish in ThisBuild := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )

  val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.websudos",
    version := PhantomVersion,
    scalaVersion := "2.10.5",
    resolvers ++= Seq(
      "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
      Resolver.bintrayRepo("websudos", "oss-releases")
    ),
    scalacOptions ++= Seq(
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-language:higherKinds",
      "-language:existentials",
      "-Yinline-warnings",
      "-Xlint",
      "-deprecation",
      "-feature",
      "-unchecked"
     ),
    fork in Test := true,
    javaOptions in Test ++= Seq("-Xmx2G")
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ publishSettings ++ StandardProject.newSettings

  lazy val phantomSbtPlugin = Project(
    id = "phantom-sbt",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "phantom-sbt",
    scalaVersion := "2.10.5",
    sbtPlugin := true,
    resolvers ++= Seq(
      Resolver.bintrayRepo("websudos", "oss-releases")
    ),
    libraryDependencies ++= Seq(
      "org.cassandraunit" % "cassandra-unit"  % CassandraUnitVersion  excludeAll (
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-jdk14")
      )
    )
  )

}
