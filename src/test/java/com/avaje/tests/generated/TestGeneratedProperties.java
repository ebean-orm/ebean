package com.avaje.tests.generated;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.EGenProps;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestGeneratedProperties extends BaseTestCase {

  @Test
  public void test_insert() {

    EGenProps bean = new EGenProps();
    bean.setName("inserting");
    Ebean.save(bean);

    assertNotNull(bean.getId());
    assertNotNull(bean.getVersion());
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

    bean.setName("updating...");
    Ebean.save(bean);

    Ebean.delete(bean);
  }
}
