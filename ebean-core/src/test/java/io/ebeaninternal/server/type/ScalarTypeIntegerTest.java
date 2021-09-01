package io.ebeaninternal.server.type;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeIntegerTest {

  private final ScalarTypeInteger type = new ScalarTypeInteger();

  @Test
  public void format_when_string() {
    assertThat(type.format("1")).isEqualTo("1");
  }

  @Test
  public void format_when_integer() {
    assertThat(type.format(1)).isEqualTo("1");
  }

}
