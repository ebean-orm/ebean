package io.ebeaninternal.server.transaction;

import io.ebean.config.ProfilingConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultProfileHandlerTest {
  @Test
  public void createProfileStream() throws Exception {

    DefaultProfileHandler handler = new DefaultProfileHandler(new ProfilingConfig());

    assertNotNull(handler.createProfileStream(12));
    assertNull(handler.createProfileStream(0));
  }

  @Test
  public void createProfileStream_when_specificIncludeIds() throws Exception {

    ProfilingConfig config = new ProfilingConfig();
    config.setIncludeProfileIds(new int[]{100,101});

    DefaultProfileHandler handler = new DefaultProfileHandler(config);

    assertNotNull(handler.createProfileStream(100));
    assertNotNull(handler.createProfileStream(101));

    assertNull(handler.createProfileStream(0));
    assertNull(handler.createProfileStream(12));

  }

}
