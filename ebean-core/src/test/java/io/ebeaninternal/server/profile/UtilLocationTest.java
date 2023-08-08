package io.ebeaninternal.server.profile;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilLocationTest {

  @Test
  void label() {
    Assertions.assertThat(UtilLocation.label("foo")).isEqualTo("foo");
    assertThat(UtilLocation.label("ProfileLocationTest$Other.<init>")).isEqualTo("ProfileLocationTest$Other.init");
  }

  @Test
  void loc() {
    assertThat(UtilLocation.loc("org.foo.MyFoo.doIt(MyFoo.java:12)")).isEqualTo("org.foo.MyFoo.doIt");
    assertThat(UtilLocation.label("org.foo.MyFoo.doIt")).isEqualTo("MyFoo.doIt");
  }

  @Test
  void locWithLineNumber() {
    assertThat(UtilLocation.loc("org.foo.MyFoo.doIt(MyFoo.java:12)", true)).isEqualTo("org.foo.MyFoo.doIt:12");
    assertThat(UtilLocation.label("org.foo.MyFoo.doIt:12")).isEqualTo("MyFoo.doIt:12");
  }
}
