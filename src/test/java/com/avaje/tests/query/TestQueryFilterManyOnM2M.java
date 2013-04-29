package com.avaje.tests.query;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MUser;

public class TestQueryFilterManyOnM2M extends BaseTestCase {

  @Test
  public void test() {

    Ebean.find(MUser.class).fetch("roles").filterMany("roles").ilike("roleName", "Jim%").findList();

  }

}
