package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanFindController;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default implementation for BeanFinderFactory.
 */
public class BeanFinderManager {

  final Logger logger = LoggerFactory.getLogger(BeanFinderManager.class);

  private final List<BeanFindController> list;

  public BeanFinderManager(BootupClasses bootupClasses) {
    list = bootupClasses.getBeanFindControllers();
  }

  public int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  public void addFindControllers(DeployBeanDescriptor<?> deployDesc) {

    for (BeanFindController c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanFindController on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.setBeanFinder(c);
      }
    }
  }

}
