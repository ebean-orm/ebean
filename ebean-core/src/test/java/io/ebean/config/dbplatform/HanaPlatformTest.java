package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HanaPlatformTest {

  HanaPlatform platform = new HanaPlatform();

  @Test
  public void uuid_default() {

    HanaPlatform platform = new HanaPlatform();
    platform.configure(new PlatformConfig());
    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);

    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar(40)");
  }

  @Test
  public void uuid_as_binary() {

    HanaPlatform platform = new HanaPlatform();
    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.AUTO_BINARY);

    platform.configure(config);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("varbinary(16)");
  }
}
