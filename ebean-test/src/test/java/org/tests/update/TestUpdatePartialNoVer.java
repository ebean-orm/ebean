package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;

public class TestUpdatePartialNoVer extends BaseTestCase {

  @Test
  public void test() {

    EBasic b = new EBasic();
    b.setName("testpart");
    b.setStatus(Status.ACTIVE);
    b.setDescription("description");

    DB.save(b);

    EBasic basic = DB.find(EBasic.class).select("status, name").setId(b.getId()).findOne();

    basic.setName("modiName");

    DB.save(basic);

  }

}
