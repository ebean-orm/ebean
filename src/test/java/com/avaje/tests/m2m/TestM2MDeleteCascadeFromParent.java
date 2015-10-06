package com.avaje.tests.m2m;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.MnocRole;
import com.avaje.tests.model.basic.MnocUser;
import com.avaje.tests.model.m2m.MnyA;
import com.avaje.tests.model.m2m.MnyB;
import com.avaje.tests.model.m2m.MnyC;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.List;

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
    Ebean.saveAssociation(c0, "bs");

    SqlQuery sqlQuery = Ebean.createSqlQuery("select count(*) as count from mny_b_mny_c");
    SqlRow unique = sqlQuery.findUnique();
    assertEquals(Long.valueOf(1), unique.getLong("count"));


    //Ebean.deleteManyToManyAssociations(b0, "cs");
    a0.delete();

  }


}
