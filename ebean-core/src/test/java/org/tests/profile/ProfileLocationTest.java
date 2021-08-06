package org.tests.profile;

import io.ebean.ProfileLocation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileLocationTest {

  private static final ProfileLocation loc = ProfileLocation.create(12, "foo");
  private static final ProfileLocation locB = ProfileLocation.create();
  private static final ProfileLocation loc2 = ProfileLocation.create();

  private boolean doIt() {
    locB.obtain(); // simulate a location moving by line number only
    return loc.obtain();
  }

  @Test
  public void test_obtain() {
    assertThat(doIt()).isTrue();
    assertThat(loc.fullLocation()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:16)");
    assertThat(loc.location()).isEqualTo("ProfileLocationTest.doIt(ProfileLocationTest.java:16)");
    assertThat(loc.label()).isEqualTo("ProfileLocationTest.doIt");
    assertThat(loc.hash()).isEqualTo(1867926812L);

    // same hash even when the line number has changed
    assertThat(locB.fullLocation()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:15)");
    assertThat(locB.location()).isEqualTo("ProfileLocationTest.doIt(ProfileLocationTest.java:15)");
    assertThat(locB.label()).isEqualTo("ProfileLocationTest.doIt");
    assertThat(locB.hash()).isEqualTo(1867926812L);
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
    assertThat(loc2.location()).isEqualTo("ProfileLocationTest$Other.<init>(ProfileLocationTest.java:52)");
  }

  static class Other {

    Other() {
      loc2.obtain();
    }
  }
}
