package org.tests.update;

import io.ebean.BaseTestCase;
import org.junit.jupiter.api.Test;

public class TestPostgresTruncate extends BaseTestCase {

  @Test
  public void test() {
    // Database server = DB.getDefault();
    //
    // EBasic e = new EBasic();
    // e.setName("something");
    // e.setStatus(Status.NEW);
    // e.setDescription("wow");
    //
    // server.save(e);
    //
    // server.beginTransaction();
    // Integer oriC =
    // DB.sqlQuery("select count(*) as c from e_basic").findOne().getInteger("c");
    // int rows = DB.sqlUpdate("truncate e_basic cascade").execute();
    // Integer currentC =
    // DB.sqlQuery("select count(*) as c from e_basic").findOne().getInteger("c");
    // server.commitTransaction();
    // //DB.cacheManager().clearAll();
    // System.out.println("table : ori="+oriC+", delC="+rows+", currentC="+currentC);

  }
}
