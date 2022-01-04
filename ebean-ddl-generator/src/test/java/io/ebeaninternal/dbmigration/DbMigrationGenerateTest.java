package io.ebeaninternal.dbmigration;

import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * This is the Migrationscript generator. It generates 3 migrationscript for the models
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DbMigrationGenerateTest {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationGenerateTest.class);

  public static void main(String[] args) throws IOException {
    run("ebean-ddl-generator/src/test/resources");
  }

  @Test
  public void invokeTest() throws IOException {
    run("src/test/resources");
  }

  public static void run(String pathToResources) throws IOException {
    logger.info("start current directory: " + new File(".").getAbsolutePath());

    DatabaseConfig config = new DatabaseConfig();
    config.setName("migrationtest");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getProperties().put("ebean.migrationtest.migration.pathToResources", pathToResources);
    config.setPackages(Arrays.asList("misc.migration.v1_0"));

    // First, we clean up the output-directory
    Files.walk(Paths.get(pathToResources, "dbmigration","migrationtest"))
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
    
    DatabaseFactory.create(config).shutdown();

    // then we generate migration scripts for v1_0
    assertThat(DbMigrationPlugin.getLastMigration()).isEqualTo("1.0__initial");
    assertThat(DbMigrationPlugin.getLastInit()).isEqualTo("1.0__initial");
    
    // and we check repeatative calls
    DatabaseFactory.create(config).shutdown();
    assertThat(DbMigrationPlugin.getLastMigration()).isNull();
    assertThat(DbMigrationPlugin.getLastInit()).isNull();
  
    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.v1_1"));
    DatabaseFactory.create(config).shutdown();
    assertThat(DbMigrationPlugin.getLastMigration()).isEqualTo("1.1,1.2__dropsFor_1.1");
    assertThat(DbMigrationPlugin.getLastInit()).isEqualTo("1.2");

    // subsequent call
    DatabaseFactory.create(config).shutdown();
    assertThat(DbMigrationPlugin.getLastMigration()).isNull();
    assertThat(DbMigrationPlugin.getLastInit()).isNull();
    
    // and now for v1_2
    config.setPackages(Arrays.asList("misc.migration.v1_2"));
    DatabaseFactory.create(config).shutdown();
    assertThat(DbMigrationPlugin.getLastMigration()).isEqualTo("1.3,1.4__dropsFor_1.3");
    assertThat(DbMigrationPlugin.getLastInit()).isEqualTo("1.4");

    logger.info("end");
  }

}
