package io.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrmUpdatePropertiesTest {

  @Test
  public void trim() {
    assertEquals("ship_id", OrmUpdateProperties.trim("${}ship_id"));
    assertEquals("(ship_id)", OrmUpdateProperties.trim("(${}ship_id)"));
  }
}
