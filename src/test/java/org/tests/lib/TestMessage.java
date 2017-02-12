package org.tests.lib;

import io.ebeaninternal.server.core.Message;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMessage {

  @Test
  public void testMessage() {

    String one = "one";
    String two = "two";

    String m = Message.msg("fetch.error", one, two);
    assertThat(m).contains("Query threw SQLException:one Query was:");
  }
}
