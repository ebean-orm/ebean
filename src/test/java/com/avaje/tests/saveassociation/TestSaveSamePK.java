package com.avaje.tests.saveassociation;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TSDetail;
import com.avaje.tests.model.basic.TSMaster;

public class TestSaveSamePK extends BaseTestCase {

  @Test
  public void test() {

    if (isMsSqlServer()) return;

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

    TSDetail fetchedDetail = Ebean.find(TSDetail.class).setId(10000).fetch("master").findUnique();
    
    Assert.assertNotNull(fetchedDetail);
  }
}
