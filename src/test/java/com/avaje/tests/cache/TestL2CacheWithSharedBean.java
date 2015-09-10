package com.avaje.tests.cache;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autotune.service.TunedQueryInfo;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.tests.model.basic.FeatureDescription;

public class TestL2CacheWithSharedBean extends BaseTestCase {

  @Test
  public void test() {

    FeatureDescription f1 = new FeatureDescription();
    f1.setName("one");
    f1.setDescription(null);

    Ebean.save(f1);

    ServerCache beanCache = Ebean.getServerCacheManager().getBeanCache(FeatureDescription.class);
    beanCache.getStatistics(true);

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("name");
    TunedQueryInfo tunedInfo = new TunedQueryInfo(tunedDetail);

    Query<FeatureDescription> query = Ebean.find(FeatureDescription.class).setId(f1.getId());

    tunedInfo.tuneQuery((SpiQuery<?>) query);

    query.findUnique(); // PUT into cache

    FeatureDescription fd2 = query.findUnique(); // LOAD cache

    fd2.getDescription(); // invoke lazy load (this fails)

    // load the cache
    FeatureDescription fetchOne = Ebean.find(FeatureDescription.class, f1.getId());
    Assert.assertNotNull(fetchOne);
    Assert.assertEquals(1, beanCache.getStatistics(false).getSize());

    FeatureDescription fetchTwo = Ebean.find(FeatureDescription.class, f1.getId());
    FeatureDescription fetchThree = Ebean.find(FeatureDescription.class, f1.getId());
    Assert.assertSame(fetchTwo, fetchThree);

    String description = fetchThree.getDescription();
    Assert.assertNull(description);
  }

}
