package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.FeatureDescription;

import static org.junit.Assert.assertEquals;

public class TestL2CacheWithSharedBean extends BaseTestCase {

//  private TunedQueryInfo createTunedQueryInfo(OrmQueryDetail tunedDetail) {
//    Origin origin = new Origin();
//    origin.setDetail(tunedDetail.toString());
//    return new TunedQueryInfo(origin);
//  }

  @Test
  public void test() {

    FeatureDescription f1 = new FeatureDescription();
    f1.setName("one");
    f1.setDescription("helloOne");

    Ebean.save(f1);

    ServerCache beanCache = Ebean.getServerCacheManager().getBeanCache(FeatureDescription.class);
    beanCache.getStatistics(true);

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("name");
//    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<FeatureDescription> query = Ebean.find(FeatureDescription.class).setId(f1.getId());

//    tunedInfo.tuneQuery((SpiQuery<?>) query);

    query.findOne(); // PUT into cache

    FeatureDescription fd2 = query.findOne(); // LOAD cache

    String description0 = fd2.getDescription(); // invoke lazy load
    assertEquals("helloOne", description0);

    // load the cache
    FeatureDescription fetchOne = Ebean.find(FeatureDescription.class, f1.getId());
    Assert.assertNotNull(fetchOne);
    assertEquals(1, beanCache.getStatistics(false).getSize());

    FeatureDescription fetchTwo = Ebean.find(FeatureDescription.class, f1.getId());
    FeatureDescription fetchThree = Ebean.find(FeatureDescription.class, f1.getId());
    Assert.assertSame(fetchTwo, fetchThree);

    String description1 = fetchThree.getDescription();
    assertEquals("helloOne", description1);
  }

}
