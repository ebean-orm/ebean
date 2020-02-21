package org.tests.model.embedded;

import io.ebean.DB;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestEmbeddedEqualsWhenCreated {

  @Test
  public void test() {

    UserInterestLive bean = new UserInterestLive(new UserInterestLiveKey(1L, 2L));
    bean.save();

    final UserInterestLive found = DB.find(UserInterestLive.class, bean.getKey());
    assertEquals(found, bean); // <- failed as bean not inserted
  }
}
