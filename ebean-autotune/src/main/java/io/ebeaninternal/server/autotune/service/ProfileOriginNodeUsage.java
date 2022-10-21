package io.ebeaninternal.server.autotune.service;

import io.avaje.applog.AppLog;
import io.ebean.bean.NodeUsageCollector;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.el.ElPropertyValue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Collects usages statistics for a given node in the object graph.
 */
public class ProfileOriginNodeUsage {

  private static final System.Logger logger = AppLog.getLogger(ProfileOriginNodeUsage.class);

  private final ReentrantLock lock = new ReentrantLock();

  private final String path;

  private int profileCount;

  private int profileUsedCount;

  private boolean modified;

  private final Set<String> aggregateUsed = new LinkedHashSet<>();

  public ProfileOriginNodeUsage(String path) {
    // handle null paths as using ConcurrentHashMap
    this.path = "".equals(path) ? null : path;
  }

  protected void buildTunedFetch(PathProperties pathProps, BeanDescriptor<?> rootDesc, boolean addVersionProperty) {
    lock.lock();
    try {
      BeanDescriptor<?> desc = rootDesc;
      if (path != null) {
        ElPropertyValue elGetValue = rootDesc.elGetValue(path);
        if (elGetValue == null) {
          logger.log(WARNING, "AutoTune: Can't find join for path[" + path + "] for " + rootDesc.name());
          return;
        } else {
          BeanProperty beanProperty = elGetValue.beanProperty();
          if (beanProperty instanceof BeanPropertyAssoc<?>) {
            desc = ((BeanPropertyAssoc<?>) beanProperty).targetDescriptor();
          }
        }
      }

      BeanProperty toOneIdProperty = null;
      boolean addedToPath = false;

      for (String propName : aggregateUsed) {
        BeanProperty beanProp = desc.findPropertyFromPath(propName);
        if (beanProp == null) {
          logger.log(WARNING, "AutoTune: Can't find property[" + propName + "] for " + desc.name());

        } else {
          if (beanProp.isId()) {
            // remember and maybe add ToOne property to parent path
            toOneIdProperty = beanProp;
          } else if (beanProp instanceof BeanPropertyAssoc<?>) {
            // intentionally skip
          } else {
            //noinspection StatementWithEmptyBody
            if (beanProp.isLob() && !beanProp.isFetchEager()) {
              // AutoTune will not include Lob's marked FetchLazy
              // (which is the default for Lob's so typical).
            } else {
              addedToPath = true;
              pathProps.addToPath(path, beanProp.name());
            }
          }
        }
      }

      if ((modified || addVersionProperty) && desc != null) {
        BeanProperty versionProp = desc.versionProperty();
        if (versionProp != null) {
          addedToPath = true;
          pathProps.addToPath(path, versionProp.name());
        }
      }

      if (toOneIdProperty != null && !addedToPath) {
        // add ToOne property to parent path
        ElPropertyValue assocOne = rootDesc.elGetValue(path);
        pathProps.addToPath(SplitName.parent(path), assocOne.name());
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Collect usage from a node.
   */
  protected void collectUsageInfo(NodeUsageCollector.State profile) {
    lock.lock();
    try {
      Set<String> used = profile.used();
      profileCount++;
      if (!used.isEmpty()) {
        profileUsedCount++;
        aggregateUsed.addAll(used);
      }
      if (profile.isModified()) {
        modified = true;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public String toString() {
    return "path[" + path + "] profileCount[" + profileCount + "] used[" + profileUsedCount + "] props" + aggregateUsed;
  }
}
