package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.cache.ServerCache;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.FeatureDescription;

import static org.junit.jupiter.api.Assertions.*;

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

    DB.save(f1);

    ServerCache beanCache = DB.cacheManager().beanCache(FeatureDescription.class);
    beanCache.statistics(true);

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("name");
//    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<FeatureDescription> query = DB.find(FeatureDescription.class).setId(f1.getId());

//    tunedInfo.tuneQuery((SpiQuery<?>) query);

    query.findOne(); // PUT into cache

    FeatureDescription fd2 = query.findOne(); // LOAD cache

    String description0 = fd2.getDescription(); // invoke lazy load
    assertEquals("helloOne", description0);

    // load the cache
    FeatureDescription fetchOne = DB.find(FeatureDescription.class, f1.getId());
    assertNotNull(fetchOne);
    assertEquals(1, beanCache.statistics(false).getSize());

    FeatureDescription fetchTwo = DB.find(FeatureDescription.class, f1.getId());
    FeatureDescription fetchThree = DB.find(FeatureDescription.class, f1.getId());
    assertSame(fetchTwo, fetchThree);

    String description1 = fetchThree.getDescription();
    assertEquals("helloOne", description1);
  }

}
