package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPostLoad;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default implementation for creating BeanControllers.
 */
final class PostLoadManager {

  private static final Logger logger = LoggerFactory.getLogger(PostLoadManager.class);

  private final List<BeanPostLoad> list;

  PostLoadManager(BootupClasses bootupClasses) {
    this.list = bootupClasses.getBeanPostLoaders();
  }

  int getRegisterCount() {
    return list.size();
  }

  /**
   * Register BeanPostLoad listeners for a given entity type.
   */
  void addPostLoad(DeployBeanDescriptor<?> deployDesc) {
    for (BeanPostLoad c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanPostLoad on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addPostLoad(c);
      }
    }
  }

}
