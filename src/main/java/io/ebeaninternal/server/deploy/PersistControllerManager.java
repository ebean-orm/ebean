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
public class PersistControllerManager {

  private static final Logger logger = LoggerFactory.getLogger(PersistControllerManager.class);

  private final List<BeanPersistController> list;

  public PersistControllerManager(BootupClasses bootupClasses) {

    list = bootupClasses.getBeanPersistControllers();
  }

  public int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  public void addPersistControllers(DeployBeanDescriptor<?> deployDesc) {

    for (BeanPersistController c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanPersistController on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addPersistController(c);
      }
    }
  }

}
