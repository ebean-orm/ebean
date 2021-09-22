package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPersistController;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.util.List;

/**
 * Default implementation for creating BeanControllers.
 */
final class PersistControllerManager {

  private final List<BeanPersistController> list;

  PersistControllerManager(BootupClasses bootupClasses) {
    list = bootupClasses.getBeanPersistControllers();
  }

  int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  void addPersistControllers(DeployBeanDescriptor<?> deployDesc) {
    for (BeanPersistController c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        CoreLog.log.debug("BeanPersistController on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addPersistController(c);
      }
    }
  }

}
