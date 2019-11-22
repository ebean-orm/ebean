package org.tests.unitinternal;

import org.junit.Test;

import static io.ebean.util.EncodeB64.enc;
import static org.junit.Assert.assertEquals;

public class TestEncodeB64 {

  @Test
  public void test() {

    assertEquals("A", enc(0));
    assertEquals("B", enc(1));
    assertEquals("Z", enc(25));
    assertEquals("a", enc(26));
    assertEquals("z", enc(51));
    assertEquals("0", enc(52));
    assertEquals("9", enc(61));
    assertEquals("-", enc(62));
    assertEquals("_", enc(63));
    assertEquals("BA", enc(64));
    assertEquals("Bk", enc(100));
    assertEquals("B9", enc(125));
    assertEquals("B-", enc(126));
    assertEquals("B_", enc(127));
    assertEquals("CA", enc(128));
  }
}
