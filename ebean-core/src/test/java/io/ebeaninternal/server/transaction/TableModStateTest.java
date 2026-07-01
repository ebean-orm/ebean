package io.ebeaninternal.server.transaction;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableModStateTest {

  private final TableModState tableModState = new TableModState();

  @Test
  void isValid() {
    Instant before = Instant.now();

    tableModState.touch(setOf("one", "two", "three"));

    Instant after = Instant.now();

    // empty
    assertTrue(tableModState.isValid(Collections.emptySet(), before));

    // no entry
    assertTrue(tableModState.isValid(setOf("noEntry"), before));

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
