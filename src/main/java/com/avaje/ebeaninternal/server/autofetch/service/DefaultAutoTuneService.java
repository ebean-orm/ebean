package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.config.AutofetchConfig;
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
import java.util.List;

/**
 * Implementation of the AutoTuneService which is comprised of profiling and query tuning.
 */
public class DefaultAutoTuneService implements AutoTuneService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAutoTuneService.class);

  private final long defaultGarbageCollectionWait;

  private final boolean garbageCollectionOnShutdown;

  private final BaseQueryTuner queryTuner;

  private final ProfileManager profileManager;

  public DefaultAutoTuneService(SpiEbeanServer server, ServerConfig serverConfig) {

    AutofetchConfig config = serverConfig.getAutofetchConfig();

    this.profileManager = new ProfileManager(config, server);
    this.queryTuner = new BaseQueryTuner(config, server, profileManager);

    this.garbageCollectionOnShutdown = config.isGarbageCollectionOnShutdown();
    this.defaultGarbageCollectionWait = (long) config.getGarbageCollectionWait();
  }

  /**
   * Load the query tuning information from it's data store.
   */
  public void startup() {

    File file = new File("ebean-autotune.xml");
    AutoTuneXmlReader reader = new AutoTuneXmlReader();
    Autotune profiling = reader.read(file);
    List<Origin> originList = profiling.getOrigin();
    for (Origin origin : originList) {
      String key = origin.getKey();
      String detail = origin.getDetail();
      OrmQueryDetailParser parser = new OrmQueryDetailParser(detail);
      OrmQueryDetail fetchDetail = parser.parse();
      TunedQueryInfo tunedQueryInfo = new TunedQueryInfo(fetchDetail);
      queryTuner.load(key, tunedQueryInfo);
    }
  }

  private void saveProfiling() {

    Autotune document = new Autotune();

    AutoTuneCollection autoTuneCollection = profileManager.profilingCollection(false);

    List<AutoTuneCollection.Entry> entries = autoTuneCollection.getEntries();
    for (AutoTuneCollection.Entry entry : entries) {
      ObjectGraphOrigin point = entry.getOrigin();
      OrmQueryDetail profileDetail = entry.getDetail();

      OrmQueryDetail tuneDetail = queryTuner.get(point.getKey());
      if (tuneDetail == null) {
        ProfileNew profileNew = document.getProfileNew();
        if (profileNew == null) {
          profileNew = new ProfileNew();
          document.setProfileNew(profileNew);
        }
        profileNew.getOrigin().add( createOrigin(entry, point));

      } else if (!tuneDetail.isAutoTuneEqual(profileDetail)) {
        Origin origin1 = createOrigin(entry, point);
        origin1.setTuneDetail(tuneDetail.toString());
        ProfileDiff diff = document.getProfileDiff();
        if (diff == null) {
          diff = new ProfileDiff();
          document.setProfileDiff(diff);
        }
        diff.getOrigin().add(origin1);
      }
    }

    File file = new File("ebean-autotune-profiling.xml");
    AutoTuneXmlWriter writer = new AutoTuneXmlWriter();
    writer.write(document, file);
  }

  @NotNull
  private Origin createOrigin(AutoTuneCollection.Entry entry, ObjectGraphOrigin point) {
    Origin origin1 = new Origin();
    origin1.setKey(point.getKey());
    origin1.setBeanType(point.getBeanType());
    origin1.setDetail(entry.getDetail().toString());
    origin1.setCallStack(point.getCallStack().description("\n"));
    return origin1;
  }

  /**
   * Shutdown the listener.
   * <p>
   * We should try to collect the usage statistics by calling a System.gc().
   * This is necessary for use with short lived applications where garbage
   * collection may not otherwise occur at all.
   * </p>
   */
  public void shutdown() {
    if (garbageCollectionOnShutdown) {
      collectUsageViaGC(-1);
      saveProfiling();
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
  public void collectUsageViaGC() {
    collectUsageViaGC(-1);
  }

  public void collectUsageViaGC(long waitMillis) {
    System.gc();
    try {
      if (waitMillis < 0) {
        waitMillis = defaultGarbageCollectionWait;
      }
      Thread.sleep(waitMillis);
    } catch (InterruptedException e) {
      logger.warn("Error while sleeping after System.gc() request.", e);
    }
    updateTunedQueryInfo();
  }

  /**
   * Update the tuned fetch plans from the current usage information.
   */
  public void updateTunedQueryInfo() {

//    if (!profiling) {
//      // we are not collecting any profiling information at
//      // the moment so don't try updating the tuned query plans.
//      return "Not profiling";
//    }
//
//    synchronized (statisticsMonitor) {
//
//      Counters counters = new Counters();
//
//      for (ProfileOrigin queryPointStatistics : statisticsMap.values()) {
//        if (!queryPointStatistics.hasUsage()) {
//          // no usage statistics collected yet...
//          counters.incrementNoUsage();
//        } else {
//          updateTunedQueryFromUsage(counters, queryPointStatistics);
//        }
//      }
//
//      String summaryInfo = counters.toString();
//
//      if (counters.isInteresting()) {
//        // only log it if its interesting
//        logging.logSummary(summaryInfo);
//      }
//
//      return summaryInfo;
//    }
  }


//
//  private void updateTunedQueryFromUsage(Counters counters, ProfileOrigin statistics) {
//
//    ObjectGraphOrigin queryPoint = statistics.getOrigin();
//    String beanType = queryPoint.getBeanType();
//
//    try {
//      Class<?> beanClass = ClassUtil.forName(beanType, this.getClass());
//      BeanDescriptor<?> beanDescriptor = server.getBeanDescriptor(beanClass);
//      if (beanDescriptor != null) {
//
//        // Determine the fetch plan from the latest statistics.
//        // Use this to compare with current "tuned fetch plan".
//        OrmQueryDetail newFetchDetail = statistics.buildTunedFetch(beanDescriptor);
//
//        // get the current tuned fetch info...
//        TunedQueryInfo currentFetch = tunedQueryInfoMap.get(queryPoint.getKey());
//
//        if (currentFetch == null) {
//          // its a new fetch plan, add it.
//          counters.incrementNew();
//
//          currentFetch = statistics.createTunedFetch(newFetchDetail);
//          logging.logNew(currentFetch);
//          tunedQueryInfoMap.put(queryPoint.getKey(), currentFetch);
//
//        } else if (!currentFetch.isSame(newFetchDetail)) {
//          // the fetch plan has changed, update it.
//          counters.incrementModified();
//
//          logging.logChanged(currentFetch, newFetchDetail);
//          currentFetch.setTunedDetail(newFetchDetail);
//
//        } else {
//          // the fetch plan has not changed...
//          counters.incrementUnchanged();
//        }
//
//        currentFetch.setProfileCount(statistics.getCounter());
//      }
//
//    } catch (ClassNotFoundException e) {
//      // expected after renaming/moving an entity bean
//      String msg = e.toString() + " updating autoFetch tuned query for " + beanType
//          + ". It isLikely this bean has been renamed or moved";
//      logging.logInfo(msg, null);
//      statisticsMap.remove(statistics.getOrigin().getKey());
//    }
//  }
//

  /**
   * Auto tune the query and enable profiling.
   */
  public boolean tuneQuery(SpiQuery<?> query) {
    return queryTuner.tuneQuery(query);
  }

}
