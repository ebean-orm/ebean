package io.ebean.platform.oracle;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DbPlatformType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OraclePlatformTest {

  @Test
  void columnAliasPrefix_Oracle11Platform() {
    Oracle11Platform platform11 = new Oracle11Platform();
    assertThat(platform11.getColumnAliasPrefix()).isEqualTo("c");
  }

  @Test
  void columnAliasPrefix_Oracle12Platform() {
    Oracle12Platform platform12 = new Oracle12Platform();
    assertThat(platform12.getColumnAliasPrefix()).isEqualTo("c");
  }

  @Test
  void columnAliasPrefix_OraclePlatform() {
    OraclePlatform platform = new OraclePlatform();
    assertThat(platform.getColumnAliasPrefix()).isNull();
  }

  @Test
  void uuid_default() {
    OraclePlatform platform = new OraclePlatform();
    platform.configure(new PlatformConfig());
    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar2(40)");
  }

  @Test
  void uuid_as_binary() {
    OraclePlatform platform = new OraclePlatform();
    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.AUTO_BINARY);

    platform.configure(config);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("raw(16)");
  }
}
