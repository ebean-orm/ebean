package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.basic.OCachedBean;
import org.tests.model.basic.OCachedBeanChild;

import static org.junit.Assert.assertEquals;

/**
 * Test class testing deleting/invalidating of cached beans
 */
public class TestCacheDelete extends BaseTestCase {

  /**
   * When deleting a cached entity all entities with a referenced OneToMany relation must also be invalidated!
   */
  @Test
  public void testCacheDeleteOneToMany() {
    // arrange
    OCachedBeanChild child = new OCachedBeanChild();
    OCachedBeanChild child2 = new OCachedBeanChild();

    OCachedBean parentBean = new OCachedBean();
    parentBean.getChildren().add(child);
    parentBean.getChildren().add(child2);
    Ebean.save(parentBean);

    // confirm there are 2 children loaded from the parent
    assertEquals(2, Ebean.find(OCachedBean.class, parentBean.getId()).getChildren().size());

    // ensure cache has been populated
    Ebean.find(OCachedBeanChild.class, child.getId());
    child2 = Ebean.find(OCachedBeanChild.class, child2.getId());
    parentBean = Ebean.find(OCachedBean.class, parentBean.getId());

    // act
    Ebean.delete(child2);
    awaitL2Cache();

    OCachedBean beanFromCache = Ebean.find(OCachedBean.class, parentBean.getId());
    assertEquals(1, beanFromCache.getChildren().size());
  }
}
