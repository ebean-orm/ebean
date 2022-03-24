package org.tests.model.onetoone;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Test
  public void testSaveCascadeWithOneToOne() {
    OtoMasterVersion master = new OtoMasterVersion();
    master.setName("m1");

    OtoChildVersion child = new OtoChildVersion();
    child.setName("c1");

    master.setChild(child);
    DB.save(master);

    assertThat(master.getVersion()).isEqualTo(1);
    assertThat(child.getVersion()).isEqualTo(1);

    child.setName("c2");
    DB.save(master);

    assertThat(master.getVersion()).isEqualTo(1);
    assertThat(child.getVersion()).isEqualTo(2);

    try (Transaction txn = DB.beginTransaction()) {
      master = DB.find(OtoMasterVersion.class).findOne();

      master.setName("m2");
      DB.save(master);
      child = DB.find(OtoChildVersion.class).findOne();
      assertThat(child.getVersion()).isEqualTo(2);
      child.setName("c3");
      DB.save(child);

      txn.commit();
    }
  }

}
