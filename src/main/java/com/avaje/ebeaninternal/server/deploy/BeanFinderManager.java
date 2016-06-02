package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebeaninternal.server.core.bootup.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
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

    for (int i = 0; i < list.size(); i++) {
      BeanFindController c = list.get(i);
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanFindController on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
        deployDesc.setBeanFinder(c);
      }
    }
  }

}
