package org.tests.saveassociation;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import static org.junit.Assert.assertNotNull;

public class TestSaveSamePK extends BaseTestCase {

  @Test
  public void test() {

    // delete in case we are running multiple times without full db drop
    DB.delete(TSMaster.class, 10000);

    TSMaster m0 = new TSMaster();
    m0.setId(10000);
    m0.setName("master1");

    DB.save(m0);

    TSDetail tsDetail = new TSDetail("m4 d1");
    tsDetail.setId(10000);
    tsDetail.setMaster(m0);

    DB.save(tsDetail);

    TSDetail fetchedDetail = DB.find(TSDetail.class).setId(10000).fetch("master").findOne();

    assertNotNull(fetchedDetail);
  }
}
