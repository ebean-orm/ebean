package org.tests.generated;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.EGenProps;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestGeneratedProperties extends BaseTestCase {

  @Test
  public void test_insert() {

    EGenProps bean = new EGenProps();
    bean.setName("inserting");
    DB.save(bean);

    assertNotNull(bean.getId());
    assertNotNull(bean.getVersion());
    assertNotNull(bean.getWhenCreated());
    assertNotNull(bean.getWhenModified());
    assertNotNull(bean.getTsCreated());
    assertNotNull(bean.getTsUpdated());
    assertNotNull(bean.getLdtCreated());
    assertNotNull(bean.getLdtUpdated());
    assertNotNull(bean.getOdtCreated());
    assertNotNull(bean.getOdtUpdated());
    assertNotNull(bean.getZdtCreated());
    assertNotNull(bean.getZdtUpdated());
    assertNotNull(bean.getLongCreated());
    assertNotNull(bean.getLongUpdated());
    assertNotNull(bean.getInstantCreated());
    assertNotNull(bean.getInstantUpdated());

    assertThat(bean.getWhenCreated().toInstant().toEpochMilli()).isEqualTo(bean.getLongCreated());
    assertThat(bean.getWhenModified().toInstant().toEpochMilli()).isEqualTo(bean.getLongCreated());

    bean.setName("updating...");
    DB.save(bean);

    assertThat(bean.getWhenModified()).isNotEqualTo(bean.getLongCreated());
    assertThat(bean.getWhenModified().toInstant().toEpochMilli()).isEqualTo(bean.getLongUpdated());
    assertThat(bean.getInstantUpdated().toEpochMilli()).isEqualTo(bean.getLongUpdated());
    assertThat(bean.getInstantCreated().toEpochMilli()).isEqualTo(bean.getLongCreated());

    DB.delete(bean);
  }
}
