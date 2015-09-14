package com.avaje.ebeaninternal.server.autotune.service;

import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.config.AutoTuneConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autotune.AutoTuneCollection;
import com.avaje.ebeaninternal.server.autotune.AutoTuneService;
import com.avaje.ebeaninternal.server.autotune.model.Autotune;
import com.avaje.ebeaninternal.server.autotune.model.Origin;
import com.avaje.ebeaninternal.server.autotune.model.ProfileDiff;
import com.avaje.ebeaninternal.server.autotune.model.ProfileEmpty;
import com.avaje.ebeaninternal.server.autotune.model.ProfileNew;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetailParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

  private final boolean queryTuning;

  private final String tuningFile;

  private final String profilingFile;

  private final String serverName;

  public DefaultAutoTuneService(SpiEbeanServer server, ServerConfig serverConfig) {

    AutoTuneConfig config = serverConfig.getAutoTuneConfig();

    this.queryTuning = config.isQueryTuning();
    this.profiling = config.isProfiling();
    this.tuningFile = config.getQueryTuningFile();
    this.profilingFile = config.getProfilingFile();
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
      File file = new File(tuningFile);
      if (!file.exists()) {
        logger.warn("AutoTune file {} not found - no automatic tuning will be applied", file.getAbsolutePath());

      } else {
        AutoTuneXmlReader reader = new AutoTuneXmlReader();
        Autotune profiling = reader.read(file);
        logger.info("AutoTune loading {} tuning entries", profiling.getOrigin().size());
        for (Origin origin : profiling.getOrigin()) {
          queryTuner.load(origin.getKey(), createTunedQueryInfo(origin));
        }
      }
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

    // count "new" and "diff" profiling entries
    AtomicInteger newCounter = new AtomicInteger();
    AtomicInteger diffCounter = new AtomicInteger();

    Set<String> profileKeys = new HashSet<String>();
    for (AutoTuneCollection.Entry entry : entries) {
      saveProfilingEntry(document, entry, newCounter, diffCounter);
      profileKeys.add(entry.getOrigin().getKey());
    }

    // report the origin keys that we didn't collect any profiling on
    Set<String> tunerKeys = queryTuner.keySet();
    for (String tuneKey : tunerKeys) {
      if (!profileKeys.contains(tuneKey)) {
        ProfileEmpty profileEmpty = document.getProfileEmpty();
        if (profileEmpty == null) {
          profileEmpty = new ProfileEmpty();
          document.setProfileEmpty(profileEmpty);
        }
        Origin emptyOrigin = new Origin();
        emptyOrigin.setKey(tuneKey);
        profileEmpty.getOrigin().add(emptyOrigin);
      }
    }

    int totalNew = newCounter.get();
    int totalDiff = diffCounter.get();
    if (totalNew == 0 && totalDiff == 0) {
      logger.info("No new or diff entries for profiling server:{}", serverName);

    } else {
      sortDocument(document);

      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
      String now = df.format(new Date());

      // write the file with serverName and now suffix as we can output the profiling many times
      File file = new File(profilingFile + "-" + serverName + "-" + now + ".xml");
      AutoTuneXmlWriter writer = new AutoTuneXmlWriter();
      writer.write(document, file);

      logger.info("writing new:{} diff:{} profiling entries for server:{}", totalNew, totalDiff, serverName);
    }
  }

  /**
   * Set the diff and new entries by bean type followed by key.
   */
  private void sortDocument(Autotune document) {

    ProfileDiff profileDiff = document.getProfileDiff();
    if (profileDiff != null) {
      Collections.sort(profileDiff.getOrigin(), new OriginNameKeySort());
    }
    ProfileNew profileNew = document.getProfileNew();
    if (profileNew != null) {
      Collections.sort(profileNew.getOrigin(), new OriginNameKeySort());
    }
    ProfileEmpty profileEmpty = document.getProfileEmpty();
    if (profileEmpty != null) {
      Collections.sort(profileEmpty.getOrigin(), new OriginKeySort());
    }
  }

  /**
   * Comparator sort by bean type then key.
   */
  class OriginNameKeySort implements Comparator<Origin> {

    @Override
    public int compare(Origin o1, Origin o2) {
      int comp = o1.getBeanType().compareTo(o2.getBeanType());
      if (comp == 0) {
        comp = o1.getKey().compareTo(o2.getKey());
      }
      return comp;
    }
  }

  /**
   * Comparator sort by bean type then key.
   */
  class OriginKeySort implements Comparator<Origin> {

    @Override
    public int compare(Origin o1, Origin o2) {
      return o1.getKey().compareTo(o2.getKey());
    }
  }

  private void saveProfilingEntry(Autotune document, AutoTuneCollection.Entry entry, AtomicInteger newCount, AtomicInteger diffCount) {

    ObjectGraphOrigin point = entry.getOrigin();
    OrmQueryDetail profileDetail = entry.getDetail();

    // compare with the existing query tuning entry
    OrmQueryDetail tuneDetail = queryTuner.get(point.getKey());
    if (tuneDetail == null) {
      // New entry
      newCount.incrementAndGet();
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
      diffCount.incrementAndGet();
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
