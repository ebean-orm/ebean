package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanQueryAdapter;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.util.List;

/**
 * Default implementation for creating BeanControllers.
 */
final class BeanQueryAdapterManager {

  private final List<BeanQueryAdapter> list;

  BeanQueryAdapterManager(BootupClasses bootupClasses) {
    this.list = bootupClasses.getBeanQueryAdapters();
  }

  int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  void addQueryAdapter(DeployBeanDescriptor<?> deployDesc) {
    for (BeanQueryAdapter c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        CoreLog.internal.debug("BeanQueryAdapter on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addQueryAdapter(c);
      }
    }
  }

}
