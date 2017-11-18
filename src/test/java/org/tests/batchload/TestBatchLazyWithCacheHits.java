package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.tests.model.basic.UUOne;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestBatchLazyWithCacheHits extends BaseTestCase {

  private ServerCache beanCache = server().getServerCacheManager().getBeanCache(UUOne.class);

  private UUOne insert(String name) {
    UUOne one = new UUOne();
    one.setName("testBLWCH" + name);
    Ebean.save(one);
    return one;
  }

  @Test
  public void testOnCacheHit() {

    ArrayList<UUOne> inserted = insertData();

    clearCacheAndStatistics();

    UUOne b = Ebean.find(UUOne.class, inserted.get(1).getId());
    assertNotNull(b);

    UUOne b2 = Ebean.find(UUOne.class, inserted.get(1).getId());
    assertNotNull(b2);

    assertBeanCacheHits(1);

    UUOne c = Ebean.find(UUOne.class)
      .where().idEq(inserted.get(2).getId())
      .findOne();
    assertNotNull(c);

    UUOne c2 = Ebean.find(UUOne.class)
      .where().idEq(inserted.get(2).getId())
      .findOne();

    assertNotNull(c2);
    assertBeanCacheHits(1);

    LoggedSqlCollector.start();

    List<UUOne> list = Ebean.find(UUOne.class)
      //.setDefaultLazyLoadBatchSize(5)
      .select("id")
      .where().startsWith("name", "testBLWCH")
      .order("name")
      .findList();

    for (UUOne uuOne : list) {
      uuOne.getName();
    }
    list.get(0).getName();

    List<String> sql = LoggedSqlCollector.stop();
    System.out.println("sql:" + sql);

    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("from uuone t0 where t0.name like ");
    platformAssertIn(sql.get(1), "from uuone t0 where t0.id");

    // not lazy loading into bean cache
    int size = beanCache.getStatistics(true).getSize();
    assertThat(size).isEqualTo(2);
  }

  private void assertBeanCacheHits(int hits) {
    ServerCacheStatistics statistics = beanCache.getStatistics(true);
    assertEquals(statistics.getHitCount(), hits);
  }

  private void clearCacheAndStatistics() {

    beanCache.clear();
    beanCache.getStatistics(true);
  }

  private ArrayList<UUOne> insertData() {
    ArrayList<UUOne> inserted = new ArrayList<>();
    String[] names = "A,B,C,D,E,F,G,H,I,J".split(",");
    for (String name : names) {
      inserted.add(insert(name));
    }
    return inserted;
  }

}
