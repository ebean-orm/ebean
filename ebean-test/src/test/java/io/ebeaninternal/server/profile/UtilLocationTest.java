package io.ebeaninternal.server.profile;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilLocationTest {

  @Test
  public void label() {
    assertThat(UtilLocation.label("foo")).isEqualTo("foo");
    assertThat(UtilLocation.label("ProfileLocationTest$Other.<init>")).isEqualTo("ProfileLocationTest$Other.init");
  }

  @Test
  public void loc() {
    assertThat(UtilLocation.loc("org.foo.MyFoo.doIt(MyFoo.java:12)")).isEqualTo("org.foo.MyFoo.doIt");
    assertThat(UtilLocation.label("org.foo.MyFoo.doIt")).isEqualTo("MyFoo.doIt");
  }
}
