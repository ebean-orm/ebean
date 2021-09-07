package org.tests.batchinsert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBatchSaveWithGetBeanId extends BaseTestCase {

  /**
   * Making this transaction with batchSize means that the insert
   * below does not occur immediately ... and the getBeanId()
   * should invoke the flush (and hence trigger the insert).
   */
  @Transactional(batchSize = 10)
  @Test
  @IgnorePlatform(Platform.HANA) // HANA doesn't support insert batching
  public void test() {

    Database server = DB.getDefault();
    Customer model = new Customer();
    model.setName("foo");

    server.insert(model);

    // should invoke a flush which then means the
    // insert occurs and the bean has an Id value
    Object beanId = server.beanId(model);
    assertNotNull(beanId);

    DB.delete(Customer.class, beanId);
  }
}
