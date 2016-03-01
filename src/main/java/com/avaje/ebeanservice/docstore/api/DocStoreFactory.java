package com.avaje.ebeanservice.docstore.api;

import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

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
