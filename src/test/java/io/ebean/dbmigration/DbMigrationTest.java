package io.ebean.dbmigration;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Platform;
import io.ebean.config.ServerConfig;
import io.ebean.config.TenantMode;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public class DbMigrationTest extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationTest.class);

  //@Ignore
  @Test
  public void writeCurrent() throws IOException {

    logger.info("start");

    DbMigration migration = new DbMigration();

    // We use src/test/resources as output directory (so we see in GIT if files will change)
    
    migration.setPathToResources("src/test/resources");

    // TODO enable other platforms, too
    migration.addPlatform(Platform.MYSQL, "mysql");
    migration.addPlatform(Platform.POSTGRES, "postgres");
    migration.addPlatform(Platform.SQLSERVER, "sqlserver");
    migration.addPlatform(Platform.H2, "h2");
    migration.addPlatform(Platform.DB2, "db2");
    migration.addPlatform(Platform.ORACLE, "oracle");

    ServerConfig config = new ServerConfig();
    config.setName("migrationtest");
    config.loadFromProperties();

    config.setPackages(Arrays.asList("misc.migration.v1_0"));
    EbeanServer server = EbeanServerFactory.create(config);
    migration.setServer(server);

    // First, we clean up the output-directory
    assertThat(migration.getMigrationDirectory().getAbsolutePath()).contains("migrationtest");
    Files.walk(migration.getMigrationDirectory().toPath())
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

    // then we generate migration scripts for v1_0
    migration.generateMigration();
    
    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.v1_1"));
    server = EbeanServerFactory.create(config);
    migration.setServer(server);
    migration.generateMigration();

    
    logger.info("end");
  }

}
