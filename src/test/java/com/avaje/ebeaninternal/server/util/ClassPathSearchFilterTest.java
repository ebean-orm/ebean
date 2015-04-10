package com.avaje.ebeaninternal.server.util;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;

public class ClassPathSearchFilterTest {

  @Test
  public void testContainedIn() throws Exception {

    HashSet<String> set = new HashSet<String>();
    set.add("the-rock");
    set.add("fooBar.jar");

    ClassPathSearchFilter filter = new ClassPathSearchFilter();
    assertTrue(filter.containedIn(set, "the-rock.jar"));
    assertTrue(filter.containedIn(set, "the-rock-1.0.1.jar"));
    assertTrue(filter.containedIn(set, "prefix-the-rock-1.0.1.jar"));
    assertTrue(filter.containedIn(set, "fooBar.jar"));
    assertFalse(filter.containedIn(set, "fooBar-1.3.jar"));

  }
}