package org.tests.cache.personinfo;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestStringIdOnly extends BaseTestCase {

  @Test
  public void insert() {

    PersonCacheEmail b0 = new PersonCacheEmail("IdOnly");
    DB.save(b0);

    PersonCacheEmail found = DB.find(PersonCacheEmail.class, b0.getId());
    assertThat(found).isNotNull();
  }

  @Test
  public void insert_whenIdOnly() {

    PersonOther b0 = new PersonOther("IdOnly");
    DB.save(b0);

    PersonOther found = DB.find(PersonOther.class, b0.getId());
    assertThat(found).isNotNull();
  }
}
