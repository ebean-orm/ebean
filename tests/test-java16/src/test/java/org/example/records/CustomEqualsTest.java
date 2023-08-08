package org.example.records;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomEqualsTest {

  @Test
  void equals() {
    var one = new CustomEquals("a", "b", "c");
    var two = new CustomEquals("a", "b", "c");

    assertThat(one).isEqualTo(two);
  }
}
