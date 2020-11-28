package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.EOptOneA;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJoinOptOneCascade extends BaseTestCase {

  @Test
  public void test() {

    // the left join cascades to the join for c
    Query<EOptOneA> query = Ebean.find(EOptOneA.class).fetch("b").fetch("b.c");

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("left join eopt_one_b ");
    assertThat(sql).contains("left join eopt_one_c ");
  }


  @Test
  public void test_where() {

    // the left join cascades to the join for c
    Query<EOptOneA> query = Ebean.find(EOptOneA.class)
      .where()
      .eq("b.c.nameForC", "foo")
      .query();

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("left join eopt_one_b ");
    assertThat(sql).contains("left join eopt_one_c ");
  }
}
