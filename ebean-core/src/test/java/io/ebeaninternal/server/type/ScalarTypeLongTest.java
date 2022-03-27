package io.ebeaninternal.server.type;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeLongTest {

  private final ScalarTypeLong type = new ScalarTypeLong();

  @Test
  public void format_when_string() {
    assertThat(type.format("1")).isEqualTo("1");
  }

  @Test
  public void format_when_long() {
    assertThat(type.format(1L)).isEqualTo("1");
  }

}
