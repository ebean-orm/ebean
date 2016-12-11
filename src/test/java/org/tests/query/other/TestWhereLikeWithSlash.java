package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.EBasic;
import org.junit.Test;

public class TestWhereLikeWithSlash extends BaseTestCase {

  @Test
  public void test() {

    EBasic basic = new EBasic();
    basic.setName("slash\\monkey");

    Ebean.save(basic);

    Query<EBasic> query = Ebean.find(EBasic.class).where().like("name", "slash\\mon%").query();
    query.findList();

    // This doesn't work in the latest version of H2 so disable for now.
    // Still good on Postgres which was the original issue
    //Assert.assertEquals(1, list.size());
  }

}
