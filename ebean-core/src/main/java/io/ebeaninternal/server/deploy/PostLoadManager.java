package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPostLoad;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Default implementation for creating BeanControllers.
 */
final class PostLoadManager {

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
        CoreLog.log.log(DEBUG, "BeanPostLoad on[{0}] {1}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addPostLoad(c);
      }
    }
  }

}
