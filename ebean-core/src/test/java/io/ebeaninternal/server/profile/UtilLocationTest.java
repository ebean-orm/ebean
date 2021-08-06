package io.ebeaninternal.server.profile;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilLocationTest {

  @Test
  public void label() {
    assertThat(UtilLocation.label("foo")).isEqualTo("foo");
    assertThat(UtilLocation.label("ProfileLocationTest$Other.<init>(ProfileLocationTest.java:47)")).isEqualTo("ProfileLocationTest$Other.init");
  }

  @Test
  public void hash() {
    assertThat(UtilLocation.hash("org.foo.MyFoo.doIt(MyFoo.java:12)")).isEqualTo(396279222L);
    assertThat(UtilLocation.hash("org.foo.MyFoo.doIt(MyFoo.java:13)")).isEqualTo(396279222L);
    assertThat(UtilLocation.hash("org.foo.MyFoo.doIt(MyFoo.java:945)")).isEqualTo(396279222L);
  }
}
