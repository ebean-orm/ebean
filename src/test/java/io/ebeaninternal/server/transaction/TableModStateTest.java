package io.ebeaninternal.server.transaction;

import io.ebeaninternal.server.core.ClockService;
import org.junit.Test;

import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TableModStateTest {

  private TableModState tableModState = new TableModState(new ClockService(Clock.systemUTC()));

  @Test
  public void isValid() {

    long now = System.currentTimeMillis();

    tableModState.touch(setOf("one", "two", "three"), now);

    // empty
    assertTrue(tableModState.isValid(Collections.emptySet(), 12L));

    // no entry
    assertTrue(tableModState.isValid(setOf("noEntry"), 12L));

    // later timestamp
    assertTrue(tableModState.isValid(setOf("one"), now + 1));
    assertTrue(tableModState.isValid(setOf("one", "two", "noEntry"), now + 1));

    // invalid
    assertFalse(tableModState.isValid(setOf("one"), now - 1));
    assertFalse(tableModState.isValid(setOf("one", "two"), now - 1));
    assertFalse(tableModState.isValid(setOf("three", "two"), now - 1));

  }

  private Set<String> setOf(String... tables) {
    Set<String> touched = new HashSet<>();
    Collections.addAll(touched, tables);
    return touched;
  }
}
