package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.NodeUsageCollector;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.PathProperties.Props;
import com.avaje.ebeaninternal.server.autofetch.AutoTuneCollection;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProfileOrigin {

  private static final long RESET_COUNT = -1000000000L;

  private final ObjectGraphOrigin origin;

  private final boolean queryTuningAddVersion;

  private final int profilingBase;

  private final double profilingRate;

  private final Map<String, ProfileOriginQuery> queryStatsMap = new ConcurrentHashMap<String, ProfileOriginQuery>();

  private final Map<String, ProfileOriginNodeUsage> nodeUsageMap = new ConcurrentHashMap<String, ProfileOriginNodeUsage>();

  private final Object monitor = new Object();

  private final AtomicLong requestCount = new AtomicLong();

  private final AtomicLong profileCount = new AtomicLong();

  public ProfileOrigin(ObjectGraphOrigin origin, boolean queryTuningAddVersion, int profilingBase, double profilingRate) {
    this.origin = origin;
    this.queryTuningAddVersion = queryTuningAddVersion;
    this.profilingBase = profilingBase;
    this.profilingRate = profilingRate;
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

    synchronized (monitor) {
      if (nodeUsageMap.isEmpty()) {
        return;
      }

      OrmQueryDetail detail = buildDetail(rootDesc);
      AutoTuneCollection.Entry entry = req.add(origin, detail);

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
    }
  }

  private OrmQueryDetail buildDetail(BeanDescriptor<?> rootDesc) {
    PathProperties pathProps = new PathProperties();

    for (ProfileOriginNodeUsage statsNode : nodeUsageMap.values()) {
      statsNode.buildTunedFetch(pathProps, rootDesc);
    }

    OrmQueryDetail detail = new OrmQueryDetail();

    Collection<Props> pathProperties = pathProps.getPathProps();
    for (Props props : pathProperties) {
      if (!props.isEmpty()) {
        detail.addFetch(props.getPath(), props.getPropertiesAsString(), null);
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

    String key = node.getPath();
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
  public void collectUsageInfo(NodeUsageCollector profile) {

    if (!profile.isEmpty()) {
      ObjectGraphNode node = profile.getNode();

      ProfileOriginNodeUsage nodeStats = getNodeStats(node.getPath());
      nodeStats.collectUsageInfo(profile);
    }
  }

  private ProfileOriginNodeUsage getNodeStats(String path) {

    synchronized (monitor) {
      ProfileOriginNodeUsage nodeStats = nodeUsageMap.get(path);
      if (nodeStats == null) {
        nodeStats = new ProfileOriginNodeUsage(path, queryTuningAddVersion);
        nodeUsageMap.put(path, nodeStats);
      }
      return nodeStats;
    }
  }

}
