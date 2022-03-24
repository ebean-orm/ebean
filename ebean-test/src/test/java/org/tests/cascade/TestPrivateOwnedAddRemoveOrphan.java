package org.tests.cascade;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPrivateOwnedAddRemoveOrphan extends BaseTestCase {

  @Test
  public void test() {

    // setup
    TSMaster master0 = new TSMaster();
    DB.save(master0);

    // act
    TSMaster master1 = DB.find(master0.getClass(), master0.getId());

    TSDetail tsDetail = new TSDetail();
    // Add then remove a bean that was never saved (to the DB)
    master1.getDetails().add(tsDetail);
    master1.getDetails().remove(tsDetail);

    DB.save(master1);

    TSMaster master2 = DB.find(master1.getClass(), master1.getId());

    assertTrue(master2.getDetails().isEmpty());
  }
}
