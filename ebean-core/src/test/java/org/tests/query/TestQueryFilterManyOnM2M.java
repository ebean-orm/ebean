package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.MUser;
import org.junit.jupiter.api.Test;

public class TestQueryFilterManyOnM2M extends BaseTestCase {

  @Test
  public void test() {

    DB.find(MUser.class).fetch("roles").filterMany("roles").ilike("roleName", "Jim%").findList();

  }

}
