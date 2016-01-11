package com.avaje.ebeaninternal.server.autotune.service;

import com.avaje.ebean.config.AutoTuneConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autotune.AutoTuneCollection;
import com.avaje.ebeaninternal.server.autotune.AutoTuneService;
import com.avaje.ebeaninternal.server.autotune.model.Autotune;
import com.avaje.ebeaninternal.server.autotune.model.Origin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the AutoTuneService which is comprised of profiling and query tuning.
 */
public class DefaultAutoTuneService implements AutoTuneService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAutoTuneService.class);

  private final SpiEbeanServer server;

  private final long defaultGarbageCollectionWait;

  private final boolean skipCollectionOnShutdown;

  private final BaseQueryTuner queryTuner;

  private final ProfileManager profileManager;

  private final boolean profiling;

  private final boolean queryTuning;

  private final String tuningFile;

  private final String profilingFile;

  private final String serverName;

  private final int profilingUpdateFrequency;

  public DefaultAutoTuneService(SpiEbeanServer server, ServerConfig serverConfig) {

    AutoTuneConfig config = serverConfig.getAutoTuneConfig();

    this.server = server;
    this.queryTuning = config.isQueryTuning();
    this.profiling = config.isProfiling();
    this.tuningFile = config.getQueryTuningFile();
    this.profilingFile = config.getProfilingFile();
    this.profilingUpdateFrequency = config.getProfilingUpdateFrequency();
    this.serverName = server.getName();
    this.profileManager = new ProfileManager(config, server);
    this.queryTuner = new BaseQueryTuner(config, server, profileManager);
    this.skipCollectionOnShutdown = config.isSkipCollectionOnShutdown();
    this.defaultGarbageCollectionWait = (long) config.getGarbageCollectionWait();
  }

  /**
   * Load the query tuning information from it's data store.
   */
  @Override
  public void startup() {

    if (queryTuning) {
      loadTuningFile();
      automaticProfiling();
    }
  }

  private void automaticProfiling() {

    if (isAutomaticTuningUpdate()) {
      server.getBackgroundExecutor().executePeriodically(new ProfilingUpdate(), profilingUpdateFrequency, TimeUnit.SECONDS);
    }
  }

  private boolean isAutomaticTuningUpdate() {
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
    if (!file.exists()) {
      logger.warn("AutoTune file {} not found - no initial automatic query tuning", file.getAbsolutePath());

    } else {
      AutoTuneXmlReader reader = new AutoTuneXmlReader();
      Autotune profiling = reader.read(file);
      logger.info("AutoTune loading {} tuning entries", profiling.getOrigin().size());
      for (Origin origin : profiling.getOrigin()) {
        queryTuner.load(origin);
      }
    }
  }

  private void runtimeTuningUpdate() {

    synchronized (this) {
      try {
        long start = System.currentTimeMillis();
        AutoTuneCollection profiling = profileManager.profilingCollection(false);

        ProfileCollectionEvent event = new ProfileCollectionEvent(profiling, queryTuner, false);

        if (!event.process()) {
          long exeMillis = System.currentTimeMillis() - start;
          logger.debug("No tuning updates for server:{} executionMillis:{}", serverName, exeMillis);

        } else {

          event.writeFile(profilingFile + "-" + serverName + "-tuningupdate");
          long exeMillis = System.currentTimeMillis() - start;
          logger.info("query tuning update - new:{} diff:{} for server:{} executionMillis:{}", event.getNewCount(), event.getDiffCount(), serverName, exeMillis);
        }
      } catch (Throwable e) {
        logger.error("Error collecting or applying automatic query tuning", e);
      }
    }
  }

  private void saveProfilingOnShutdown(boolean reset) {

    synchronized (this) {
      if (isAutomaticTuningUpdate()) {
        runtimeTuningUpdate();
        outputAggregateTuning();

      } else {

        AutoTuneCollection profiling = profileManager.profilingCollection(reset);
        ProfileCollectionEvent event = new ProfileCollectionEvent(profiling, queryTuner, true);

        if (!event.process()) {
          logger.info("No new or diff entries for profiling server:{}", serverName);

        } else {
          event.writeFile(profilingFile + "-" + serverName);
          logger.info("writing new:{} diff:{} profiling entries for server:{}", event.getNewCount(), event.getDiffCount(), serverName);
        }
      }
    }
  }

  private void outputAggregateTuning() {

    //TODO outputAggregateTuning()
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
    if (profiling && !skipCollectionOnShutdown) {
      collectProfiling(-1);
      saveProfilingOnShutdown(false);
    }
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
      logger.warn("Error while sleeping after System.gc() request.", e);
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
