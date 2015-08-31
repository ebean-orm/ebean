package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.NodeUsageCollector;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.config.AutofetchConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.autofetch.ProfilingListener;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ProfileManager implements ProfilingListener {

  private static final Logger logger = LoggerFactory.getLogger(ProfileManager.class);

  private final boolean queryTuningAddVersion;

  private final boolean profiling;

  /**
   * Map of the usage and query statistics gathered.
   */
  private final Map<String, ProfileOrigin> profileMap = new ConcurrentHashMap<String, ProfileOrigin>();

  private final Object monitor = new Object();

  private final SpiEbeanServer server;

  public ProfileManager(AutofetchConfig config, SpiEbeanServer server) {
    this.server = server;
    this.profiling = config.isProfiling();
    this.queryTuningAddVersion = config.isQueryTuningAddVersion();
  }

  /**
   * Gather query execution statistics. This could either be the originating
   * query in which case the parentNode will be null, or a lazy loading query
   * resulting from traversal of the object graph.
   */
  public void collectQueryInfo(ObjectGraphNode node, long beans, long micros) {

    if (node != null) {
      ObjectGraphOrigin origin = node.getOriginQueryPoint();
      if (origin != null) {
        ProfileOrigin stats = getProfileOrigin(origin);
        stats.collectQueryInfo(node, beans, micros);
      }
    }
  }

  /**
   * Collect usage statistics from a node in the object graph.
   * <p>
   * This is sent to use from a EntityBeanIntercept when the finalise method
   * is called on the bean.
   * </p>
   */
  public void collectNodeUsage(NodeUsageCollector usageCollector) {

    ProfileOrigin profileOrigin = getProfileOrigin(usageCollector.getNode().getOriginQueryPoint());
    profileOrigin.collectUsageInfo(usageCollector);
  }

  private ProfileOrigin getProfileOrigin(ObjectGraphOrigin originQueryPoint) {
    synchronized (monitor) {
      ProfileOrigin stats = profileMap.get(originQueryPoint.getKey());
      if (stats == null) {
        stats = new ProfileOrigin(originQueryPoint, queryTuningAddVersion);
        profileMap.put(originQueryPoint.getKey(), stats);
      }
      return stats;
    }
  }


  /**
   * Update the tuned fetch plans from the current usage information.
   */
  public void updateTunedQueryInfo() {

    if (!profiling) {
      // we are not collecting any profiling information at
      // the moment so don't try updating the tuned query plans.
      return;// "Not profiling";
    }

    synchronized (monitor) {

      for (ProfileOrigin origin : profileMap.values()) {
        if (origin.hasUsage()) {
          OrmQueryDetail ormQueryDetail = updateTunedQueryFromUsage(origin);

        }
      }
    }
  }


  private OrmQueryDetail updateTunedQueryFromUsage(ProfileOrigin statistics) {

    BeanDescriptor<?> desc = server.getBeanDescriptorById(statistics.getOrigin().getBeanType());
    return desc == null ? null : statistics.buildTunedFetch(desc);
  }
}
