package io.ebeaninternal.dbmigration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultDbMigrationTest {

  private final DefaultDbMigration migration = new DefaultDbMigration();

  @Test
  void trimDropsFor() {
    assertEquals("1.2", migration.trimDropsFor("V1.2__hello"));
    assertEquals("1.2", migration.trimDropsFor("v1.2__hello"));
    assertEquals("1.2", migration.trimDropsFor("v1.2"));
    assertEquals("junk1.2", migration.trimDropsFor("junk1.2"));
    assertEquals("junk1.2", migration.trimDropsFor("junk1.2__"));
    assertEquals("junk1.2", migration.trimDropsFor("junk1.2__more"));
  }

  @Test
  void checkDropVersion_when_matches_throwsIAE() {
    assertThrows(IllegalArgumentException.class, () -> migration.checkDropVersion("1.0", "1.0"));
  }

  @Test
  void checkDropVersion_ok() {
    migration.checkDropVersion("1.0", null);
    migration.checkDropVersion("1.0", "1.0.0");
    migration.checkDropVersion("1.0", "1.1");
  }
}
