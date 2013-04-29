package com.avaje.tests.cascade;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.TSDetail;
import com.avaje.tests.model.basic.TSMaster;

public class TestPrivateOwned extends BaseTestCase {

  @Test
  public void test() {

    TSMaster m0 = new TSMaster();
    m0.setName("m1");

    m0.addDetail(new TSDetail("m1 detail 1"));
    m0.addDetail(new TSDetail("m1 detail 2"));

    Ebean.save(m0);

    TSMaster master = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> details = master.getDetails();

    TSDetail removedDetail = details.remove(1);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    Set<?> modifyRemovals = bc.getModifyRemovals();

    Assert.assertNotNull(modifyRemovals);
    Assert.assertTrue(modifyRemovals.size() == 1);
    Assert.assertTrue(modifyRemovals.contains(removedDetail));

    Ebean.save(master);

    TSMaster masterReload = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> detailsReload = masterReload.getDetails();

    // the removed bean has really been removed
    Assert.assertTrue(detailsReload.size() == 1);

    TSMaster master3 = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> details3 = master3.getDetails();
    details3.clear();
  }

}
