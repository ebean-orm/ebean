package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

public class TestPrivateOwnedCascadeOrder extends BaseTestCase {

  @Test
  public void test() {

    // setup
    TSMaster master = new TSMaster();
    master.getDetails().add(new TSDetail("c97", "ONE"));
    master.getDetails().add(new TSDetail("c96", "TWO"));

    Ebean.save(master);

    // act
    TSMaster master1 = Ebean.find(master.getClass(), master.getId());

    // Check Ebean deletes the existing c97 first as the unique values clash
    master1.getDetails().clear();
    master1.getDetails().add(new TSDetail("c98", "TWO"));

    Ebean.save(master1);
  }

  @Test
  public void testWithTransaction() {

    // setup
    TSMaster master = new TSMaster();
    master.getDetails().add(new TSDetail("d97", "ONE2"));
    master.getDetails().add(new TSDetail("d96", "TWO2"));

    Ebean.save(master);

    // act
    TSMaster master1 = Ebean.find(master.getClass(), master.getId());

    // Check Ebean deletes the existing d96 first as the unique values clash
    Transaction transaction = Ebean.beginTransaction();
    try {
      master1.getDetails().clear();
      master1.getDetails().add(new TSDetail("d98", "TWO2"));

      Ebean.save(master1);
      transaction.commit();
    } finally {
      transaction.end();
    }

    // Cleanup
    Ebean.find(TSDetail.class).where().or().eq("name", "d97").eq("name", "d98").delete();
    Ebean.delete(master1);
  }
}
