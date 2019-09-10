package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

public class TestTransient extends BaseTestCase {

  @Test
  public void testTransient() {

    ResetBasicData.reset();

    Customer cnew = new Customer();
    cnew.setName("testTrans");

    Ebean.save(cnew);
    Integer custId = cnew.getId();

    Customer c = Ebean.find(Customer.class).setAutoTune(false).setId(custId).findOne();

    Assert.assertNotNull(c);

    BeanState beanState = Ebean.getBeanState(c);
    Assert.assertFalse("not new or dirty as transient", beanState.isNewOrDirty());

    c.getLock().tryLock();
    try {
      c.setSelected(Boolean.TRUE);
    } finally {
      c.getLock().unlock();
    }

    Boolean selected = c.getSelected();
    Assert.assertNotNull(selected);

    Assert.assertFalse("not new or dirty as transient", beanState.isNewOrDirty());

    Ebean.save(c);

    selected = c.getSelected();
    Assert.assertNotNull(selected);

    c.setName("Modified");
    Assert.assertTrue("dirty now", beanState.isNewOrDirty());

    selected = c.getSelected();
    Assert.assertNotNull(selected);

    Ebean.save(c);
    Assert.assertFalse("Not dirty after save", beanState.isNewOrDirty());

    selected = c.getSelected();
    Assert.assertNotNull(selected);

    String updateStmt = "update customer set name = 'testTrans2' where id = :id";
    int rows = Ebean.createUpdate(Customer.class, updateStmt).set("id", custId).execute();

    Assert.assertTrue("changed name back", 1 == rows);

    // cleanup
    Ebean.delete(Customer.class, custId);
  }
}
