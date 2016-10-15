package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.EOptOneA;

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
