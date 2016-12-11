package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.EOptOneA;
import org.junit.Assert;
import org.junit.Test;

public class TestJoinOptOneCascade extends BaseTestCase {

  @Test
  public void test() {

    // the left join cascades to the join for c
    Query<EOptOneA> query = Ebean.find(EOptOneA.class).fetch("b").fetch("b.c");

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.contains("left join eopt_one_b "));
    Assert.assertTrue(sql.contains("left join eopt_one_c "));
  }

}
