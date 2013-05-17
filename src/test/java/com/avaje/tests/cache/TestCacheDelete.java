package com.avaje.tests.cache;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.OCachedBean;
import com.avaje.tests.model.basic.OCachedBeanChild;

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

        OCachedBean cachedBean = new OCachedBean();
        cachedBean.getChildren().add(child);
        cachedBean.getChildren().add(child2);
        Ebean.save(cachedBean);

        // ensure cache has been populated
        Ebean.find(OCachedBeanChild.class, child.getId());
        child2 = Ebean.find(OCachedBeanChild.class, child2.getId());
        cachedBean = Ebean.find(OCachedBean.class, cachedBean.getId());

        // act
        Ebean.delete(child2);

        // assert
        Assert.assertTrue(Ebean.find(OCachedBean.class, cachedBean.getId()).getChildren().size() == 1);
    }
}
