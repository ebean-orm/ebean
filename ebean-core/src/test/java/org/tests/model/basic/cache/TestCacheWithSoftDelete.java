package org.tests.model.basic.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCacheWithSoftDelete extends BaseTestCase {

  @Test
  public void idIn_expect_hitCache() {

    ESoftWithCache bean = new ESoftWithCache("hello");
    DB.save(bean);

    final List<ESoftWithCache> found = DB.find(ESoftWithCache.class)
      .where().idIn(bean.id())
      //.setUseCache(true)
      .findList();

    assertThat(found).hasSize(1);

    final List<ESoftWithCache> foundAgain = DB.find(ESoftWithCache.class)
      .where().idIn(bean.id())
      //.setUseCache(true)
      .findList();

    assertThat(foundAgain).hasSize(1);

  }
}
