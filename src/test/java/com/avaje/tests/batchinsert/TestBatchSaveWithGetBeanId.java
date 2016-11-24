package com.avaje.tests.batchinsert;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

public class TestBatchSaveWithGetBeanId extends BaseTestCase {

  /**
   * Making this transaction with batchSize means that the insert
   * below does not occur immediately ... and the getBeanId()
   * should invoke the flush (and hence trigger the insert).
   */
  @Transactional(batchSize = 10)
  @Test
  public void test() {
    assumeFalse("Not yet supported for MSSQL.", isMsSqlServer());
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
