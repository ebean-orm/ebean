package org.tests.generated;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.EGenProps;

import java.time.Instant;

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

  @Test
  public void test_update_no_overwrite() {
    EGenProps bean = new EGenProps();
    bean.setName("updating");
    DB.save(bean);

    bean = DB.find(EGenProps.class, bean.getId());
    bean.setInstantCreated(Instant.parse("2022-01-01T00:00:00Z"));
    bean.setInstantUpdated(Instant.parse("2022-01-02T00:00:00Z"));
    try (Transaction txn = DB.beginTransaction()) {
      txn.setOverwriteGeneratedProperties(false);
      DB.save(bean);
      txn.commit();
    }

    bean = DB.find(EGenProps.class, bean.getId());
    assertThat(bean.getInstantCreated()).isEqualTo(Instant.parse("2022-01-01T00:00:00Z"));
    assertThat(bean.getInstantUpdated()).isEqualTo(Instant.parse("2022-01-02T00:00:00Z"));
  }

  @Test
  public void test_insert_no_overwrite() {
    EGenProps bean = new EGenProps();
    try (Transaction txn = DB.beginTransaction()) {
      txn.setOverwriteGeneratedProperties(false);
      bean.setName("inserting");
      bean.setInstantCreated(Instant.parse("2022-01-01T00:00:00Z"));
      bean.setInstantUpdated(Instant.parse("2022-01-02T00:00:00Z"));
      DB.save(bean);
      txn.commit();
    }


    bean = DB.find(EGenProps.class, bean.getId());
    assertThat(bean.getInstantCreated()).isEqualTo(Instant.parse("2022-01-01T00:00:00Z"));
    assertThat(bean.getInstantUpdated()).isEqualTo(Instant.parse("2022-01-02T00:00:00Z"));
  }
}
