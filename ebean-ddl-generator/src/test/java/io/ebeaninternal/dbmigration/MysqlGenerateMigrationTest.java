package io.ebeaninternal.dbmigration;

import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class to test the alternative drop behaviour using stored procedures for MySql databases .
 *
 * @author Jonas P&ouml;hler, FOCONIS AG
 */
public class MysqlGenerateMigrationTest {

  @Test
  public void testMysqlStoredProcedures() throws Exception {
    
    
    String pathToResources = "src/test/resources";
    
    DatabaseConfig config = new DatabaseConfig();
    config.setName("migrationtest");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getPlatformConfig().setUseMigrationStoredProcedures(true);
    config.setRunMigration(false);
    config.getProperties().put("ebean.migrationtest.migration.pathToResources", pathToResources);
    config.getProperties().put("ebean.migrationtest.migration.platforms", "mysql");
    config.getProperties().put("ebean.migrationtest.migration.migrationPath", "dbmigration/migrationtest-procedures/");
    config.setPackages(Arrays.asList("misc.migration.mysql_v1_0"));

    // First, we clean up the output-directory
    Path path = Paths.get(pathToResources , "dbmigration","migrationtest-procedures");
    Files.walk(path)
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
    
    DatabaseFactory.create(config).shutdown();

    // then we generate migration scripts for v1_0
    assertThat(DbMigrationPlugin.getLastMigration()).isEqualTo("1.0__initial");
    assertThat(DbMigrationPlugin.getLastInit()).isEqualTo("1.0__initial");
    
  
    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.mysql_v1_1"));
    DatabaseFactory.create(config).shutdown();
    assertThat(DbMigrationPlugin.getLastMigration()).isEqualTo("1.1,1.2__dropsFor_1.1");
    assertThat(DbMigrationPlugin.getLastInit()).isEqualTo("1.2");
    
    final Path sqlFile = path.resolve("mysql/1.2__dropsFor_1.1.sql");

    assertThat(sqlFile).isNotEmptyFile();
    assertThat(Files.readAllLines(sqlFile, StandardCharsets.UTF_8))
        .contains("CALL usp_ebean_drop_column('migtest_e_basic', 'status2');")
        .contains("CALL usp_ebean_drop_column('migtest_e_basic', 'description');");
  }
}
