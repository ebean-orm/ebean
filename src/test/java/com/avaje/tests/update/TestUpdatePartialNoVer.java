package com.avaje.tests.update;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasic;
import com.avaje.tests.model.basic.EBasic.Status;
import org.junit.Test;

public class TestUpdatePartialNoVer extends BaseTestCase {

  @Test
  public void test() {

    EBasic b = new EBasic();
    b.setName("testpart");
    b.setStatus(Status.ACTIVE);
    b.setDescription("description");

    Ebean.save(b);

    EBasic basic = Ebean.find(EBasic.class).select("status, name").setId(b.getId()).findUnique();

    basic.setName("modiName");

    Ebean.save(basic);

  }

}
