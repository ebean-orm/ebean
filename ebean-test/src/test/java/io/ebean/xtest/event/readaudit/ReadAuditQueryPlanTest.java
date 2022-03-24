package io.ebean.xtest.event.readaudit;


import io.ebean.event.readaudit.ReadAuditQueryPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ReadAuditQueryPlanTest {

  @Test
  public void testEquals() {

    ReadAuditQueryPlan plan1 = new ReadAuditQueryPlan("org.Bean", "queryKey", "select id from foo");

    assertEquals(plan1, new ReadAuditQueryPlan("org.Bean", "queryKey", "select id from foo"));
    assertNotEquals(plan1, new ReadAuditQueryPlan("org.Bean", "queryKey", "select id from Notfoo"));
    assertNotEquals(plan1, new ReadAuditQueryPlan("org.Bean", "notQueryKey", "select id from foo"));
    assertNotEquals(plan1, new ReadAuditQueryPlan("org.NotBean", "queryKey", "select id from foo"));
  }
}
