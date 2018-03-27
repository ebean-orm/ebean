package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.NodeUsageCollector;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.el.ElPropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects usages statistics for a given node in the object graph.
 */
public class ProfileOriginNodeUsage {

  private static final Logger logger = LoggerFactory.getLogger(ProfileOriginNodeUsage.class);

  private final Object monitor = new Object();

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

    synchronized (monitor) {

      BeanDescriptor<?> desc = rootDesc;
      if (path != null) {
        ElPropertyValue elGetValue = rootDesc.getElGetValue(path);
        if (elGetValue == null) {
          logger.warn("AutoTune: Can't find join for path[" + path + "] for " + rootDesc.getName());
          return;
        } else {
          BeanProperty beanProperty = elGetValue.getBeanProperty();
          if (beanProperty instanceof BeanPropertyAssoc<?>) {
            desc = ((BeanPropertyAssoc<?>) beanProperty).getTargetDescriptor();
          }
        }
      }

      for (String propName : aggregateUsed) {
        BeanProperty beanProp = desc.findPropertyFromPath(propName);
        if (beanProp == null) {
          logger.warn("AutoTune: Can't find property[" + propName + "] for " + desc.getName());

        } else {
          if (beanProp instanceof BeanPropertyAssoc<?>) {
            BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) beanProp;
            String targetIdProp = assocProp.getTargetIdProperty();
            String manyPath = SplitName.add(path, assocProp.getName());
            pathProps.addToPath(manyPath, targetIdProp);
          } else {
            //noinspection StatementWithEmptyBody
            if (beanProp.isLob() && !beanProp.isFetchEager()) {
              // AutoTune will not include Lob's marked FetchLazy
              // (which is the default for Lob's so typical).
            } else {
              pathProps.addToPath(path, beanProp.getName());
            }
          }
        }
      }

      if ((modified || addVersionProperty) && desc != null) {
        BeanProperty versionProp = desc.getVersionProperty();
        if (versionProp != null) {
          pathProps.addToPath(path, versionProp.getName());
        }
      }
    }
  }

  /**
   * Collect usage from a node.
   */
  protected void collectUsageInfo(NodeUsageCollector profile) {

    synchronized (monitor) {

      Set<String> used = profile.getUsed();

      profileCount++;
      if (!used.isEmpty()) {
        profileUsedCount++;
        aggregateUsed.addAll(used);
      }
      if (profile.isModified()) {
        modified = true;
      }
    }
  }

  @Override
  public String toString() {
    return "path[" + path + "] profileCount[" + profileCount + "] used[" + profileUsedCount + "] props" + aggregateUsed;
  }
}
