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

        OCachedBean parentBean = new OCachedBean();
        parentBean.getChildren().add(child);
        parentBean.getChildren().add(child2);
        Ebean.save(parentBean);

        // confirm there are 2 children loaded from the parent
        Assert.assertEquals(2, Ebean.find(OCachedBean.class, parentBean.getId()).getChildren().size());

        // ensure cache has been populated
        Ebean.find(OCachedBeanChild.class, child.getId());
        child2 = Ebean.find(OCachedBeanChild.class, child2.getId());
        parentBean = Ebean.find(OCachedBean.class, parentBean.getId());

        // act
        Ebean.delete(child2);

        // assert
        OCachedBean beanFromCache = Ebean.find(OCachedBean.class, parentBean.getId());
        Assert.assertEquals(1, beanFromCache.getChildren().size());
    }
}
