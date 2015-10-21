package com.avaje.tests.cascade;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.TSDetail;
import com.avaje.tests.model.basic.TSMaster;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    assertNotNull(modifyRemovals);
    assertTrue(modifyRemovals.size() == 1);
    assertTrue(modifyRemovals.contains(removedDetail));

    Ebean.save(master);

    TSMaster masterReload = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> detailsReload = masterReload.getDetails();

    // the removed bean has really been removed
    assertTrue(detailsReload.size() == 1);

    TSMaster master3 = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> details3 = master3.getDetails();
    details3.clear();
  }


  @Test
  public void insertThenUpdate() {

    TSMaster m0 = new TSMaster();
    m0.setName("m2");

    m0.addDetail(new TSDetail("m2 detail 1"));
    m0.addDetail(new TSDetail("m2 detail 2"));

    Ebean.save(m0);
    assertThat(m0.getDetails()).hasSize(2);

    m0.getDetails().remove(0);
    Ebean.save(m0);

    TSMaster masterReload = Ebean.find(TSMaster.class, m0.getId());
    assertThat(masterReload.getDetails()).hasSize(1);
  }

}
