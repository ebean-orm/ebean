package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.FeatureDescription;

import static org.junit.jupiter.api.Assertions.*;

class TestL2CacheWithSharedBean extends BaseTestCase {

  @Test
  void test() {
    FeatureDescription f1 = new FeatureDescription();
    f1.setName("one");
    f1.setDescription("helloOne");
    DB.save(f1);

    ServerCache beanCache = DB.cacheManager().beanCache(FeatureDescription.class);
    beanCache.statistics(true);

    Query<FeatureDescription> query = DB.find(FeatureDescription.class).setId(f1.getId());
    query.findOne(); // PUT into cache

    FeatureDescription fd2 = query.findOne(); // LOAD from cache
    assertEquals("helloOne", fd2.getDescription());

    // load from cache
    FeatureDescription fetchOne = findByIdUnmodifiable(f1.getId());
    assertNotNull(fetchOne);
    assertEquals(1, beanCache.statistics(false).getSize());

    FeatureDescription fetchTwo = findByIdUnmodifiable(f1.getId());
    FeatureDescription fetchThree = findByIdUnmodifiable(f1.getId());
    assertSame(fetchTwo, fetchThree);
    assertEquals("helloOne", fetchThree.getDescription());
  }

  private static FeatureDescription findByIdUnmodifiable(Integer id) {
    return DB.find(FeatureDescription.class)
      .setId(id)
      .setUnmodifiable(true) // with this true, we can return shared bean instances
      .findOne();
  }

}
