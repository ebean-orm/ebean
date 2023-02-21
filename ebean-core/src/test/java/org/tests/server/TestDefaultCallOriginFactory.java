package org.tests.server;

import io.ebean.bean.CallOrigin;
import io.ebeaninternal.server.core.CallOriginFactory;
import io.ebeaninternal.server.core.HelpDefaultCallOriginFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDefaultCallOriginFactory {

  private final CallOriginFactory factory = HelpDefaultCallOriginFactory.create(2);

  @Test
  void createCallOrigin() {
    CallOrigin callOrigin = inner();
    String topElement = callOrigin.getTopElement();
    assertThat(topElement).contains("org.tests.server.TestDefaultCallOriginFactory.inner(TestDefaultCallOriginFactory.java:23)");
    assertThat(callOrigin.getFullDescription()).contains("TestDefaultCallOriginFactory.java:16");
  }

  private CallOrigin inner() {
    return factory.createCallOrigin();
  }
}
