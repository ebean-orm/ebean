package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.oracle.Oracle11Platform;
import io.ebean.config.dbplatform.oracle.Oracle12Platform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OraclePlatformTest {

  @Test
  void columnAliasPrefix_Oracle11Platform() {
    Oracle11Platform platform11 = new Oracle11Platform();
    assertThat(platform11.columnAliasPrefix).isEqualTo("c");
  }

  @Test
  void columnAliasPrefix_Oracle12Platform() {
    Oracle12Platform platform12 = new Oracle12Platform();
    assertThat(platform12.columnAliasPrefix).isEqualTo("c");
  }

  @Test
  void columnAliasPrefix_OraclePlatform() {
    OraclePlatform platform = new OraclePlatform();
    assertThat(platform.columnAliasPrefix).isNull();
  }

  @Test
  void uuid_default() {
    OraclePlatform platform = new OraclePlatform();
    platform.configure(new PlatformConfig(), false);
    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar2(40)");
  }

  @Test
  void uuid_as_binary() {
    OraclePlatform platform = new OraclePlatform();
    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.AUTO_BINARY);

    platform.configure(config, false);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("raw(16)");
  }
}
