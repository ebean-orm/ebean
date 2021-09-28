package org.tests.cache.personinfo;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestCacheSoftDeleteLazyLoad {

  private final String personEmailId = "SDLL-Email-01";
  private final String personId = "SDLL-Person-01";

  void setup(){
    PersonCacheInfo person = new PersonCacheInfo(personId , "Hello");
    PersonCacheEmail personEmail = new PersonCacheEmail(personEmailId, "hello@foo.com");
    personEmail.setPersonInfo(person);
    DB.save(person);
    DB.save(personEmail);
  }

  @Test
  void softDeleteReferencedBean_when_lazyLoad_expect() {
    setup();
    // load cache
    DB.find(PersonCacheEmail.class, personEmailId);

    // soft delete our bean referenced by email
    PersonCacheInfo info = DB.find(PersonCacheInfo.class, personId);
    DB.delete(info);

    // load from cache (still references a now soft deleted bean)
    PersonCacheEmail email2 = DB.find(PersonCacheEmail.class, personEmailId);
    assert email2 != null;

    LoggedSql.start();
    PersonCacheInfo personInfo = email2.getPersonInfo();
    assertThat(personInfo).isNotNull();
    assertThat(personInfo.deleted()).isTrue(); // lazy load includes soft deletes

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0))
      .contains(" from person_cache_info t0 where t0.person_id = ?")
      .doesNotContain("and t0.deleted =");
  }
}
