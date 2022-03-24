package io.ebeaninternal.server.querydefn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrmUpdatePropertiesTest {

  @Test
  public void trim() {
    Assertions.assertEquals("ship_id", OrmUpdateProperties.trim("${}ship_id"));
    assertEquals("(ship_id)", OrmUpdateProperties.trim("(${}ship_id)"));
  }
}
