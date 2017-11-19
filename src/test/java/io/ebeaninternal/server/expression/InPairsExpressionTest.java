package io.ebeaninternal.server.expression;

import io.ebean.Pairs;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class InPairsExpressionTest extends BaseExpressionTest {

  private Pairs pairs() {
    return pairs("sku", "code");
  }

  private Pairs pairs(String property0, String property1) {
    Pairs pairs = new Pairs(property0, property1)
      .add("2", 1000)
      .add("2", 1001)
      .add("3", 1000);

    return pairs;
  }

  @Test
  public void same_samePlan_sameBind() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs(), false);

    same(e0, e1);

    assertTrue(e0.isSameByBind(e1));
  }

  @Test
  public void same_samePlan_diffBind() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs().add("4", 1000), false);
    e0.prepareExpression(multi());
    e1.prepareExpression(multi());

    // when multi() ... same as bind count not important
    same(e0, e1);
    assertFalse(e0.isSameByBind(e1));
  }

  @Test
  public void same_noMulti_diffPlan() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs().add("4", 1000), false);
    e0.prepareExpression(noMulti());
    e1.prepareExpression(noMulti());

    // when noMulti() ... bind count different so different plan
    different(e0, e1);
  }

  @Test
  public void diffProperty0_diff() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs("k", "code"), false);
    different(e0, e1);
  }

  @Test
  public void diffProperty1_diff() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs("sku", "c"), false);
    different(e0, e1);
  }

  @Test
  public void diffSeparator_diff() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs().setConcatSeparator(":"), false);
    different(e0, e1);
  }

  @Test
  public void diffSuffix_diff() throws Exception {

    InPairsExpression e0 = new InPairsExpression(pairs(), false);
    InPairsExpression e1 = new InPairsExpression(pairs().setConcatSuffix(":"), false);
    different(e0, e1);
  }

}
