package org.tests.cache.personinfo;

import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonCacheTests {

  private void insert(int id, String email) {

    PersonCacheInfo person = new PersonCacheInfo("P00" + id, "P00" + id);

    PersonCacheEmail personEmail = new PersonCacheEmail("E00" + id, email);
    personEmail.setPersonInfo(person);

    Ebean.save(person);
    Ebean.save(personEmail);
  }

  private void addTestData() {

    insert(1, "testA");
    insert(2, "testB");
    insert(3, "testC");
  }

  @Test
  public void testInQuery() {

    addTestData();

    LoggedSqlCollector.start();

    Ebean.find(PersonCacheInfo.class)
      .select("personId") // do not fetch name
      .setUseCache(true)
      .findList();

    ServerCache beanCache = Ebean.getDefaultServer().getServerCacheManager().getBeanCache(PersonCacheInfo.class);
    beanCache.getStatistics(true);

    List<Object> ids = Arrays.asList(new String[]{"E001", "E002", "E003"});

    List<PersonCacheEmail> emailList =
      Ebean.find(PersonCacheEmail.class)
        .where().idIn(ids)
        .setUseCache(true)
        .findList();


    for (PersonCacheEmail email : emailList) {
      // get property that isn't in the cached data
      assertThat(email.getPersonInfo().getName()).isNotNull();
      System.out.println(email.getPersonInfo().getName());
    }

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);

    assertThat(beanCache.getStatistics(true).getHitCount()).isEqualTo(3);

    System.out.println("Fetch again ...");

    emailList =
      Ebean.find(PersonCacheEmail.class)
        .where().idIn(ids)
        .setUseCache(true)
        .findList();


    for (PersonCacheEmail email : emailList) {
      // get property but it is in the cache data now
      System.out.println(email.getPersonInfo().getName());
      assertThat(email.getPersonInfo().getName()).isNotNull();
    }

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);

    assertThat(beanCache.getStatistics(true).getHitCount()).isEqualTo(3);

  }
}
