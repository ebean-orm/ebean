package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    DB.save(master);

    assertNotNull(child.getId());

    OtoChild child2 = DB.find(OtoChild.class, child.getId());
    OtoMaster master2 = child2.getMaster();
    assertNotNull(master2);
  }

}
