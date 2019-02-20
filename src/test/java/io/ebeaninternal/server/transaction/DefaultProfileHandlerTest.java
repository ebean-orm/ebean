package io.ebeaninternal.server.transaction;

import io.ebean.config.ProfilingConfig;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DefaultProfileHandlerTest {

  @Test
  public void createProfileStream() {

    ProfilingConfig profilingConfig = new ProfilingConfig();
    profilingConfig.setDirectory("target/profiling");

    DefaultProfileHandler handler = new DefaultProfileHandler(profilingConfig);

    assertNotNull(handler.createProfileStream(12));
    assertNull(handler.createProfileStream(0));
  }

  @Test
  public void createProfileStream_when_specificIncludeIds() {

    ProfilingConfig config = new ProfilingConfig();
    config.setDirectory("target/profiling");
    config.setIncludeProfileIds(new int[]{100,101});

    DefaultProfileHandler handler = new DefaultProfileHandler(config);

    assertNotNull(handler.createProfileStream(100));
    assertNotNull(handler.createProfileStream(101));

    assertNull(handler.createProfileStream(0));
    assertNull(handler.createProfileStream(12));

  }

}
