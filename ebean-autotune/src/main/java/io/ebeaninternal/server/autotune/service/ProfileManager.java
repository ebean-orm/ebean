package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.NodeUsageCollector;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebean.config.AutoTuneConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the collection of object graph usage profiling.
 */
public class ProfileManager implements ProfilingListener {

  private final ReentrantLock lock = new ReentrantLock();

  private final boolean queryTuningAddVersion;

  /**
   * Converted from a 0-100 int to a double. Effectively a percentage rate at
   * which to collect profiling information.
   */
  private final double profilingRate;

  private final int profilingBase;

  /**
   * Map of the usage and query statistics gathered.
   */
  private final Map<String, ProfileOrigin> profileMap = new ConcurrentHashMap<>();

  private final SpiEbeanServer server;

  public ProfileManager(AutoTuneConfig config, SpiEbeanServer server) {
    this.server = server;
    this.profilingRate = config.getProfilingRate();
    this.profilingBase = config.getProfilingBase();
    this.queryTuningAddVersion = config.isQueryTuningAddVersion();
  }

  @Override
  public boolean isProfileRequest(ObjectGraphNode origin, SpiQuery<?> query) {
    ProfileOrigin profileOrigin = profileMap.get(origin.origin().key());
    if (profileOrigin == null) {
      profileMap.put(origin.origin().key(), createProfileOrigin(origin, query));
      return true;
    } else {
      return profileOrigin.isProfile();
    }
  }

  /**
   * Create the profile origin noting the query detail currently being used.
   * <p>
   * For new profiling entries it is useful to compare the profiling against the current
   * query detail that is specified in the code (as the query might already be manually optimised).
   */
  private ProfileOrigin createProfileOrigin(ObjectGraphNode origin, SpiQuery<?> query) {
    ProfileOrigin profileOrigin = new ProfileOrigin(origin.origin(), queryTuningAddVersion, profilingBase, profilingRate);
    // set the current query detail (fetch group) so that we can compare against profiling for new entries
    profileOrigin.setOriginalQuery(query.getDetail().asString());
    return profileOrigin;
  }

  /**
   * Gather query execution statistics. This could either be the originating
   * query in which case the parentNode will be null, or a lazy loading query
   * resulting from traversal of the object graph.
   */
  @Override
  public void collectQueryInfo(ObjectGraphNode node, long beans, long micros) {
    if (node != null) {
      ObjectGraphOrigin origin = node.origin();
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
   */
  @Override
  public void collectNodeUsage(NodeUsageCollector.State usageCollector) {
    ProfileOrigin profileOrigin = getProfileOrigin(usageCollector.node().origin());
    profileOrigin.collectUsageInfo(usageCollector);
  }

  private ProfileOrigin getProfileOrigin(ObjectGraphOrigin originQueryPoint) {
    lock.lock();
    try {
      return profileMap.computeIfAbsent(originQueryPoint.key(), k -> new ProfileOrigin(originQueryPoint, queryTuningAddVersion, profilingBase, profilingRate));
    } finally {
      lock.unlock();
    }
  }

  /**
   * Collect all the profiling information.
   */
  public AutoTuneCollection profilingCollection(boolean reset) {
    AutoTuneCollection req = new AutoTuneCollection();
    for (ProfileOrigin origin : profileMap.values()) {
      BeanDescriptor<?> desc = server.descriptorById(origin.getOrigin().beanType());
      if (desc != null) {
        origin.profilingCollection(desc, req, reset);
      }
    }
    return req;
  }

}
