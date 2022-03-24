package org.tests.cascade;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import javax.persistence.OptimisticLockException;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPrivateOwnedIgnoreTransientOrphan extends BaseTestCase {

  @Test
  public void test() {

    /** new object **/
    TSMaster master0 = new TSMaster();

    /** recovered after first save **/
    TSMaster master1 = null;

    /** recovered after transient child ignored **/
    TSMaster master2 = null;

    DB.save(master0);

    master1 = DB.find(master0.getClass(), master0.getId());

    // Add then remove a bean that was never saved (to the DB)
    master1.getDetails().add(new TSDetail());
    master1.getDetails().clear();

    try {
      DB.save(master1);
    } catch (OptimisticLockException exception) {
      // Occured when the "unsaved" bean was wrongly being deleted
      fail("Optimistic lock exception wrongly thrown: " + exception.getMessage());
      return;
    }

    master2 = master1 = DB.find(master1.getClass(), master1.getId());

    assertTrue(master2.getDetails().isEmpty());
  }
}
