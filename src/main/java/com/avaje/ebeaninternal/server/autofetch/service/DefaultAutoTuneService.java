package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.config.AutoTuneConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autofetch.AutoTuneCollection;
import com.avaje.ebeaninternal.server.autofetch.AutoTuneService;
import com.avaje.ebeaninternal.server.autofetch.model.Autotune;
import com.avaje.ebeaninternal.server.autofetch.model.Origin;
import com.avaje.ebeaninternal.server.autofetch.model.ProfileDiff;
import com.avaje.ebeaninternal.server.autofetch.model.ProfileNew;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetailParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Implementation of the AutoTuneService which is comprised of profiling and query tuning.
 */
public class DefaultAutoTuneService implements AutoTuneService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAutoTuneService.class);

  private final long defaultGarbageCollectionWait;

  private final boolean skipCollectionOnShutdown;

  private final BaseQueryTuner queryTuner;

  private final ProfileManager profileManager;

  private final boolean profiling;

  public DefaultAutoTuneService(SpiEbeanServer server, ServerConfig serverConfig) {

    AutoTuneConfig config = serverConfig.getAutoTuneConfig();

    this.profiling = config.isProfiling();
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

    File file = new File("ebean-autotune.xml");
    AutoTuneXmlReader reader = new AutoTuneXmlReader();
    Autotune profiling = reader.read(file);
    for (Origin origin : profiling.getOrigin()) {
      queryTuner.load(origin.getKey(), createTunedQueryInfo(origin));
    }
  }

  @NotNull
  private TunedQueryInfo createTunedQueryInfo(Origin origin) {
    OrmQueryDetail detail = new OrmQueryDetailParser(origin.getDetail()).parse();
    return new TunedQueryInfo(detail);
  }

  private void saveProfiling(boolean reset) {

    Autotune document = new Autotune();

    AutoTuneCollection autoTuneCollection = profileManager.profilingCollection(reset);

    List<AutoTuneCollection.Entry> entries = autoTuneCollection.getEntries();
    for (AutoTuneCollection.Entry entry : entries) {
      saveProfilingEntry(document, entry);
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
    String now = df.format(new Date());

    File file = new File("ebean-autotune-profiling"+"-"+now+".xml");
    AutoTuneXmlWriter writer = new AutoTuneXmlWriter();
    writer.write(document, file);
  }

  private void saveProfilingEntry(Autotune document, AutoTuneCollection.Entry entry) {

    ObjectGraphOrigin point = entry.getOrigin();
    OrmQueryDetail profileDetail = entry.getDetail();

    // compare with the existing query tuning entry
    OrmQueryDetail tuneDetail = queryTuner.get(point.getKey());
    if (tuneDetail == null) {
      // New entry
      ProfileNew profileNew = document.getProfileNew();
      if (profileNew == null) {
        profileNew = new ProfileNew();
        document.setProfileNew(profileNew);
      }
      Origin origin = createOrigin(entry, point);
      origin.setOriginal(entry.getOriginalQuery());
      profileNew.getOrigin().add(origin);

    } else if (!tuneDetail.isAutoTuneEqual(profileDetail)) {
      // Diff entry
      Origin origin = createOrigin(entry, point);
      origin.setOriginal(tuneDetail.toString());
      ProfileDiff diff = document.getProfileDiff();
      if (diff == null) {
        diff = new ProfileDiff();
        document.setProfileDiff(diff);
      }
      diff.getOrigin().add(origin);
    }
  }

  /**
   * Create the XML Origin bean for the given entry and ObjectGraphOrigin.
   */
  @NotNull
  private Origin createOrigin(AutoTuneCollection.Entry entry, ObjectGraphOrigin point) {
    Origin origin = new Origin();
    origin.setKey(point.getKey());
    origin.setBeanType(point.getBeanType());
    origin.setDetail(entry.getDetail().toString());
    origin.setCallStack(point.getCallStack().description("\n"));
    return origin;
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
      saveProfiling(false);
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
