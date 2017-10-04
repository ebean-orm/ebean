package io.ebeaninternal.server.query;

import io.ebean.util.SplitName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SplitNameTest {

  @Test
  public void add() throws Exception {

    assertEquals(SplitName.add("a", "b"), "a.b");
    assertEquals(SplitName.add("a", "b.c"), "a.b.c");
  }

  @Test
  public void count() throws Exception {

    assertEquals(SplitName.count("a"), 0);
    assertEquals(SplitName.count("a.b"), 1);
    assertEquals(SplitName.count("a.b.c"), 2);
    assertEquals(SplitName.count("a.b.c.foo"), 3);
  }

  @Test
  public void parent() throws Exception {
    assertNull(SplitName.parent("a"));
    assertEquals(SplitName.parent("a.b"), "a");
    assertEquals(SplitName.parent("a.b.c"), "a.b");
    assertNull(SplitName.parent(null));
  }

  @Test
  public void split() throws Exception {

    String[] split = SplitName.split("a.b.c");
    assertEquals(split[0], "a.b");
    assertEquals(split[1], "c");
  }

  @Test
  public void begin_when_one() throws Exception {

    assertEquals(SplitName.begin("a"), "a");
  }

  @Test
  public void begin_when_both() throws Exception {

    assertEquals(SplitName.begin("a.b"), "a");
  }

  @Test
  public void begin_when_multi() throws Exception {

    assertEquals(SplitName.begin("a.b.c"), "a");
  }

  @Test
  public void splitBegin_when_both() throws Exception {

    String[] split = SplitName.splitBegin("a.b");
    assertEquals(split[0], "a");
    assertEquals(split[1], "b");
  }

  @Test
  public void splitBegin_when_bothPlus() throws Exception {

    String[] split = SplitName.splitBegin("a.b.c");
    assertEquals(split[0], "a");
    assertEquals(split[1], "b.c");
  }

  @Test
  public void splitBegin_when_one() throws Exception {

    String[] split = SplitName.splitBegin("a");
    assertEquals(split[0], "a");
    assertNull(split[1]);
  }
}
