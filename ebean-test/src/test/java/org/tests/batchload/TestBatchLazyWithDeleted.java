package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBatchLazyWithDeleted extends BaseTestCase {

  @Test
  public void testOnDeleted() {

    DB.deleteAll(DB.find(UUTwo.class).findList());
    DB.deleteAll(DB.find(UUOne.class).findList());

    UUOne oneA = new UUOne();
    oneA.setName("oneA");

    UUOne oneB = new UUOne();
    oneB.setName("oneB");

    UUTwo two = new UUTwo();
    two.setName("two-bld-A");
    two.setMaster(oneA);

    UUTwo twoB = new UUTwo();
    twoB.setName("two-bld-B");
    twoB.setMaster(oneA);

    UUTwo twoC = new UUTwo();
    twoC.setName("two-bld-C");
    twoC.setMaster(oneB);

    DB.save(oneA);
    DB.save(oneB);
    DB.save(two);
    DB.save(twoB);
    DB.save(twoC);

    List<UUTwo> list = DB.find(UUTwo.class)
      .fetchLazy("master")
      .where().startsWith("name", "two-bld-")
      .orderBy("name")
      .findList();

    // delete a bean that will be batch lazy loaded but
    // is NOT the bean that will invoke the lazy loading
    // (in this case it is the second bean in the list).
    int deletedCount = DB.delete(UUOne.class, oneB.getId());
    assertEquals(1, deletedCount);

    for (UUTwo u : list) {
      u.getMaster();
      //BeanState beanState = DB.beanState(master);
      //assertTrue(beanState.isReference());
    }

    // invoke lazy loading on the second 'master' bean
    // list.get(2).getMaster().getName();

    // invoke lazy loading on the first 'master' bean
    list.get(0).getMaster().getName();

    // this breaks with IllegalStateException
    // because the 2nd UUOne bean is still in 'reference' state
    // and has not had its lazyLoadProperty set so it fails as
    // it was removed from the "lazy load context" earlier
    try {
      list.get(2).getMaster().getName();
      assertTrue(false);
    } catch (EntityNotFoundException e) {
      // this bean was deleted and lazy loading failed
      assertTrue(true);
    }


  }

}
