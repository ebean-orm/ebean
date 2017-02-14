package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPersistListener;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Manages the assignment/registration of BeanPersistListener with their
 * respective DeployBeanDescriptor's.
 */
public class PersistListenerManager {

  private static final Logger logger = LoggerFactory.getLogger(PersistListenerManager.class);

  private final List<BeanPersistListener> list;

  public PersistListenerManager(BootupClasses bootupClasses) {
    list = bootupClasses.getBeanPersistListeners();
  }

  public int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  public <T> void addPersistListeners(DeployBeanDescriptor<T> deployDesc) {

    for (BeanPersistListener listener : list) {
      if (listener.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanPersistListener on[{}] {}", deployDesc.getFullName(), listener.getClass().getName());
        deployDesc.addPersistListener(listener);
      }
    }
  }

}
