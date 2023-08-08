package org.example.records;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAccessorsTest {

  @Test
  void computationInAccessors() {
    var one = new CustomAccessors("a", "b", "c");
    assertThat(one.line1()).isEqualTo("line1:a");
    assertThat(one.line2()).isEqualTo("b|c");
    assertThat(one.city()).isEqualTo("c");
  }

}
