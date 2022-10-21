package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanFindController;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Default implementation for BeanFinderFactory.
 */
final class BeanFinderManager {

  private static final System.Logger log = CoreLog.internal;

  private final List<BeanFindController> list;

  BeanFinderManager(BootupClasses bootupClasses) {
    list = bootupClasses.getBeanFindControllers();
  }

  int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  void addFindControllers(DeployBeanDescriptor<?> deployDesc) {
    for (BeanFindController c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        log.log(DEBUG, "BeanFindController on[{0}] {1}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.setBeanFinder(c);
      }
    }
  }

}
