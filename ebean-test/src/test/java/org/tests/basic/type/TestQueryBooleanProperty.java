package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TOne;

public class TestQueryBooleanProperty extends BaseTestCase {

  @Test
  public void test() {

    // when run in MySql is test for BUG 323
    DB.find(TOne.class).where().eq("active", true).findList();
  }
}
