package io.ebeanservice.docstore.api;

import io.ebean.plugin.SpiServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Creates the integration components for DocStore integration.
 */
public interface DocStoreFactory {

  /**
   * Create and return the DocStore integration components.
   */
  DocStoreIntegration create(SpiServer server);

  /**
   * Create the doc store specific adapter for the given bean type.
   */
  <T> DocStoreBeanAdapter<T> createAdapter(BeanDescriptor<T> desc, DeployBeanDescriptor<T> deploy);

}
