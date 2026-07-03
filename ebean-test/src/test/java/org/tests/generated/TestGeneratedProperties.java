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
  public void test_insert_generatedPropertiesDisabled_preservesBeanValues() {
    Instant created = Instant.parse("2022-01-01T00:00:00Z");
    Instant updated = Instant.parse("2022-01-02T00:00:00Z");

    EGenProps bean = new EGenProps();
    bean.setName("restore-insert");
    bean.setInstantCreated(created);
    bean.setInstantUpdated(updated);

    try (Transaction txn = DB.beginTransaction()) {
      txn.setGeneratedPropertiesEnabled(false);
      DB.save(bean);
      txn.commit();
    }

    bean = DB.find(EGenProps.class, bean.getId());
    assertThat(bean.getInstantCreated()).isEqualTo(created);
    assertThat(bean.getInstantUpdated()).isEqualTo(updated);
    DB.delete(bean);
  }

  @Test
  public void test_insert_generatedPropertiesDisabled_nullValueStillFilled() {
    EGenProps bean = new EGenProps();
    bean.setName("restore-insert-null");
    // intentionally NOT setting instantCreated / instantUpdated

    try (Transaction txn = DB.beginTransaction()) {
      txn.setGeneratedPropertiesEnabled(false);
      DB.save(bean);
      txn.commit();
    }

    bean = DB.find(EGenProps.class, bean.getId());
    // null values must still be filled by the generator
    assertThat(bean.getInstantCreated()).isNotNull();
    assertThat(bean.getInstantUpdated()).isNotNull();
    DB.delete(bean);
  }

  @Test
  public void test_update_generatedPropertiesDisabled_preservesBeanValues() {
    EGenProps bean = new EGenProps();
    bean.setName("restore-update");
    DB.save(bean);

    Instant created = Instant.parse("2022-03-01T00:00:00Z");
    Instant updated = Instant.parse("2022-03-02T00:00:00Z");

    bean = DB.find(EGenProps.class, bean.getId());
    bean.setInstantCreated(created);
    bean.setInstantUpdated(updated);

    try (Transaction txn = DB.beginTransaction()) {
      txn.setGeneratedPropertiesEnabled(false);
      DB.save(bean);
      txn.commit();
    }

    bean = DB.find(EGenProps.class, bean.getId());
    assertThat(bean.getInstantCreated()).isEqualTo(created);
    assertThat(bean.getInstantUpdated()).isEqualTo(updated);
    DB.delete(bean);
  }
}
