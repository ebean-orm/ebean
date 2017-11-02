package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.autotune.model.Origin;
import io.ebeaninternal.server.autotune.service.TunedQueryInfo;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.tests.model.basic.FeatureDescription;
import org.junit.Assert;
import org.junit.Test;

public class TestL2CacheWithSharedBean extends BaseTestCase {

  private TunedQueryInfo createTunedQueryInfo(OrmQueryDetail tunedDetail) {
    Origin origin = new Origin();
    origin.setDetail(tunedDetail.toString());
    return new TunedQueryInfo(origin);
  }

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
    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<FeatureDescription> query = Ebean.find(FeatureDescription.class).setId(f1.getId());

    tunedInfo.tuneQuery((SpiQuery<?>) query);

    query.findOne(); // PUT into cache

    FeatureDescription fd2 = query.findOne(); // LOAD cache

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
