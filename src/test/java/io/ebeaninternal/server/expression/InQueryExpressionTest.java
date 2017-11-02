package io.ebeaninternal.server.expression;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class InQueryExpressionTest extends BaseExpressionTest {


  private InQueryExpression exp(String propertyName, boolean not, String sql, Object... bindValues) {
    return new InQueryExpression(propertyName, not, sql, Arrays.asList(bindValues));
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp("name", true, "sql", 10), exp("name", true, "sql", 10));
  }

  @Test
  public void isSameByPlan_when_diffBind_same() {

    same(exp("name", true, "sql", 10), exp("name", true, "sql", 20));
  }

  @Test
  public void isSameByPlan_when_diffNPropertyName() {

    different(exp("name", true, "sql", 10), exp("nameDiff", true, "sql", 10));
  }

  @Test
  public void isSameByPlan_when_diffNot() {

    different(exp("name", true, "sql", 10), exp("name", false, "sql", 10));
  }

  @Test
  public void isSameByPlan_when_diffSql() {

    different(exp("name", true, "sql", 10), exp("name", true, "sqlDiff", 10));
  }

  @Test
  public void isSameByBind_when_sameBindValues() {

    assertThat(exp("name", true, "sql", 10).isSameByBind(exp("name", true, "sql", 10))).isTrue();
  }

  @Test
  public void isSameByBind_when_sameMultipleBindValues() {

    assertThat(exp("name", true, "sql", 10, "ABC", 20).isSameByBind(exp("name", true, "sql", 10, "ABC", 20))).isTrue();
  }

  @Test
  public void isSameByBind_when_diffMultipleBindValues() {

    assertThat(exp("name", true, "sql", 10, "ABC", 20).isSameByBind(exp("name", true, "sql", 10, "ABC", 21))).isFalse();
  }

  @Test
  public void isSameByBind_when_diffMultipleBindValuesByOrder() {

    assertThat(exp("name", true, "sql", 10, "ABC", 20).isSameByBind(exp("name", true, "sql", 10, 20, "ABC"))).isFalse();
  }

  @Test
  public void isSameByBind_when_diffBindValues() {

    assertThat(exp("name", true, "sql", 10).isSameByBind(exp("name", true, "sql", 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_lessBindValues() {

    assertThat(exp("name", true, "sql", 10, 20).isSameByBind(exp("name", true, "sql", 20))).isFalse();
  }

  @Test
  public void isSameByBind_when_moreBindValues() {

    assertThat(exp("name", true, "sql", 10).isSameByBind(exp("name", true, "sql", 10, 20))).isFalse();
  }
}
