package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Disabled;

@Disabled
public class DatesAndTimesWithPgTest extends DatesAndTimesTest {
  public DatesAndTimesWithPgTest() {
    platform = "pg";
  }
}