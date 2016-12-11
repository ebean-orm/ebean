package org.tests.batchinsert;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.annotation.Transactional;
import org.tests.model.basic.Customer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestBatchSaveWithGetBeanId extends BaseTestCase {

  /**
   * Making this transaction with batchSize means that the insert
   * below does not occur immediately ... and the getBeanId()
   * should invoke the flush (and hence trigger the insert).
   */
  @Transactional(batchSize = 10)
  @Test
  public void test() {

    EbeanServer server = Ebean.getDefaultServer();
    Customer model = new Customer();
    model.setName("foo");

    server.insert(model);

    // should invoke a flush which then means the
    // insert occurs and the bean has an Id value
    Object beanId = server.getBeanId(model);
    assertNotNull(beanId);
  }
}
