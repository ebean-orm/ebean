package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
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

    for (int i = 0; i < list.size(); i++) {
      BeanQueryAdapter c = list.get(i);
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanQueryAdapter on[" + deployDesc.getFullName() + "] " + c.getClass().getName());
        deployDesc.addQueryAdapter(c);
      }
    }
  }

}
