package com.avaje.ebeanservice.docstore.none;

import com.avaje.ebean.DocumentStore;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeanservice.docstore.api.DocStoreBeanAdapter;
import com.avaje.ebeanservice.docstore.api.DocStoreFactory;
import com.avaje.ebeanservice.docstore.api.DocStoreIntegration;
import com.avaje.ebeanservice.docstore.api.DocStoreUpdateProcessor;

/**
 * A stub implementation of DocStoreFactory that will barf if the docStore features are used.
 */
public class NoneDocStoreFactory implements DocStoreFactory {

  @Override
  public DocStoreIntegration create(SpiServer server) {
    return new NoneIntegration();
  }

  @Override
  public <T> DocStoreBeanAdapter<T> createAdapter(BeanDescriptor<T> desc, DeployBeanDescriptor<T> deploy) {
    return new NoneDocStoreBeanAdapter<>(desc, deploy);
  }

  static class NoneIntegration implements DocStoreIntegration {

    @Override
    public DocStoreUpdateProcessor updateProcessor() {
      return new NoneDocStoreUpdateProcessor();
    }

    @Override
    public DocumentStore documentStore() {
      return new NoneDocStore();
    }
  }
}
