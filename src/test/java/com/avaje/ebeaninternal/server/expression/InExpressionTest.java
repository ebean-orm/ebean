package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class InExpressionTest {


  @Test
  public void queryPlanHash_given_diffPropertyName_should_differentPlanHash() throws Exception {

    List<Integer> values = values(42, 92);

    InExpression ex1 = new InExpression("foo", values, false);
    InExpression ex2 = new InExpression("bar", values, false);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertNotEquals(b1.build(), b2.build());
  }

  @Test
  public void queryPlanHash_given_diffBindCount_should_differentPlanHash() throws Exception {

    List<Integer> values1 = values(42, 92);
    List<Integer> values2 = values(42, 92, 82);

    InExpression ex1 = new InExpression("foo", values1, false);
    InExpression ex2 = new InExpression("foo", values2, false);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertNotEquals(b1.build(), b2.build());
  }

  @Test
  public void queryPlanHash_given_diffNotFlag_should_differentPlanHash() throws Exception {

    List<Integer> values = values(42, 92);

    InExpression ex1 = new InExpression("foo", values, true);
    InExpression ex2 = new InExpression("foo", values, false);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertNotEquals(b1.build(), b2.build());
  }

  @Test
  public void queryPlanHash_given_sameNotFlag_should_samePlanHash() throws Exception {

    List<Integer> values = values(42, 92);

    InExpression ex1 = new InExpression("foo", values, true);
    InExpression ex2 = new InExpression("foo", values, true);

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    ex1.queryPlanHash(b1);

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    ex2.queryPlanHash(b2);

    assertEquals(b1.build(), b2.build());
  }

  List<Integer> values(int... vals) {
    ArrayList list = new ArrayList<Integer>();
    for (int i = 0; i < vals.length; i++) {
      list.add(vals[i]);
    }
    return list;
  }
}