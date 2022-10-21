package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPersistListener;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Manages the assignment/registration of BeanPersistListener with their
 * respective DeployBeanDescriptor's.
 */
final class PersistListenerManager {

  private final List<BeanPersistListener> list;

  PersistListenerManager(BootupClasses bootupClasses) {
    list = bootupClasses.getBeanPersistListeners();
  }

  int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  <T> void addPersistListeners(DeployBeanDescriptor<T> deployDesc) {
    for (BeanPersistListener listener : list) {
      if (listener.isRegisterFor(deployDesc.getBeanType())) {
        CoreLog.log.log(DEBUG, "BeanPersistListener on[{0}] {1}", deployDesc.getFullName(), listener.getClass().getName());
        deployDesc.addPersistListener(listener);
      }
    }
  }

}
