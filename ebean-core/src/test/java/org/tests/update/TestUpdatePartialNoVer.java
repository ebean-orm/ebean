package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;
import org.junit.Test;

public class TestUpdatePartialNoVer extends BaseTestCase {

  @Test
  public void test() {

    EBasic b = new EBasic();
    b.setName("testpart");
    b.setStatus(Status.ACTIVE);
    b.setDescription("description");

    Ebean.save(b);

    EBasic basic = Ebean.find(EBasic.class).select("status, name").setId(b.getId()).findOne();

    basic.setName("modiName");

    Ebean.save(basic);

  }

}
