package io.ebeaninternal.server.profile;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicProfileLocationTest {

  @Test
  public void obtain() {

    DProfileLocation loc = new DProfileLocation(12);
    String obtain = loc.obtain();

    assertThat(obtain).endsWith(":12)");
  }
}
