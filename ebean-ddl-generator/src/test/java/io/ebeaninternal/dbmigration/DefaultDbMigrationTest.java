package io.ebeaninternal.dbmigration;

import io.ebeaninternal.extraddl.model.DdlScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Test
  void writeExtraDdl_initScript_notOverwrittenWhenExists(@TempDir Path dir) throws IOException {
    File migrationDir = dir.toFile();

    migration.writeExtraDdl(migrationDir, initScript("partition help", "ORIGINAL"));
    File initFile = new File(migrationDir, "I__partition_help.sql");
    assertThat(initFile).exists().content().isEqualTo("ORIGINAL");

    // hand-tune the generated init script
    Files.writeString(initFile.toPath(), "CUSTOMISED");

    // regeneration must not clobber an existing init script
    migration.writeExtraDdl(migrationDir, initScript("partition help", "ORIGINAL"));
    assertThat(initFile).content().isEqualTo("CUSTOMISED");
  }

  @Test
  void writeExtraDdl_repeatableScript_alwaysRewritten(@TempDir Path dir) throws IOException {
    File migrationDir = dir.toFile();

    migration.writeExtraDdl(migrationDir, repeatableScript("my view", "V1"));
    File file = new File(migrationDir, "R__my_view.sql");
    assertThat(file).exists().content().isEqualTo("V1");

    Files.writeString(file.toPath(), "STALE");

    // repeatable scripts are regenerated from their source every time
    migration.writeExtraDdl(migrationDir, repeatableScript("my view", "V1"));
    assertThat(file).content().isEqualTo("V1");
  }

  private static DdlScript initScript(String name, String value) {
    return script(name, value, true);
  }

  private static DdlScript repeatableScript(String name, String value) {
    return script(name, value, false);
  }

  private static DdlScript script(String name, String value, boolean init) {
    DdlScript script = new DdlScript();
    script.setName(name);
    script.setValue(value);
    script.setInit(init);
    return script;
  }
}
