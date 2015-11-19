/*
 * Copyright 2014-2015 Websudos Ltd, Sphonic Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.websudos.phantom.sbt

import java.util.concurrent.atomic.AtomicBoolean

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import sbt.Keys._
import sbt._

import scala.concurrent.blocking
import scala.util.control.NonFatal

/**
 * sbt plugin for starting Cassandra in embedded mode before running
 * the tests.
 *
 * First the plugin must be included in your `plugins.sbt`:
 *
 * {{{
 *   addSbtPlugin("com.websudos" %% "phantom-sbt" % "1.13.0")
 * }}}
 The plugin does the following
 * things:
 *
 * - Automatically starts Cassandra in embedded mode whenever the test task is run
 * - Forces the tests for the projects that include the settings to always run in a
 *   forked JVM as this is the only way to make parallel tests using phantom work.
 *   (This is not caused by the implementation of this plugin or the new connector
 *   or zookeeper artifacts, this is caused by implementation details in the official
 *   `phantom-dsl` artifact, mainly the use of reflection which is not thread-safe
 *   in Scala 2.10)
 *
 * If you want to specify a custom Cassandra configuration,
 * you can do that with a setting:
 *
 * {{{
 *   phantomCassandraConfig := baseDirectory.value / "config" / "cassandra.yaml"
 * }}}
 */
object PhantomSbtPlugin extends AutoPlugin {

  override def requires = sbt.plugins.JvmPlugin

  /**
   * Keys for all settings of this plugin.
   */
  object autoImport {

    val phantomStartEmbeddedCassandra = taskKey[Unit]("Starts embedded Cassandra")
    val phantomCleanupEmbeddedCassandra = taskKey[Unit]("Clean up embedded Cassandra by dropping all of its keyspaces")

    val phantomCassandraConfig = settingKey[Option[File]]("YAML file for Cassandra configuration")
  }

  import autoImport._


  override def projectSettings = Seq(
    phantomCassandraConfig := None,
    phantomStartEmbeddedCassandra := EmbeddedCassandra.start(phantomCassandraConfig.value, streams.value.log),
    phantomCleanupEmbeddedCassandra := EmbeddedCassandra.cleanup(streams.value.log),
    test in Test <<= (test in Test).dependsOn(phantomStartEmbeddedCassandra),
    testQuick in Test <<= (testQuick in Test).dependsOn(phantomStartEmbeddedCassandra),
    testOnly in Test <<= (testOnly in Test).dependsOn(phantomStartEmbeddedCassandra),
    fork := true
  )
}

/**
 * Singleton object that is responsible for starting
 * Cassandra in embedded mode, but only once.
 * Subsequent calls to `start` will be ignored.
 */
object EmbeddedCassandra {

  println("Initialize EmbeddedCassandra singleton.")

  private[this] val started = new AtomicBoolean(false)

  /**
   * Starts Cassandra in embedded mode if it has not been
   * started yet.
   */
  def start (config: Option[File], logger: Logger): Unit = {
    this.synchronized {
      if (started.compareAndSet(false, true)) {
        blocking {
          val configFile = config.map(_.toURI.toString) getOrElse EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE
          System.setProperty("cassandra.config", configFile)
          try {
            EmbeddedCassandraServerHelper.mkdirs()
          } catch {
            case NonFatal(e) =>
              logger.error(s"Error creating Embedded cassandra directories: ${e.getMessage}")
          }
          config match {
            case Some(file) =>
              logger.info(s"Starting Cassandra in embedded mode with configuration from $file.")
              EmbeddedCassandraServerHelper.startEmbeddedCassandra(file,
                EmbeddedCassandraServerHelper.DEFAULT_TMP_DIR, EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT)
            case None =>
              logger.info("Starting Cassandra in embedded mode with default configuration.")
              EmbeddedCassandraServerHelper.startEmbeddedCassandra()
          }
        }
      } else {
        logger.info("Embedded Cassandra has already been started")
      }
    }
  }

  def cleanup (logger: Logger): Unit = {
    this.synchronized {
      if (started) {
        logger.info("Cleaning up embedded Cassandra")
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
      } else {
        logger.info("Cassandra is not running, not cleaning up")
      }
    }
  }
}
