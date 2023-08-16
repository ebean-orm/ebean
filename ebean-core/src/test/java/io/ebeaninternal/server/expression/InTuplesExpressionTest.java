package io.ebeaninternal.server.expression;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InTuplesExpressionTest {

  @Test
  void literalThreshold_maxInBindingZero_2properties() {
    int threshold = InTuplesExpression.literalThreshold(0, 2);
    assertThat(threshold).isEqualTo(2500);
  }

  @Test
  void literalThreshold_maxInBindingZero_3properties() {
    int threshold = InTuplesExpression.literalThreshold(0, 3);
    assertThat(threshold).isEqualTo(1666);
  }

  @Test
  void literalThreshold_maxInBindingZero_4properties() {
    int threshold = InTuplesExpression.literalThreshold(0, 4);
    assertThat(threshold).isEqualTo(1250);
  }

  @Test
  void literalThreshold_sqlServer_2properties() {
    int threshold = InTuplesExpression.literalThreshold(2000, 2);
    assertThat(threshold).isEqualTo(800);
  }

  @Test
  void literalThreshold_sqlServer_3properties() {
    int threshold = InTuplesExpression.literalThreshold(2000, 3);
    assertThat(threshold).isEqualTo(466);
  }

  @Test
  void literalThreshold_sqlServer_4properties() {
    int threshold = InTuplesExpression.literalThreshold(2000, 4);
    assertThat(threshold).isEqualTo(300);
  }

  @Test
  void literalThreshold_5000() {
    int threshold = InTuplesExpression.literalThreshold(5000, 2);
    assertThat(threshold).isEqualTo(2300);
  }

}
