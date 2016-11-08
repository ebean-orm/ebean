package com.avaje.tests.lib;

import com.avaje.ebeaninternal.server.core.Message;
import org.junit.Assert;
import org.junit.Test;

public class TestMessage {

  @Test
  public void testMessage() {

    String one = "one";
    String two = "two";

    String m = Message.msg("fetch.error", one, two);
    boolean b = m.startsWith("Query threw SQLException:one Query was:");
    Assert.assertTrue(b);
  }
}
