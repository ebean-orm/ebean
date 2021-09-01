package io.ebeaninternal.server.type;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeUUIDBaseTest {

  private final ScalarTypeUUIDBase type = new ScalarTypeUUIDNative();
  private final UUID uuid = UUID.randomUUID();

  @Test
  public void format_as_uuid() {
    assertThat(type.format(uuid)).isEqualTo(uuid.toString());
  }

  @Test
  public void format_as_String() {
    assertThat(type.format(uuid.toString())).isEqualTo(uuid.toString());
  }
}
