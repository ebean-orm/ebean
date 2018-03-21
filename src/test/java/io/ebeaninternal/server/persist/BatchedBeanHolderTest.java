package io.ebeaninternal.server.persist;

import io.ebean.BaseTestCase;
import io.ebeaninternal.api.TDSpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanManager;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

public class BatchedBeanHolderTest extends BaseTestCase {

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testAppend() {

    TDSpiEbeanServer server = new TDSpiEbeanServer("foo");

    BeanDescriptor beanDescriptor = getBeanDescriptor(Customer.class);

    //BeanDescriptor beanDescriptor = Mockito.mock(BeanDescriptor.class);
    BeanManager beanManager = new BeanManager(beanDescriptor, null);


    BatchedBeanHolder holder = new BatchedBeanHolder(null, beanDescriptor, 1);

    Customer customer = new Customer();
    customer.setUpdtime(new Timestamp(System.currentTimeMillis()));
    PersistRequestBean req1 = new PersistRequestBean(server, customer, null, beanManager, null, null, PersistRequest.Type.INSERT, 0);


    int size = holder.append(req1);
    assertEquals(1, size);

    PersistRequestBean req2 = new PersistRequestBean(server, customer, null, beanManager, null, null, PersistRequest.Type.INSERT, 0);
    size = holder.append(req2);
    assertEquals(0, size);

  }
}
