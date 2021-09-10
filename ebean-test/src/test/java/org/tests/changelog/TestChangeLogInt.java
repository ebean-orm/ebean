package org.tests.changelog;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicChangeLog;

public class TestChangeLogInt extends BaseTestCase {

  @Test
  public void test() {

    EBasicChangeLog bean3 = new EBasicChangeLog();
    bean3.setName("bean3");
    bean3.setShortDescription("bean3 hi");
    DB.save(bean3);

    bean3.setName("mod bean3");
    bean3.setShortDescription("update bean3");
    DB.save(bean3);
  }
}
