package io.ebeaninternal.server.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Md5Test {

  @Test
  public void hash() throws Exception {

    String content = "some random content we wish to hash";
    String hash1 = Md5.hash(content);
    String hash2 = Md5.hash(content);
    assertEquals(hash1, hash2);
  }

}
