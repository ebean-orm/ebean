package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPostConstructListener;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default implementation for creating BeanControllers.
 */
public class PostConstructManager {

  private static final Logger logger = LoggerFactory.getLogger(PostConstructManager.class);

  private final List<BeanPostConstructListener> list;

  public PostConstructManager(BootupClasses bootupClasses) {
    this.list = bootupClasses.getBeanPostConstructoListeners();
  }

  public int getRegisterCount() {
    return list.size();
  }

  /**
   * Register BeanPostLoad listeners for a given entity type.
   */
  public void addPostConstructListeners(DeployBeanDescriptor<?> deployDesc) {

    for (BeanPostConstructListener c : list) {
      if (c.isRegisterFor(deployDesc.getBeanType())) {
        logger.debug("BeanPostLoad on[{}] {}", deployDesc.getFullName(), c.getClass().getName());
        deployDesc.addPostConstructListener(c);
      }
    }
  }

}
