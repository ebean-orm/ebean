package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.tests.model.m2m.MnyA;
import org.tests.model.m2m.MnyB;
import org.tests.model.m2m.MnyC;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestM2MDeleteCascadeFromParent extends BaseTestCase {

  @Test
  public void test() {

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
    Ebean.save(c0);

    SqlQuery sqlQuery = Ebean.createSqlQuery("select count(*) as count from mny_b_mny_c");
    SqlRow unique = sqlQuery.findOne();
    assertEquals(Long.valueOf(1), unique.getLong("count"));


    //Ebean.deleteManyToManyAssociations(b0, "cs");
    a0.delete();

  }


}
