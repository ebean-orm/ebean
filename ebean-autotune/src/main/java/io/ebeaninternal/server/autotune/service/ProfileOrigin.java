package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.NodeUsageCollector;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebean.text.PathProperties;
import io.ebean.text.PathProperties.Props;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class ProfileOrigin {

  private final ReentrantLock lock = new ReentrantLock();

  private static final long RESET_COUNT = -1000000000L;

  private final ObjectGraphOrigin origin;

  private final boolean queryTuningAddVersion;

  private final int profilingBase;

  private final double profilingRate;

  private final Map<String, ProfileOriginQuery> queryStatsMap = new ConcurrentHashMap<>();

  private final Map<String, ProfileOriginNodeUsage> nodeUsageMap = new ConcurrentHashMap<>();

  private final AtomicLong requestCount = new AtomicLong();

  private final AtomicLong profileCount = new AtomicLong();

  private String originalQuery;

  public ProfileOrigin(ObjectGraphOrigin origin, boolean queryTuningAddVersion, int profilingBase, double profilingRate) {
    this.origin = origin;
    this.queryTuningAddVersion = queryTuningAddVersion;
    this.profilingBase = profilingBase;
    this.profilingRate = profilingRate;
  }

  public String getOriginalQuery() {
    return originalQuery;
  }

  public void setOriginalQuery(String originalQuery) {
    this.originalQuery = originalQuery;
  }

  /**
   * Return true if this query should be profiled based on a percentage rate.
   */
  public boolean isProfile() {

    long count = requestCount.incrementAndGet();
    if (count < profilingBase) {
      return true;
    }
    long hits = profileCount.get();
    if (profilingRate > (double) hits / count) {
      profileCount.incrementAndGet();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Collect profiling information with the option to reset the underlying profiling detail.
   */
  public void profilingCollection(BeanDescriptor<?> rootDesc, AutoTuneCollection req, boolean reset) {
    lock.lock();
    try {
      if (nodeUsageMap.isEmpty()) {
        return;
      }

      OrmQueryDetail detail = buildDetail(rootDesc);
      AutoTuneCollection.Entry entry = req.add(origin, detail, originalQuery);

      Collection<ProfileOriginQuery> values = queryStatsMap.values();
      for (ProfileOriginQuery queryEntry : values) {
        entry.addQuery(queryEntry.createEntryQuery(reset));
      }
      if (reset) {
        nodeUsageMap.clear();
        if (requestCount.get() > RESET_COUNT) {
          requestCount.set(profilingBase);
          profileCount.set(0);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  OrmQueryDetail buildDetail(BeanDescriptor<?> rootDesc) {

    PathProperties pathProps = new PathProperties();
    for (ProfileOriginNodeUsage statsNode : nodeUsageMap.values()) {
      statsNode.buildTunedFetch(pathProps, rootDesc, queryTuningAddVersion);
    }

    OrmQueryDetail detail = new OrmQueryDetail();

    for (Props props : pathProps.getPathProps()) {
      if (!props.isEmpty()) {
        detail.fetch(props.getPath(), props.getPropertiesAsString(), null);
      }
    }

    detail.sortFetchPaths(rootDesc);
    return detail;
  }

  /**
   * Return the origin.
   */
  public ObjectGraphOrigin getOrigin() {
    return origin;
  }

  /**
   * Collect query execution summary statistics.
   * <p>
   * This can give us a quick overview into bad lazy loading areas etc.
   * </p>
   */
  public void collectQueryInfo(ObjectGraphNode node, long beansLoaded, long micros) {

    String key = node.path();
    if (key == null) {
      key = "";
    }

    ProfileOriginQuery stats = queryStatsMap.get(key);
    if (stats == null) {
      // a race condition but we don't care
      stats = new ProfileOriginQuery(key);
      queryStatsMap.put(key, stats);
    }
    stats.add(beansLoaded, micros);
  }

  /**
   * Collect the usage information for from a instance for this node.
   */
  public void collectUsageInfo(NodeUsageCollector.State profile) {
    if (!profile.isEmpty()) {
      getNodeStats(profile.node().path()).collectUsageInfo(profile);
    }
  }

  private ProfileOriginNodeUsage getNodeStats(String path) {
    lock.lock();
    try {
      // handle null paths as using ConcurrentHashMap
      path = (path == null) ? "" : path;
      ProfileOriginNodeUsage nodeStats = nodeUsageMap.get(path);
      if (nodeStats == null) {
        nodeStats = new ProfileOriginNodeUsage(path);
        nodeUsageMap.put(path, nodeStats);
      }
      return nodeStats;
    } finally {
      lock.unlock();
    }
  }

}
