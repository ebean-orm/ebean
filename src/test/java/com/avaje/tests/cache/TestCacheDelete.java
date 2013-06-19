package com.avaje.tests.cache;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.CachedBean;
import com.avaje.tests.model.basic.CachedBeanChild;
import org.junit.Assert;
import org.junit.Test;

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
        CachedBeanChild child = new CachedBeanChild();
        CachedBeanChild child2 = new CachedBeanChild();

        CachedBean cachedBean = new CachedBean();
        cachedBean.getChildren().add(child);
        cachedBean.getChildren().add(child2);
        Ebean.save(cachedBean);

        // ensure cache has been populated
        Ebean.find(CachedBeanChild.class, child.getId());
        child2 = Ebean.find(CachedBeanChild.class, child2.getId());
        cachedBean = Ebean.find(CachedBean.class, cachedBean.getId());

        // act
        Ebean.delete(child2);

        // assert
        Assert.assertTrue(Ebean.find(CachedBean.class, cachedBean.getId()).getChildren().size() == 1);
    }
}
