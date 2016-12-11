package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;

public class TestOneToOneCascadeSave extends BaseTestCase {

  @Test
  public void test() {


    OtoMaster master = new OtoMaster();
    master.setName("CName");

    OtoChild child = new OtoChild();
    child.setName("OName");

    master.setChild(child);
    // The parent customer object should be automatically set onto the child
    // object if it is currently null so you don't need to do the extra
    // o.setCustomer(c);

    Ebean.save(master);

    Assert.assertNotNull(child.getId());

    OtoChild child2 = Ebean.find(OtoChild.class, child.getId());
    OtoMaster master2 = child2.getMaster();
    Assert.assertNotNull(master2);

  }

}
