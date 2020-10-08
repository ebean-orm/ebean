package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.OptimisticLockException;

public class TestPrivateOwnedIgnoreTransientOrphan extends BaseTestCase {

  @Test
  public void test() {

    /** new object **/
    TSMaster master0 = new TSMaster();

    /** recovered after first save **/
    TSMaster master1 = null;

    /** recovered after transient child ignored **/
    TSMaster master2 = null;

    Ebean.save(master0);

    master1 = Ebean.find(master0.getClass(), master0.getId());

    // Add then remove a bean that was never saved (to the DB)
    master1.getDetails().add(new TSDetail());
    master1.getDetails().clear();

    try {
      Ebean.save(master1);
    } catch (OptimisticLockException exception) {
      // Occured when the "unsaved" bean was wrongly being deleted
      Assert.fail("Optimistic lock exception wrongly thrown: " + exception.getMessage());
      return;
    }

    master2 = master1 = Ebean.find(master1.getClass(), master1.getId());

    Assert.assertTrue(master2.getDetails().isEmpty());
  }
}
