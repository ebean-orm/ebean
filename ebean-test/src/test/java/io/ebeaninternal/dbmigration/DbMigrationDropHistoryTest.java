package io.ebeaninternal.dbmigration;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * This is the Migrationscript generator. It generates 3 migrationscript for the models
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DbMigrationDropHistoryTest {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationDropHistoryTest.class);

  @Test
  public void invokeTest() throws IOException {
    main(null);
  }

  @Test
  public void lastVersion_no_v_Prefix() {
    File d = new File("src/test/resources/migrationtest-history/dbmigration");
    Assertions.assertThat(LastMigration.lastVersion(d, null)).isEqualTo("1.2");
  }

  public static void main(String[] args) throws IOException {

    logger.info("start");
    // First, we clean up the output-directory
    Files.walk(Paths.get("src/test/resources/migrationtest-history"))
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
    DefaultDbMigration migration = new DefaultDbMigration();

    // We use src/test/resources as output directory (so we see in GIT if files will change)

    migration.setPathToResources("src/test/resources");

    DatabaseConfig config = new DatabaseConfig();
    config.setName("migrationtest-history");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);


    config.setPackages(Arrays.asList("misc.migration.history.v1_0"));
    Database server = DatabaseFactory.create(config);
    migration.setServer(server);

    // First, we clean up the output-directory
    assertThat(migration.migrationDirectory().getAbsolutePath()).contains("migrationtest-history");
    Files.walk(migration.migrationDirectory().toPath())
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

    // then we generate migration scripts for v1_0
    assertThat(migration.generateMigration()).isEqualTo("1.0__initial");
    // and we check repeatative calls
    assertThat(migration.generateMigration()).isNull();

    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.history.v1_1"));
    server.shutdown();
    server = DatabaseFactory.create(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.1");
    assertThat(migration.generateMigration()).isNull(); // subsequent call


    List<String> pendingDrops = migration.getPendingDrops();
    assertThat(pendingDrops).contains("1.1");

    migration.setGeneratePendingDrop("1.1");
    assertThat(migration.generateMigration()).isEqualTo("1.2__dropsFor_1.1");
    assertThatThrownBy(()->migration.generateMigration())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No 'pendingDrops'"); // subsequent call

    server.shutdown();
    logger.info("end");
  }

}
