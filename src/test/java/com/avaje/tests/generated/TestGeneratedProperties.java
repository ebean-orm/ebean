package com.avaje.tests.generated;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.EGenProps;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestGeneratedProperties extends BaseTestCase {

  @Test
  public void test_insert() {

    EGenProps bean = new EGenProps();
    bean.setName("inserting");
    Ebean.save(bean);

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

    assertThat(bean.getWhenCreated().toInstant().toEpochMilli()).isEqualTo(bean.getLongCreated());
    assertThat(bean.getWhenModified().toInstant().toEpochMilli()).isEqualTo(bean.getLongCreated());
    assertThat(bean.getWhenModified().toInstant().toEpochMilli()).isEqualTo(bean.getLongCreated());

    bean.setName("updating...");
    Ebean.save(bean);

    assertThat(bean.getWhenModified()).isNotEqualTo(bean.getLongCreated());
    assertThat(bean.getWhenModified().toInstant().toEpochMilli()).isEqualTo(bean.getLongUpdated());

    Ebean.delete(bean);
  }
}
