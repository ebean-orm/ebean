package org.tests.profile;

import io.ebean.ProfileLocation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileLocationTest {

  private static final ProfileLocation loc = ProfileLocation.create("foo");
  private static final ProfileLocation locB = ProfileLocation.create();
  private static final ProfileLocation loc2 = ProfileLocation.create();
  private static final ProfileLocation locWithLine = ProfileLocation.createWithLine();
  private boolean doIt() {
    locB.obtain(); // simulate a location moving by line number only
    boolean result = loc.obtain();
    locWithLine.obtain();
    return result;
  }

  @Test
  void test_obtain() {
    assertThat(doIt()).isTrue();
    assertThat(loc.fullLocation()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:16)");
    assertThat(loc.location()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt");
    assertThat(loc.label()).isEqualTo("ProfileLocationTest.doIt");

    // same hash even when the line number has changed
    assertThat(locB.fullLocation()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:15)");
    assertThat(locB.location()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt");
    assertThat(locB.label()).isEqualTo("ProfileLocationTest.doIt");
  }

  @Test
  void test_obtainWithLine() {
    doIt();

    // same hash even when the line number has changed
    assertThat(locWithLine.fullLocation()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:17)");
    assertThat(locWithLine.location()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt:17");
    assertThat(locWithLine.label()).isEqualTo("ProfileLocationTest.doIt:17");
  }

  @Test
  void test_add() {
    loc.add(100);
  }

  @Test
  void test_constructor() {
    Other other = new Other();
    other.hashCode();

    assertThat(loc2.label()).isEqualTo("ProfileLocationTest$Other.init");
    assertThat(loc2.location()).isEqualTo("org.tests.profile.ProfileLocationTest$Other.<init>");
  }

  static class Other {

    Other() {
      loc2.obtain();
    }
  }
}
