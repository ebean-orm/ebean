package io.ebeanservice.docstore.none;

import io.ebean.DocumentStore;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import io.ebeanservice.docstore.api.DocStoreFactory;
import io.ebeanservice.docstore.api.DocStoreIntegration;
import io.ebeanservice.docstore.api.DocStoreUpdateProcessor;

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
