package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.MUser;
import org.junit.Test;

public class TestQueryFilterManyOnM2M extends BaseTestCase {

  @Test
  public void test() {

    Ebean.find(MUser.class).fetch("roles").filterMany("roles").ilike("roleName", "Jim%").findList();

  }

}
