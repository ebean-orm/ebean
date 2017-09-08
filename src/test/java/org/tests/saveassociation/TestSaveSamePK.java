package org.tests.saveassociation;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;
import org.junit.Assert;
import org.junit.Test;

public class TestSaveSamePK extends BaseTestCase {

  @Test
  public void test() {

    // delete in case we are running multiple times without full db drop
    Ebean.delete(TSMaster.class, 10000);

    TSMaster m0 = new TSMaster();
    m0.setId(10000);
    m0.setName("master1");

    Ebean.save(m0);

    TSDetail tsDetail = new TSDetail("master1 detail1");
    tsDetail.setId(10000);
    tsDetail.setMaster(m0);

    Ebean.save(tsDetail);

    TSDetail fetchedDetail = Ebean.find(TSDetail.class).setId(10000).fetch("master").findOne();

    Assert.assertNotNull(fetchedDetail);
  }
}
