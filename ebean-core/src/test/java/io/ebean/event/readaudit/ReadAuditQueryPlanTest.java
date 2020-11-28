package io.ebean.event.readaudit;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
