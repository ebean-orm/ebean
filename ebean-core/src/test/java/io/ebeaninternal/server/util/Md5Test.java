package io.ebeaninternal.server.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class Md5Test {

  @Test
  public void hash() throws Exception {
    String content = "some random content we wish to hash";
    String hash1 = Md5.hash(content);
    String hash2 = Md5.hash(content);
    assertEquals(hash1, hash2);
    assertEquals(hash1, "62c20bf679ff56cb746452ab5c88e3ed");
  }

  @Test
  public void hashDifferent() throws Exception {
    String hash1 = Md5.hash("one");
    String hash2 = Md5.hash("two");
    String hash3 = Md5.hash("onetwo");

    assertNotEquals(hash1, hash2);
    assertNotEquals(hash2, hash3);
    assertEquals(hash1, "f97c5d29941bfb1b2fdab0874906ab82");
  }

  @Test
  public void hashMulti() {
    String hash1 = Md5.hash("one", "two");
    String hash2 = Md5.hash("onetwo");

    assertEquals(hash1, hash2);
    assertEquals(hash1, "5b9164ad6f496d9dee12ec7634ce253f");
  }

  @Test
  public void hashMulti_when_null() {
    String hash1 = Md5.hash("one", null);
    String hash2 = Md5.hash("one");

    assertEquals(hash1, hash2);
    assertEquals(hash1, "f97c5d29941bfb1b2fdab0874906ab82");
  }

  @Test
  public void when_null() {
    String hash1 = Md5.hash(null, null);
    assertEquals(hash1, "d41d8cd98f00b204e9800998ecf8427e");
  }
}
