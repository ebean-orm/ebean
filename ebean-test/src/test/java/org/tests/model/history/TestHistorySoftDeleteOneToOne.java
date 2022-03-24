package org.tests.model.history;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistorySoftDeleteOneToOne extends BaseTestCase {

  @Test
  public void findOne() {

    HsdUser u1 = new HsdUser("u1");

    DB.save(u1);
    DB.delete(u1);

    HsdUser one = DB.find(HsdUser.class)
      .setId(u1.getId())
      .setIncludeSoftDeletes()
      .findOne();

    assertThat(one).isNotNull();

  }
}
