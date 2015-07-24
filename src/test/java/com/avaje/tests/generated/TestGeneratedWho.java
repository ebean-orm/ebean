package com.avaje.tests.generated;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.MyCurrentUserProvider;
import com.avaje.tests.model.EWhoProps;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestGeneratedWho extends BaseTestCase {

  @Test
  public void insertUpdate() {

    EWhoProps bean = new EWhoProps();
    bean.setName("one");

    MyCurrentUserProvider.setUserId("INSERT_WHO_1");
    Ebean.save(bean);

    assertEquals("one", bean.getName());
    assertEquals("INSERT_WHO_1", bean.getWhoCreated());
    assertEquals("INSERT_WHO_1", bean.getWhoModified());
    assertNotNull(bean.getWhenCreated());
    assertNotNull(bean.getWhenModified());

    MyCurrentUserProvider.setUserId("UPDATE_WHO_1");
    bean.setName("two");
    Ebean.save(bean);

    assertEquals("two", bean.getName());
    assertEquals("INSERT_WHO_1", bean.getWhoCreated());
    assertEquals("UPDATE_WHO_1", bean.getWhoModified());

    MyCurrentUserProvider.setUserId("UPDATE_WHO_2");
    bean.setName("three");
    Ebean.save(bean);

    assertEquals("INSERT_WHO_1", bean.getWhoCreated());
    assertEquals("UPDATE_WHO_2", bean.getWhoModified());

    MyCurrentUserProvider.resetToDefault();
  }
}
