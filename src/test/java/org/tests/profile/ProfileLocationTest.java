package org.tests.profile;

import io.ebean.ProfileLocation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileLocationTest {

  private static ProfileLocation loc = ProfileLocation.create(12, "foo");

  private String doIt() {
    return loc.obtain();
  }

  @Test
  public void test_obtain() {

    assertThat(doIt()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:13)");
  }

  @Test
  public void test_add() {
    loc.add(100);
  }
}
