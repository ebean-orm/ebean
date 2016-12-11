package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TOne;
import org.junit.Test;

public class TestQueryBooleanProperty extends BaseTestCase {

  @Test
  public void test() {

    // when run in MySql is test for BUG 323
    Ebean.find(TOne.class).where().eq("active", true).findList();
  }
}
