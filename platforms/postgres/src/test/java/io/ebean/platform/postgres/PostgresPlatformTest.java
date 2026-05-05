package io.ebean.platform.postgres;

import io.ebean.annotation.Platform;
import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.test.containers.PostgresContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static io.ebean.Query.LockType.*;
import static io.ebean.Query.LockWait.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresPlatformTest {

  /**
   * Let's just run this test manually.
   */
  @Disabled
  @Test
  void platformDetection() throws SQLException {
    PostgresContainer container = PostgresContainer.builder("15")
      .containerName("pg15_ebeanTest")
      .dbName("unit")
      .port(0) // use a random port
      .build();

    container.startWithDropCreate();

    try (Connection connection = container.createConnection()) {
      connection.setAutoCommit(true);
      DatabaseMetaData metaData = connection.getMetaData();

      PostgresPlatformProvider platformProvider = new PostgresPlatformProvider();

      DatabasePlatform platform = platformProvider.create(15, 0, metaData, connection);
      assertThat(platform.name()).isEqualTo("postgres");

      connection.setAutoCommit(false);
      DatabasePlatform platform2 = platformProvider.create(15, 0, metaData, connection);
      assertThat(platform2.name()).isEqualTo("postgres");

    } finally {
      container.stopRemove();
    }
  }

  @Test
  void testStringType() {
    PostgresPlatform platform = new PostgresPlatform();
    platform.configure(new PlatformConfig());
    DbPlatformType dbType = platform.dbTypeMap().get(DbType.VARCHAR);
    String columnDefn = dbType.renderType(0, 0);

    assertThat(columnDefn).isEqualTo("varchar");
  }

  @Test
  void testUuidType() {
    PostgresPlatform platform = new PostgresPlatform();
    platform.configure(new PlatformConfig());
    DbPlatformType dbType = platform.dbTypeMap().get(DbPlatformType.UUID);
    String columnDefn = dbType.renderType(0, 0);

    assertThat(columnDefn).isEqualTo("uuid");
  }

  @Test
  void default_forUpdate_expect_noKeyUsed() {
    PostgresPlatform platform = new PostgresPlatform();

    PlatformConfig config = new PlatformConfig();
    platform.configure(config);

    assertThat(config.isForUpdateNoKey()).isFalse();
    assertThat(platform.withForUpdate("X", SKIPLOCKED, DEFAULT)).isEqualTo("X for update skip locked");
    assertThat(platform.withForUpdate("X", NOWAIT, DEFAULT)).isEqualTo("X for update nowait");
    assertThat(platform.withForUpdate("X", WAIT, DEFAULT)).isEqualTo("X for update");

    assertThat(platform.withForUpdate("X", SKIPLOCKED, UPDATE)).isEqualTo("X for update skip locked");
    assertThat(platform.withForUpdate("X", SKIPLOCKED, NO_KEY_UPDATE)).isEqualTo("X for no key update skip locked");
    assertThat(platform.withForUpdate("X", SKIPLOCKED, SHARE)).isEqualTo("X for share skip locked");
    assertThat(platform.withForUpdate("X", SKIPLOCKED, KEY_SHARE)).isEqualTo("X for key share skip locked");

    assertThat(platform.withForUpdate("X", NOWAIT, UPDATE)).isEqualTo("X for update nowait");
    assertThat(platform.withForUpdate("X", NOWAIT, NO_KEY_UPDATE)).isEqualTo("X for no key update nowait");
    assertThat(platform.withForUpdate("X", NOWAIT, SHARE)).isEqualTo("X for share nowait");
    assertThat(platform.withForUpdate("X", NOWAIT, KEY_SHARE)).isEqualTo("X for key share nowait");

    assertThat(platform.withForUpdate("X", WAIT, UPDATE)).isEqualTo("X for update");
    assertThat(platform.withForUpdate("X", WAIT, NO_KEY_UPDATE)).isEqualTo("X for no key update");
    assertThat(platform.withForUpdate("X", WAIT, SHARE)).isEqualTo("X for share");
    assertThat(platform.withForUpdate("X", WAIT, KEY_SHARE)).isEqualTo("X for key share");
  }

  @Test
  void lockWithKey_forUpdate() {
    PostgresPlatform platform = new PostgresPlatform();

    PlatformConfig config = new PlatformConfig();
    config.setForUpdateNoKey(true);
    platform.configure(config);

    assertThat(config.isForUpdateNoKey()).isTrue();
    assertThat(platform.withForUpdate("X", SKIPLOCKED, DEFAULT)).isEqualTo("X for no key update skip locked");
    assertThat(platform.withForUpdate("X", NOWAIT, DEFAULT)).isEqualTo("X for no key update nowait");
    assertThat(platform.withForUpdate("X", WAIT, DEFAULT)).isEqualTo("X for no key update");
  }

  @Test
  public void configure_customType() {
    PlatformConfig config = new PlatformConfig();
    config.addCustomMapping(DbType.VARCHAR, "text", Platform.POSTGRES);
    config.addCustomMapping(DbType.DECIMAL, "decimal(24,4)");

    // PG renders custom decimal and varchar
    PostgresPlatform pgPlatform = new PostgresPlatform();
    pgPlatform.configure(config);
    assertEquals(defaultDecimalDefn(pgPlatform), "decimal(24,4)");
    assertEquals(defaultDefn(DbType.VARCHAR, pgPlatform), "text");
  }

  private String defaultDecimalDefn(DatabasePlatform dbPlatform) {
    return defaultDefn(DbType.DECIMAL, dbPlatform);
  }

  private String defaultDefn(DbType type, DatabasePlatform dbPlatform) {
    return dbPlatform.dbTypeMap().get(type).renderType(0, 0);
  }

}
