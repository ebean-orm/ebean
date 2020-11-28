package io.ebeaninternal.dbmigration;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultDbMigrationTest {

  private final DefaultDbMigration migration = new DefaultDbMigration();

  @Test
  public void trimDropsFor() {
    assertEquals("1.2", migration.trimDropsFor("V1.2__hello"));
    assertEquals("1.2", migration.trimDropsFor("v1.2__hello"));
    assertEquals("1.2", migration.trimDropsFor("v1.2"));
    assertEquals("junk1.2", migration.trimDropsFor("junk1.2"));
    assertEquals("junk1.2", migration.trimDropsFor("junk1.2__"));
    assertEquals("junk1.2", migration.trimDropsFor("junk1.2__more"));
  }
}
