package io.ebeaninternal.server.autotune.service;

import io.avaje.applog.AppLog;
import io.ebean.config.AutoTuneConfig;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.autotune.AutoTuneService;
import io.ebeaninternal.server.autotune.model.Autotune;
import io.ebeaninternal.server.autotune.model.Origin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

/**
 * Implementation of the AutoTuneService which is comprised of profiling and query tuning.
 */
public class DefaultAutoTuneService implements AutoTuneService {

  private static final System.Logger logger = AppLog.getLogger(DefaultAutoTuneService.class);

  private final ReentrantLock lock = new ReentrantLock();

  private final SpiEbeanServer server;

  private final long defaultGarbageCollectionWait;

  private final boolean skipGarbageCollectionOnShutdown;

  private final boolean skipProfileReportingOnShutdown;

  private final BaseQueryTuner queryTuner;

  private final ProfileManager profileManager;

  private final boolean profiling;

  private final boolean queryTuning;

  private final String tuningFile;

  private final String profilingFile;

  private final String serverName;

  private final int profilingUpdateFrequency;

  private long runtimeChangeCount;

  public DefaultAutoTuneService(SpiEbeanServer server, DatabaseConfig databaseConfig) {
    AutoTuneConfig config = databaseConfig.getAutoTuneConfig();
    this.server = server;
    this.queryTuning = config.isQueryTuning();
    this.profiling = config.isProfiling();
    this.tuningFile = config.getQueryTuningFile();
    this.profilingFile = config.getProfilingFile();
    this.profilingUpdateFrequency = config.getProfilingUpdateFrequency();
    this.serverName = server.name();
    this.profileManager = new ProfileManager(config, server);
    this.queryTuner = new BaseQueryTuner(config, server, profileManager);
    this.skipGarbageCollectionOnShutdown = config.isSkipGarbageCollectionOnShutdown();
    this.skipProfileReportingOnShutdown = config.isSkipProfileReportingOnShutdown();
    this.defaultGarbageCollectionWait = config.getGarbageCollectionWait();
  }

  /**
   * Load the query tuning information from it's data store.
   */
  @Override
  public void startup() {

    if (queryTuning) {
      loadTuningFile();
      if (isRuntimeTuningUpdates()) {
        // periodically gather and update query tuning
        server.backgroundExecutor().scheduleWithFixedDelay(new ProfilingUpdate(), profilingUpdateFrequency, profilingUpdateFrequency, TimeUnit.SECONDS);
      }
    }
  }


  /**
   * Return true if the tuning should update periodically at runtime.
   */
  private boolean isRuntimeTuningUpdates() {
    return profilingUpdateFrequency > 0;
  }

  private class ProfilingUpdate implements Runnable {

    @Override
    public void run() {
      runtimeTuningUpdate();
    }
  }

  /**
   * Load tuning information from an existing tuning file.
   */
  private void loadTuningFile() {
    File file = new File(tuningFile);
    if (file.exists()) {
      loadAutoTuneProfiling(AutoTuneXmlReader.read(file));
    } else {
      // look for autotune as a resource
      try (InputStream stream = getClass().getResourceAsStream("/" + tuningFile)) {
        if (stream != null) {
          loadAutoTuneProfiling(AutoTuneXmlReader.read(stream));
        } else {
          logger.log(WARNING, "AutoTune file {0} not found - no initial automatic query tuning", tuningFile);
        }
      } catch (IOException e) {
        throw new IllegalStateException("Error on auto close of " + tuningFile, e);
      }
    }
  }

  private void loadAutoTuneProfiling(Autotune profiling) {
    logger.log(INFO, "AutoTune loading {0} tuning entries", profiling.getOrigin().size());
    for (Origin origin : profiling.getOrigin()) {
      queryTuner.put(origin);
    }
  }

  /**
   * Collect profiling, check for new/diff to existing tuning and apply changes.
   */
  private void runtimeTuningUpdate() {
    lock.lock();
    try {
      try {
        long start = System.currentTimeMillis();

        AutoTuneCollection profiling = profileManager.profilingCollection(false);

        AutoTuneDiffCollection event = new AutoTuneDiffCollection(profiling, queryTuner, true);
        event.process();
        if (event.isEmpty()) {
          long exeMillis = System.currentTimeMillis() - start;
          logger.log(DEBUG, "No query tuning updates for server:{0} executionMillis:{1}", serverName, exeMillis);

        } else {
          // report the query tuning changes that have been made
          runtimeChangeCount += event.getChangeCount();
          event.writeFile(profilingFile + "-" + serverName + "-update");
          long exeMillis = System.currentTimeMillis() - start;
          logger.log(INFO, "query tuning updates - new:{0} diff:{1} for server:{2} executionMillis:{3}", event.getNewCount(), event.getDiffCount(), serverName, exeMillis);
        }
      } catch (Throwable e) {
        logger.log(ERROR, "Error collecting or applying automatic query tuning", e);
      }
    } finally {
      lock.unlock();
    }
  }

  private void saveProfilingOnShutdown(boolean reset) {
    lock.lock();
    try {
      if (isRuntimeTuningUpdates()) {
        runtimeTuningUpdate();
        outputAllTuning();

      } else {

        AutoTuneCollection profiling = profileManager.profilingCollection(reset);

        AutoTuneDiffCollection event = new AutoTuneDiffCollection(profiling, queryTuner, false);
        event.process();
        if (event.isEmpty()) {
          logger.log(INFO, "No new or diff entries for profiling server:{0}", serverName);

        } else {
          event.writeFile(profilingFile + "-" + serverName);
          logger.log(INFO, "writing new:{0} diff:{1} profiling entries for server:{2}", event.getNewCount(), event.getDiffCount(), serverName);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Output all the query tuning (the "all" file).
   * <p>
   * This is the originally loaded tuning plus any tuning changes picked up and applied at runtime.
   * </p>
   * <p>
   * This "all" file can be used as the next "ebean-autotune.xml" file.
   * </p>
   */
  private void outputAllTuning() {

    if (runtimeChangeCount == 0) {
      logger.log(INFO, "no runtime query tuning changes for server:{0}", serverName);

    } else {
      AutoTuneAllCollection event = new AutoTuneAllCollection(queryTuner);
      int size = event.size();
      File existingTuning = new File(tuningFile);
      if (existingTuning.exists()) {
        // rename the existing autotune.xml file (appending 'now')
        if (!existingTuning.renameTo(new File(tuningFile + "." + AutoTuneXmlWriter.now()))) {
          logger.log(WARNING, "Failed to rename autotune file [{0}]", tuningFile);
        }
      }

      event.writeFile(tuningFile, false);
      logger.log(INFO, "query tuning detected [{0}] changes, writing all [{1}] tuning entries for server:{2}", runtimeChangeCount, size, serverName);
    }
  }

  /**
   * Shutdown the listener.
   * <p>
   * We should try to collect the usage statistics by calling a System.gc().
   * This is necessary for use with short lived applications where garbage
   * collection may not otherwise occur at all.
   * </p>
   */
  @Override
  public void shutdown() {
    if (profiling) {
      if (!skipGarbageCollectionOnShutdown && !skipProfileReportingOnShutdown) {
        // trigger GC to update profiling information on recently executed queries
        collectProfiling(-1);
      }
      if (!skipProfileReportingOnShutdown) {
        saveProfilingOnShutdown(false);
      }
    }
  }

  /**
   * Output the profiling.
   * <p>
   * When profiling updates are applied to tuning at runtime this reports all tuning and profiling combined.
   * When profiling is not applied at runtime then this reports the diff report with new and diff entries relative
   * to the existing tuning.
   * </p>
   */
  @Override
  public void reportProfiling() {
    saveProfilingOnShutdown(false);
  }

  /**
   * Ask for a System.gc() so that we gather node usage information.
   * <p>
   * Really only want to do this sparingly but useful just prior to shutdown
   * for short run application where garbage collection may otherwise not
   * occur at all.
   * </p>
   * <p>
   * waitMillis will do a thread sleep to give the garbage collection a little
   * time to do its thing assuming we are shutting down the VM.
   * </p>
   * <p>
   * If waitMillis is -1 then the defaultGarbageCollectionWait is used which
   * defaults to 100 milliseconds.
   * </p>
   */
  @Override
  public void collectProfiling() {
    collectProfiling(-1);
  }

  public void collectProfiling(long waitMillis) {
    System.gc();
    try {
      if (waitMillis < 0) {
        waitMillis = defaultGarbageCollectionWait;
      }
      Thread.sleep(waitMillis);
    } catch (InterruptedException e) {
      // restore the interrupted status
      Thread.currentThread().interrupt();
      logger.log(WARNING, "Error while sleeping after System.gc() request.", e);
    }
  }

  /**
   * Auto tune the query and enable profiling.
   */
  @Override
  public boolean tuneQuery(SpiQuery<?> query) {
    return queryTuner.tuneQuery(query);
  }

}
