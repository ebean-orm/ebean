package io.ebean;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DbPrimaryTest {

  @Test
  void testIsSkipPrimaryServer() {
    DbPrimary.setSkip(true);
    assertTrue(DbPrimary.isSkip());
    DbPrimary.setSkip(false);
    assertFalse(DbPrimary.isSkip());
  }

  @Test
  void testGetPrimaryServerName() {
    assertEquals("h2", DbPrimary.getDefaultServerName());
  }

}
