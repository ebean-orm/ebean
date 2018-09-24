package io.ebeaninternal.dbmigration;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.annotation.Platform;
import io.ebean.config.ServerConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * This is the Migrationscript generator. It generates 3 migrationscript for the models
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DbMigrationGenerateTest {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationGenerateTest.class);

  @Test
  public void invokeTest() throws IOException {
    main(null);
  }

  public static void main(String[] args) throws IOException {

    logger.info("start");

    DefaultDbMigration migration = new DefaultDbMigration();

    // We use src/test/resources as output directory (so we see in GIT if files will change)

    migration.setPathToResources("src/test/resources");


    // migration.addPlatform(Platform.GENERIC, "generic"); there is no ddl handler for generic
    // migration.addPlatform(Platform.SQLANYWHERE, "sqlanywhere"); and sqlanywhere
    migration.addPlatform(Platform.DB2, "db2");
    migration.addPlatform(Platform.H2, "h2");
    migration.addPlatform(Platform.HSQLDB, "hsqldb");
    migration.addPlatform(Platform.MYSQL, "mysql");
    migration.addPlatform(Platform.POSTGRES, "postgres");
    migration.addPlatform(Platform.ORACLE, "oracle");
    migration.addPlatform(Platform.SQLITE, "sqlite");
    migration.addPlatform(Platform.SQLSERVER17, "sqlserver17");

    ServerConfig config = new ServerConfig();
    config.setName("migrationtest");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);


    config.setPackages(Arrays.asList("misc.migration.v1_0"));
    EbeanServer server = EbeanServerFactory.create(config);
    migration.setServer(server);

    // First, we clean up the output-directory
    assertThat(migration.getMigrationDirectory().getAbsolutePath()).contains("migrationtest");
    Files.walk(migration.getMigrationDirectory().toPath())
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

    // then we generate migration scripts for v1_0
    assertThat(migration.generateMigration()).isEqualTo("1.0__initial");
    // and we check repeatative calls
    assertThat(migration.generateMigration()).isNull();

    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.v1_1"));
    server = EbeanServerFactory.create(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.1");
    assertThat(migration.generateMigration()).isNull(); // subsequent call



    System.setProperty("ddl.migration.pendingDropsFor", "1.1");
    assertThat(migration.generateMigration()).isEqualTo("1.2__dropsFor_1.1");

    assertThatThrownBy(()->migration.generateMigration())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No 'pendingDrops'"); // subsequent call

    System.clearProperty("ddl.migration.pendingDropsFor");
    assertThat(migration.generateMigration()).isNull(); // subsequent call

    // and now for v1_2 with
    config.setPackages(Arrays.asList("misc.migration.v1_2"));
    server = EbeanServerFactory.create(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.3");
    assertThat(migration.generateMigration()).isNull(); // subsequent call


    System.setProperty("ddl.migration.pendingDropsFor", "1.3");
    assertThat(migration.generateMigration()).isEqualTo("1.4__dropsFor_1.3");
    assertThatThrownBy(()->migration.generateMigration())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No 'pendingDrops'"); // subsequent call

    System.clearProperty("ddl.migration.pendingDropsFor");
    assertThat(migration.generateMigration()).isNull(); // subsequent call

    logger.info("end");
  }

}
