package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanQueryAdapter;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default implementation for creating BeanControllers.
 */
public class BeanQueryAdapterManager {

  private static final Logger logger = LoggerFactory.getLogger(BeanQueryAdapterManager.class);

  private final List<BeanQueryAdapter> list;

  public BeanQueryAdapterManager(BootupClasses bootupClasses) {

    list = bootupClasses.getBeanQueryAdapters();
  }

  public int getRegisterCount() {
    return list.size();
  }

  /**
   * Return the BeanPersistController for a given entity type.
   */
  public void addQueryAdapter(DeployBeanDescriptor<?> deployDesc) {

    for (BeanQueryAdapter c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanQueryAdapter on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addQueryAdapter(c);
      }
    }
  }

}
