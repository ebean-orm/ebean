package io.ebeaninternal.server.expression.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresDbExpressionTest {

  PostgresDbExpression expression = new PostgresDbExpression();

  @Test
  public void concat() {
    assertThat(expression.concat("p0", ",", "p1", "suffix")).isEqualTo("(p0||','||p1||'suffix')");
    assertThat(expression.concat("p0", ",", "p1", null)).isEqualTo("(p0||','||p1)");
    assertThat(expression.concat("p0", ",", "p1", "")).isEqualTo("(p0||','||p1)");
  }
}
