package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
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
}
