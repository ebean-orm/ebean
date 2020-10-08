package io.ebeaninternal.server.expression.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OracleDbExpressionTest {

  OracleDbExpression expression = new OracleDbExpression();

  @Test
  public void concat() {
    assertThat(expression.concat("p0", ",", "q1", "suffix")).isEqualTo("(p0||','||q1||'suffix')");
    assertThat(expression.concat("p0", ",", "q1", null)).isEqualTo("(p0||','||q1)");
    assertThat(expression.concat("p0", ",", "q1", "")).isEqualTo("(p0||','||q1)");
  }

}
