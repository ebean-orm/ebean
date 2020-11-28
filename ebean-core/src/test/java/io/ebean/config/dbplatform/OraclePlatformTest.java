package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OraclePlatformTest {

  @Test
  public void uuid_default() {

    OraclePlatform platform = new OraclePlatform();
    platform.configure(new PlatformConfig(), false);
    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);

    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar2(40)");
  }


  @Test
  public void uuid_as_binary() {

    OraclePlatform platform = new OraclePlatform();
    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.AUTO_BINARY);

    platform.configure(config, false);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("raw(16)");
  }
}
