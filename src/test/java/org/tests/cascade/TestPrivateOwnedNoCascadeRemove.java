package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.TSDetailTwo;
import org.tests.model.basic.TSMasterTwo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TestPrivateOwnedNoCascadeRemove extends BaseTestCase {

  @Test
  public void test() {

    TSMasterTwo m0 = new TSMasterTwo();
    m0.setName("m1");

    m0.addDetail(new TSDetailTwo("m1 detail 1"));
    m0.addDetail(new TSDetailTwo("m1 detail 2"));

    Ebean.save(m0);

    TSMasterTwo master = Ebean.find(TSMasterTwo.class, m0.getId());
    List<TSDetailTwo> details = master.getDetails();

    TSDetailTwo removedDetail = details.remove(1);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    Set<?> modifyRemovals = bc.getModifyRemovals();

    Assert.assertNotNull(modifyRemovals);
    Assert.assertTrue(modifyRemovals.size() == 1);
    Assert.assertTrue(modifyRemovals.contains(removedDetail));

    Ebean.save(master);

    TSMasterTwo masterReload = Ebean.find(TSMasterTwo.class, m0.getId());
    List<TSDetailTwo> detailsReload = masterReload.getDetails();

    // the removed bean has really been removed
    Assert.assertTrue(detailsReload.size() == 1);

    try {
      Ebean.delete(masterReload);
      Assert.fail("delete should error");
    } catch (Exception e) {
      Assert.assertTrue("delete failed", true);
    }
  }

}
