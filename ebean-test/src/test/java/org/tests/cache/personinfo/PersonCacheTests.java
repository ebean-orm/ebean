package org.tests.cache.personinfo;

import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheRegion;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersonCacheTests {

  private static final Logger log = LoggerFactory.getLogger(PersonCacheTests.class);

  private final List<Object> ids = Arrays.asList("E001", "E002", "E003");

  private ServerCache beanCacheInfo = DB.getDefault().cacheManager().beanCache(PersonCacheInfo.class);
  private ServerCache beanCacheEmail = DB.getDefault().cacheManager().beanCache(PersonCacheEmail.class);

  private ServerCacheRegion region = DB.getDefault().cacheManager().region("email");

  private void insert(int id, String email) {

    PersonCacheInfo person = new PersonCacheInfo("P00" + id, "P00" + id);

    PersonCacheEmail personEmail = new PersonCacheEmail("E00" + id, email);
    personEmail.setPersonInfo(person);

    DB.save(person);
    DB.save(personEmail);
  }

  private void addTestData() {

    insert(1, "testA");
    insert(2, "testB");
    insert(3, "testC");
  }

  @Test
  public void testInQuery() {

    addTestData();

    LoggedSql.start();

    DB.find(PersonCacheInfo.class)
      .select("personId") // do not fetch name
      .setUseCache(true)
      .findList();

    beanCacheInfo.statistics(true);

    List<PersonCacheEmail> emailList =
      DB.find(PersonCacheEmail.class)
        .where().idIn(ids)
        .setUseCache(true)
        .findList();


    for (PersonCacheEmail email : emailList) {
      // get property that isn't in the cached data
      assertThat(email.getPersonInfo().getName()).isNotNull();
    }

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(3);

    assertThat(beanCacheInfo.statistics(true).getHitCount()).isEqualTo(3);

    // force cache misses on PersonCacheEmail
    beanCacheEmail.clear();
    log.info("Fetch again - cache missing on PersonCacheEmail and cache hits on PersonCacheInfo ...");

    emailList =
      DB.find(PersonCacheEmail.class)
        .where().idIn(ids)
        .setUseCache(true)
        .findList();

    for (PersonCacheEmail email : emailList) {
      // get property but it is in the cache data now
      System.out.println(email.getPersonInfo().getName());
      assertThat(email.getPersonInfo().getName()).isNotNull();
    }

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);

    assertThat(beanCacheInfo.statistics(true).getHitCount()).isEqualTo(3);

    turnOffRegion_expect_noCacheUse();

  }

  private void turnOffRegion_expect_noCacheUse() {

    assertTrue(region.isEnabled());
    beanCacheEmail.statistics(true);

    log.info("Disabled region ...");
    region.setEnabled(false);

    for (Object id : ids) {
      DB.find(PersonCacheEmail.class)
        .where().idEq(id)
        .setUseCache(true)
        .findOne();
    }

    // assert that we didn't hit the cache
    ServerCacheStatistics statistics = beanCacheEmail.statistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(0);
    assertThat(statistics.getMissCount()).isEqualTo(0);

    log.info("Enabled region ...");
    region.setEnabled(true);

    for (Object id : ids) {
      DB.find(PersonCacheEmail.class)
        .where().idEq(id)
        .setUseCache(true)
        .findOne();
    }

    // assert that we DID hit the cache
    statistics = beanCacheEmail.statistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(3);
    assertThat(statistics.getMissCount()).isEqualTo(0);
  }
}
