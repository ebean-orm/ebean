package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MUser;

public class TestQueryFilterManyOnM2M extends BaseTestCase {

  @Test
  public void test() {

    DB.find(MUser.class).fetch("roles").filterMany("roles").ilike("roleName", "Jim%").findList();

  }

}
