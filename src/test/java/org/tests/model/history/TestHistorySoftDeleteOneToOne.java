package org.tests.model.history;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistorySoftDeleteOneToOne extends BaseTestCase {

  @Test
  public void findOne() {

    HsdUser u1 = new HsdUser("u1");

    Ebean.save(u1);
    Ebean.delete(u1);

    HsdUser one = Ebean.find(HsdUser.class)
      .setId(u1.getId())
      .setIncludeSoftDeletes()
      .findOne();

    assertThat(one).isNotNull();

  }
}
