package io.ebeaninternal.server.profile;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilLocationTest {

  @Test
  public void label() {

    assertThat(UtilLocation.label("foo")).isEqualTo("foo");
    assertThat(UtilLocation.label("ProfileLocationTest$Other.<init>(ProfileLocationTest.java:47)")).isEqualTo("ProfileLocationTest$Other.init");
  }
}
