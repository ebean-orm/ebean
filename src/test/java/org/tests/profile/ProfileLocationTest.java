package org.tests.profile;

import io.ebean.ProfileLocation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileLocationTest {

  private static ProfileLocation loc = ProfileLocation.create(12);

  @Test
  public void test() {

    assertThat(doIt()).isEqualTo("org.tests.profile.ProfileLocationTest.doIt(ProfileLocationTest.java:19)");
  }

  private String doIt() {
    return loc.obtain();
  }
}
