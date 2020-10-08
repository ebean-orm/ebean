package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPersistController;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default implementation for creating BeanControllers.
 */
class PersistControllerManager {

  private static final Logger logger = LoggerFactory.getLogger(PersistControllerManager.class);

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
        logger.debug("BeanPersistController on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addPersistController(c);
      }
    }
  }

}
