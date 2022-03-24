package io.ebean.xtest.docstore;


import io.ebean.BaseTestCase;
import io.ebean.Query;
import io.ebean.docstore.DocUpdateContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.support.DocStoreBeanBaseAdapter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class DocStoreBeanBaseAdapterTest extends BaseTestCase {


  @Test
  @SuppressWarnings("unchecked")
  public void test_basic_construction() throws Exception {

    SpiEbeanServer server = spiEbeanServer();
    BeanDescriptor<Order> orderDesc = server.descriptor(Order.class);

    DeployBeanDescriptor<Order> deployDesc = (DeployBeanDescriptor<Order>) mock(DeployBeanDescriptor.class);

    TDAdapter<Order> adapter = new TDAdapter<>(orderDesc, deployDesc);

    Assertions.assertThat(adapter.indexName()).isEqualTo("order");
    Assertions.assertThat(adapter.indexType()).isEqualTo("order");
    Assertions.assertThat(adapter.getQueueId()).isEqualTo("order");
  }

  static class TDAdapter<T> extends DocStoreBeanBaseAdapter<T> {

    TDAdapter(BeanDescriptor<T> desc, DeployBeanDescriptor<T> deploy) {
      super(desc, deploy);
    }

    @Override
    public void applyPath(Query<T> query) {
    }

    @Override
    public void deleteById(Object idValue, DocUpdateContext txn) throws IOException {
    }

    @Override
    public void index(Object idValue, Object entityBean, DocUpdateContext txn) throws IOException {
    }

    @Override
    public void insert(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException {
    }

    @Override
    public void update(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException {
    }

    @Override
    public void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocUpdateContext txn) throws IOException {
    }
  }
}
