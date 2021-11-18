package io.ebeaninternal.dbmigration;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.PlatformConfig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class to test the alternative drop behaviour using stored procedures for MySql databases .
 *
 * @author Jonas P&ouml;hler, FOCONIS AG
 */
@Disabled("build success depends on test order")
public class MysqlGenerateMigrationTest {

  @Test
  public void testMysqlStoredProcedures() throws Exception {
    DefaultDbMigration migration = new DefaultDbMigration();
    migration.setIncludeIndex(true);
    // We use src/test/resources as output directory (so we see in GIT if files will change)
    migration.setPathToResources("src/test/resources");

    migration.addPlatform(Platform.MYSQL, "mysql");

    final PlatformConfig platformConfig = new PlatformConfig();
    platformConfig.setUseMigrationStoredProcedures(true);

    DatabaseConfig config = new DatabaseConfig();
    config.setName("migrationtest");
    config.loadFromProperties();
    config.setPlatformConfig(platformConfig);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getProperties().put("ebean.migration.migrationPath", "db/migration/mysql");

    config.setPackages(Arrays.asList("misc.migration.mysql_v1_0"));
    Database server = DatabaseFactory.create(config);
    migration.setServer(server);
    migration.setMigrationPath("mysql/procedures");

    // First, we clean up the output-directory
    assertThat(migration.migrationDirectory().getAbsolutePath()).contains("procedures");
    Files.walk(migration.migrationDirectory().toPath())
      .filter(Files::isRegularFile)
      .map(Path::toFile).forEach(File::delete);

    // then we generate migration scripts for v1_0
    assertThat(migration.generateMigration()).isEqualTo("1.0__initial");

    config.setPackages(Arrays.asList("misc.migration.mysql_v1_1"));
    server.shutdown();
    server = DatabaseFactory.create(config);
    migration.setServer(server);
    migration.setMigrationPath("mysql/procedures");
    assertThat(migration.generateMigration()).isEqualTo("1.1");

    System.setProperty("ddl.migration.pendingDropsFor", "1.1");
    assertThat(migration.generateMigration()).isEqualTo("1.2__dropsFor_1.1");

    final Path sqlFile = migration.migrationDirectory().toPath()
      .resolve("mysql/1.2__dropsFor_1.1.sql");

    assertThat(sqlFile).isNotEmptyFile();
    assertThat(Files.readAllLines(sqlFile, StandardCharsets.UTF_8))
      .contains("CALL usp_ebean_drop_column('migtest_e_basic', 'status2');")
      .contains("CALL usp_ebean_drop_column('migtest_e_basic', 'description');");

  }
}
