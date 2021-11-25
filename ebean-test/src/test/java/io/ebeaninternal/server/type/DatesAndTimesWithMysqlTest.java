package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Disabled;

@Disabled
public class DatesAndTimesWithMysqlTest extends DatesAndTimesTest {
  public DatesAndTimesWithMysqlTest() {
    platform = "mysql";
  }
}