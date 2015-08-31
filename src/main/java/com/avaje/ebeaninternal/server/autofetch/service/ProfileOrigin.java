package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.NodeUsageCollector;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.PathProperties.Props;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileOrigin implements Serializable {


  private static final long serialVersionUID = -5586783791097230766L;

  private final ObjectGraphOrigin origin;

  private final boolean queryTuningAddVersion;

  private int counter;

  private final Map<String, ProfileOriginQuery> queryStatsMap = new LinkedHashMap<String, ProfileOriginQuery>();

  private final Map<String, ProfileOriginNodeUsage> nodeUsageMap = new LinkedHashMap<String, ProfileOriginNodeUsage>();

  @SuppressWarnings("RedundantStringConstructorCall")
  private final String monitor = new String();

  public ProfileOrigin(ObjectGraphOrigin origin, boolean queryTuningAddVersion) {
    this.origin = origin;
    this.queryTuningAddVersion = queryTuningAddVersion;
  }

  public ObjectGraphOrigin getOrigin() {
    return origin;
  }

//  public TunedQueryInfo createTunedFetch(OrmQueryDetail newFetchDetail) {
//    synchronized (monitor) {
//      // NB: create a copy of queryPoint allowing garbage
//      // collection of source...
//      return new TunedQueryInfo(origin, newFetchDetail, counter);
//    }
//  }

  /**
   * Return the number of times the root query has executed.
   * <p>
   * This tells us how much profiling we have done for this query.
   * For example, after 100 times we may stop collecting more profiling info.
   * </p>
   */
  public int getCounter() {
    return counter;
  }

  /**
   * Return true if this has usage statistics.
   */
  public boolean hasUsage() {
    synchronized (monitor) {
      return !nodeUsageMap.isEmpty();
    }
  }

  public OrmQueryDetail buildTunedFetch(BeanDescriptor<?> rootDesc) {

    synchronized (monitor) {
      if (nodeUsageMap.isEmpty()) {
        return null;
      }

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
  }


  public void collectQueryInfo(ObjectGraphNode node, long beansLoaded, long micros) {

    String key = node.getPath();
    if (key == null) {
      key = "";
      // this is basically the number of times the root query
      // has executed which gives us an indication of how
      // much profiling information we have gathered.
      counter++;
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
      nodeStats.publish(profile);
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

//  public String getUsageDebug() {
//    synchronized (monitor) {
//      StringBuilder sb = new StringBuilder();
//      sb.append("root[").append(origin.getBeanType()).append("] ");
//      for (ProfileOriginNodeUsage node : nodeUsageMap.values()) {
//        sb.append(node.toString()).append("\n");
//      }
//      return sb.toString();
//    }
//  }
//
//  public String getQueryStatDebug() {
//    synchronized (monitor) {
//      StringBuilder sb = new StringBuilder();
//      for (ProfileOriginQuery queryStat : queryStatsMap.values()) {
//        sb.append(queryStat.toString()).append("\n");
//      }
//      return sb.toString();
//    }
//  }
//
//  public String toString() {
//
//    synchronized (monitor) {
//      return getUsageDebug();
//    }
//  }

}
