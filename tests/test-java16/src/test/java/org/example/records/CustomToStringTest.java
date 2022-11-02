package org.example.records;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomToStringTest {

  @Test
  void testToString() {
    assertThat(new CustomToString("a", "b", "c").toString()).isEqualTo("[line1='a', line2='b', city='c']");
    assertThat(new CustomToString("z", "x", "y").toString()).isEqualTo("[line1='z', line2='x', city='y']");
  }
}
