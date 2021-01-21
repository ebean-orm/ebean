package org.integration;

import io.ebean.redis.DuelCache;
import org.domain.Person;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.domain.query.QPerson;
import org.junit.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterTest {


  private Database createOther(DataSource dataSource) {

    DatabaseConfig config = new DatabaseConfig();
    config.setDataSource(dataSource);
    config.loadFromProperties();
    config.setDefaultServer(false);
    config.setName("other");
    config.setDdlGenerate(false);
    config.setDdlRun(false);

    return DatabaseFactory.create(config);
  }

  @Test
  public void testBothNear() {
    // ensure the default server exists first
    final Database db = DB.getDefault();
    Database other = createOther(db.getPluginApi().getDataSource());

    new QPerson()
      .name.eq("Someone")
      .delete();

    Person foo = new Person("Someone");
    foo.save();

    DB.getServerCacheManager().clearAll();
    other.getMetaInfoManager().resetAllMetrics();

    Person fooA = DB.find(Person.class, foo.getId());
    Person fooB = other.find(Person.class, foo.getId());
    fooA = DB.find(Person.class, foo.getId());
    fooB = other.find(Person.class, foo.getId());
    fooA = DB.find(Person.class, foo.getId());
    fooB = other.find(Person.class, foo.getId());

    DuelCache dualCache = (DuelCache) other.getServerCacheManager().getBeanCache(Person.class);
    assertCounts(dualCache, 2, 1, 1, 0);

  }

  @Test
  public void test() throws InterruptedException {

    // ensure the default server exists first
    final Database db = DB.getDefault();

    Database other = createOther(db.getPluginApi().getDataSource());

    for (int i = 0; i < 10; i++) {
      Person foo = new Person("name " + i);
      foo.save();
    }

    other.getServerCacheManager().clearAll();
    other.getMetaInfoManager().resetAllMetrics();

    DuelCache dualCache = (DuelCache) other.getServerCacheManager().getBeanCache(Person.class);

    Person foo0 = other.find(Person.class, 1);
    assertCounts(dualCache, 0, 1, 0, 1);

    other.find(Person.class, 1);
    assertCounts(dualCache, 1, 1, 0, 1);

    other.find(Person.class, 1);
    assertCounts(dualCache, 2, 1, 0, 1);

    other.find(Person.class, 1);
    assertCounts(dualCache, 3, 1, 0, 1);

    other.find(Person.class, 2);
    assertCounts(dualCache, 3, 2, 0, 2);


    foo0.setName("name2");
    foo0.save();
    allowAsyncMessaging();

    Person foo3 = other.find(Person.class, 1);
    assertThat(foo3.getName()).isEqualTo("name2");
    assertCounts(dualCache, 3, 3, 1, 2);


    foo0.setName("name3");
    foo0.save();
    allowAsyncMessaging();

    foo3 = other.find(Person.class, 1);
    assertThat(foo3.getName()).isEqualTo("name3");
    assertCounts(dualCache, 3, 4, 2, 2);
  }

  private void assertCounts(DuelCache dualCache, int nearHits, int nearMiss, int remoteHit, int remoteMiss) {

    assertThat(dualCache.getNearHitCount()).isEqualTo(nearHits);
    assertThat(dualCache.getNearMissCount()).isEqualTo(nearMiss);
    assertThat(dualCache.getRemoteHitCount()).isEqualTo(remoteHit);
    assertThat(dualCache.getRemoteMissCount()).isEqualTo(remoteMiss);
  }

  private void allowAsyncMessaging() throws InterruptedException {
    Thread.sleep(20);
  }
}
