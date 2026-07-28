package io.ebean.platform.db2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DB2PlatformTest {

  @Test
  void existsWithCaseWhen_trueForDB2() {
    DB2LuwPlatform platform = new DB2LuwPlatform();

    assertTrue(platform.existsWithCaseWhen());
    assertEquals(" from sysibm.sysdummy1", platform.existsFromClause());
  }
}
