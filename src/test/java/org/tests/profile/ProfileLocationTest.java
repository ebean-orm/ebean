package org.tests.profile;

import io.ebean.ProfileLocation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileLocationTest {

  private static ProfileLocation loc = ProfileLocation.create(12, "foo");

  private static ProfileLocation loc2 = ProfileLocation.create();

  private String doIt() {
    return loc.obtain();
  }

  @Test
  public void test_obtain() {

    assertThat(doIt()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:15)");
    assertThat(loc.shortDescription()).isEqualTo("ProfileLocationTest.doIt(ProfileLocationTest.java:15)");
    assertThat(loc.label()).isEqualTo("ProfileLocationTest.doIt");
  }

  @Test
  public void test_add() {
    loc.add(100);
  }

  @Test
  public void test_constructor() {

    Other other = new Other();
    other.hashCode();

    assertThat(loc2.label()).isEqualTo("ProfileLocationTest$Other.init");
    assertThat(loc2.shortDescription()).isEqualTo("ProfileLocationTest$Other.<init>(ProfileLocationTest.java:44)");
  }

  static class Other {

    Other() {
      loc2.obtain();
    }
  }
}
