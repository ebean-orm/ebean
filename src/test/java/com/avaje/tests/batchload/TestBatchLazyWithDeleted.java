package com.avaje.tests.batchload;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.UUOne;
import com.avaje.tests.model.basic.UUTwo;

public class TestBatchLazyWithDeleted extends BaseTestCase {

  @Test
  public void testOnDeleted() {

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

    Ebean.save(oneA);
    Ebean.save(oneB);
    Ebean.save(two);
    Ebean.save(twoB);
    Ebean.save(twoC);

    List<UUTwo> list = Ebean.find(UUTwo.class)
        .fetch("master", new FetchConfig().lazy(5))
        .where().startsWith("name", "two-bld-")
        .order("name")
        .findList();

    // delete a bean that will be batch lazy loaded but
    // is NOT the bean that will invoke the lazy loading
    // (in this case it is the second bean in the list).
    int deletedCount = Ebean.delete(UUOne.class, oneB.getId());
    Assert.assertEquals(1, deletedCount);

    for (UUTwo u : list) {
      UUOne master = u.getMaster();
      //BeanState beanState = Ebean.getBeanState(master);
      //Assert.assertTrue(beanState.isReference());
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
      Assert.assertTrue(false);
    } catch (EntityNotFoundException e) {
      // this bean was deleted and lazy loading failed 
      Assert.assertTrue(true);
    }


  }

}
