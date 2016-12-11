package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.TDSpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanManager;
import org.tests.model.basic.Customer;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class BatchedBeanHolderTest {

  @Test
  public void testAppend() throws Exception {


    TDSpiEbeanServer server = new TDSpiEbeanServer("foo");

    BeanDescriptor beanDescriptor = Mockito.mock(BeanDescriptor.class);
    BeanManager beanManager = new BeanManager(beanDescriptor, null);


    BatchedBeanHolder holder = new BatchedBeanHolder(null, beanDescriptor, 1);

    Customer customer = new Customer();
    PersistRequestBean req1 = new PersistRequestBean(server, customer, null, beanManager, null, null, PersistRequest.Type.INSERT, false, false);


    int size = holder.append(req1);
    assertEquals(1, size);

    PersistRequestBean req2 = new PersistRequestBean(server, customer, null, beanManager, null, null, PersistRequest.Type.INSERT, false, false);
    size = holder.append(req2);
    assertEquals(0, size);

  }
}
