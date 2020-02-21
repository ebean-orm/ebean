package io.ebeaninternal.server.expression.platform;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class H2DbExpressionTest {

  H2DbExpression expression = new H2DbExpression();

  @Test
  public void concat() {
    assertThat(expression.concat("p0", ",", "q1", "suffix")).isEqualTo("concat(p0,',',q1,'suffix')");
    assertThat(expression.concat("p0", ",", "q1", null)).isEqualTo("concat(p0,',',q1)");
    assertThat(expression.concat("p0", ",", "q1", "")).isEqualTo("concat(p0,',',q1)");
  }
}
