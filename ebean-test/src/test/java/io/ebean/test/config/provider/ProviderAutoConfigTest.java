package io.ebean.test.config.provider;

import io.ebean.config.ServerConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProviderAutoConfigTest {

  private ProviderAutoConfig config = new ProviderAutoConfig(new ServerConfig());

  @Test
  public void msg() {

    assertEquals(config.msg(1), "for testing purposes a current user provider has been configured. Use io.ebean.test.UserContext to set current user in tests.");
    assertEquals(config.msg(2), "for testing purposes a current tenant provider has been configured. Use io.ebean.test.UserContext to set current tenant in tests.");
    assertEquals(config.msg(3), "for testing purposes a current user and tenant provider has been configured. Use io.ebean.test.UserContext to set current user and tenant in tests.");
  }

  @Test
  public void msgUnexpected() {
    // rather than fail ...
    assertEquals(config.msg(4), "for testing purposes [unexpected??] has been configured. Use io.ebean.test.UserContext to [unexpected??] in tests.");
  }
}
