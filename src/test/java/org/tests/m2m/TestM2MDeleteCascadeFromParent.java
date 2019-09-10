package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlRow;
import org.junit.Test;
import org.tests.model.m2m.MnyA;
import org.tests.model.m2m.MnyB;
import org.tests.model.m2m.MnyC;

import static org.junit.Assert.assertEquals;

public class TestM2MDeleteCascadeFromParent extends BaseTestCase {

  @Test
  public void test() {

    DB.sqlUpdate("delete from mny_b_mny_c").execute();
    DB.sqlUpdate("delete from mny_c").execute();
    DB.sqlUpdate("delete from mny_b").execute();
    DB.sqlUpdate("delete from mny_a").execute();

    MnyC c0 = new MnyC();
    c0.setName("c0");
    c0.save();

    MnyA a0 = new MnyA();
    a0.setName("a0");
    a0.save();

    MnyB b0 = new MnyB();
    b0.setName("b0");
    b0.setA(a0);
    b0.save();

    c0.getBs().add(b0);
    DB.save(c0);

    SqlRow row = DB.sqlQuery("select count(*) as count from mny_b_mny_c").findOne();
    assertEquals(Long.valueOf(1), row.getLong("count"));

    a0.delete();
  }


}
