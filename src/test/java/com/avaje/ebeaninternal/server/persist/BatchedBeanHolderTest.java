package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.api.TDSpiEbeanServer;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanManager;
import com.avaje.tests.model.basic.Customer;
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
    PersistRequestBean req1 = new PersistRequestBean(server, customer, null, beanManager, null, null, PersistRequest.Type.INSERT, false);


    int size = holder.append(req1);
    assertEquals(1, size);

    PersistRequestBean req2 = new PersistRequestBean(server, customer, null, beanManager, null, null, PersistRequest.Type.INSERT, false);
    size = holder.append(req2);
    assertEquals(0, size);

  }
}