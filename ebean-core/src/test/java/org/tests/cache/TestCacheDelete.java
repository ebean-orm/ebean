package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedBean;
import org.tests.model.basic.OCachedBeanChild;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    DB.save(parentBean);

    // confirm there are 2 children loaded from the parent
    assertEquals(2, DB.find(OCachedBean.class, parentBean.getId()).getChildren().size());

    // ensure cache has been populated
    DB.find(OCachedBeanChild.class, child.getId());
    child2 = DB.find(OCachedBeanChild.class, child2.getId());
    parentBean = DB.find(OCachedBean.class, parentBean.getId());

    // act
    DB.delete(child2);
    awaitL2Cache();

    OCachedBean beanFromCache = DB.find(OCachedBean.class, parentBean.getId());
    assertEquals(1, beanFromCache.getChildren().size());
  }
}
