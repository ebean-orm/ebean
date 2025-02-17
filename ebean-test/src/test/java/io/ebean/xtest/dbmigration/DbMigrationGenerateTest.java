package io.ebean.xtest.dbmigration;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.Platform;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.DbOffline;

import io.ebean.dbmigration.DbMigration;
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
 * This is the Migrationscript generator. It will generate migrationscripts for all platforms.
 * It uses the packages 'misc.migration'
 * <ul>
 * <li><code>misc.migration.v1_0</code> Initial package.
 * <li><code>misc.migration.v1_1</code> modified v1_0 - every migration change, that ebean supports should be done here.
 * <li><code>misc.migration.v1_2</code> This is (nearly) the same as v1_0 and simulates every migration change in the other way
 * </ul>
 *
 * Changes in the migration scripts should be committed after they are reviewed (changes may be legitime or show, that something is broken).
 * The {@link DbMigrationTest} will execute the generated scripts for various platforms.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DbMigrationGenerateTest {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationGenerateTest.class);

  public static void main(String[] args) throws IOException {
    run("ebean-test/src/test/resources");
  }

  @Test
  public void invokeTest() throws IOException {
    run("src/test/resources");
  }

  public static void run(String pathToResources) throws IOException {
    logger.info("start current directory: " + new File(".").getAbsolutePath());


    // First, we clean up the output-directory
    Files.walk(Paths.get(pathToResources, "migrationtest"))
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

    DbMigration migration = DbMigration.create();
    migration.setIncludeIndex(true);
    // We use src/test/resources as output directory (so we see in GIT if files will change)
    migration.setPathToResources(pathToResources);

    migration.addPlatform(Platform.CLICKHOUSE);
    migration.addPlatform(Platform.COCKROACH);
    // for platforms like DB2-LUW, we specify the exact platform in the migration path
    // see https://github.com/ebean-orm/ebean-migration/issues/102
    migration.addPlatform(Platform.DB2LUW, "db2luw");
    migration.addPlatform(Platform.DB2FORI, "db2fori");
    migration.addPlatform(Platform.DB2ZOS, "db2zos");
    migration.addPlatform(Platform.DB2, "db2legacy");
    migration.addPlatform(Platform.GENERIC);
    migration.addPlatform(Platform.H2);
    migration.addPlatform(Platform.HANA);
    migration.addPlatform(Platform.HSQLDB);
    migration.addPlatform(Platform.MARIADB);
    migration.addPlatform(Platform.MARIADB, "mariadb-noprocs");
    migration.addPlatform(Platform.MYSQL);
    migration.addPlatform(Platform.MYSQL55, "mysql55");
    migration.addPlatform(Platform.NUODB);
    migration.addPlatform(Platform.ORACLE);
    migration.addPlatform(Platform.ORACLE11, "oracle11"); // uses sequences
    migration.addPlatform(Platform.POSTGRES);
    migration.addPlatform(Platform.POSTGRES9, "postgres9"); // different DDL as base!
    migration.addPlatform(Platform.SQLANYWHERE);
    migration.addPlatform(Platform.SQLITE);
    migration.addPlatform(Platform.SQLSERVER16, "sqlserver16");
    migration.addPlatform(Platform.SQLSERVER17, "sqlserver17");
    migration.addPlatform(Platform.YUGABYTE);


    DatabaseConfig config = new DatabaseConfig();
    config.setName("migrationtest");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getProperties().put("ebean.hana.generateUniqueDdl", "true"); // need to generate unique statements to prevent them from being filtered out as duplicates by the DdlRunner

    config.setPackages(Arrays.asList("misc.migration.v1_0"));
    Database server = createServer(config);
    migration.setServer(server);

    // then we generate migration scripts for v1_0
    assertThat(migration.generateMigration()).isEqualTo("1.0__initial");
    // and we check repeatative calls
    assertThat(migration.generateMigration()).isNull();

    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.v1_1"));
    server.shutdown();
    server = createServer(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.1,1.2__dropsFor_1.1");
    assertThat(migration.generateMigration()).isNull(); // subsequent call

    // and now for v1_2 with
    config.setPackages(Arrays.asList("misc.migration.v1_2"));
    server.shutdown();
    server = createServer(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.3,1.4__dropsFor_1.3");
    assertThat(migration.generateMigration()).isNull(); // subsequent call

    server.shutdown();
    logger.info("end");
  }

  private static Database createServer(DatabaseConfig config) {
    DbOffline.setGenerateMigration();
    Database server = DatabaseFactory.create(config);
    DbOffline.reset();
    return server;
  }

}
