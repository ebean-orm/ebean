package io.ebeaninternal.server.transaction;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TableModStateTest {

  private TableModState tableModState = new TableModState();

  @Test
  public void isValid() {

    long before = System.nanoTime();

    tableModState.touch(setOf("one", "two", "three"));

    long after = System.nanoTime();

    // empty
    assertTrue(tableModState.isValid(Collections.emptySet(), 12L));

    // no entry
    assertTrue(tableModState.isValid(setOf("noEntry"), 12L));

    // later timestamp
    assertTrue(tableModState.isValid(setOf("one"), after));
    assertTrue(tableModState.isValid(setOf("one", "two", "noEntry"), after));

    // invalid
    assertFalse(tableModState.isValid(setOf("one"), before));
    assertFalse(tableModState.isValid(setOf("one", "two"), before));
    assertFalse(tableModState.isValid(setOf("three", "two"), before));

  }

  private Set<String> setOf(String... tables) {
    Set<String> touched = new HashSet<>();
    Collections.addAll(touched, tables);
    return touched;
  }
}
