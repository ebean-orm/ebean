package io.ebeaninternal.dbmigration;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexMigrationTest {

  @Test
  public void index() throws IOException {
    File topDir = new File("src/test/resources/dbmigration/index");
    if (!topDir.exists()) {
      throw new IllegalStateException("Not expected - dir does not exist " + topDir.getAbsolutePath());
    }
    DatabasePlatform pg = new PostgresPlatform();
    IndexMigration indexMigration = new IndexMigration(topDir, pg);
    indexMigration.generate();


    File expected = new File(topDir, "idx_postgres.migrations");
    assertThat(expected).exists();

    final List<String> expectedLines = Arrays.asList(
      "-965417868,     I__init_1.sql",
      "907060870,      1.0__hello.sql",
      "-1938594527,    1.1__foo.sql",
      "-1960070312,    R__view_1.sql");

    final List<String> lines = Files.readAllLines(expected.toPath(), StandardCharsets.UTF_8);
    assertThat(lines).containsAll(expectedLines);
  }

  @Test
  public void index2_withSubDirectories() throws IOException {
    File topDir = new File("src/test/resources/dbmigration/index2");
    if (!topDir.exists()) {
      throw new IllegalStateException("Not expected - dir does not exist " + topDir.getAbsolutePath());
    }
    DatabasePlatform pg = new H2Platform();
    IndexMigration indexMigration = new IndexMigration(topDir, pg);
    indexMigration.generate();

    File expected = new File(topDir, "idx_h2.migrations");
    assertThat(expected).exists();

    final List<String> expectedLines = Arrays.asList(
      "-965417868,     I__init_1.sql",
      "-390611389,     g1/1.0__a.sql",
      "1908338681,     g1/1.1__b.sql",
      "-1776543936,    g2/2.0__a.sql",
      "253052666,      g2/2.1__2b.sql",
      "-1960070312,    R__view_1.sql");

    final List<String> lines = Files.readAllLines(expected.toPath(), StandardCharsets.UTF_8);
    assertThat(lines).containsAll(expectedLines);
  }
}
