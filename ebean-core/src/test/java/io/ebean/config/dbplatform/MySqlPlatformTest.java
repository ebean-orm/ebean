package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlPlatformTest {

  @Test
  public void uuid_default() {

    MySqlPlatform platform = new MySqlPlatform();
    platform.configure(new PlatformConfig(), false);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar(40)");
  }


  @Test
  public void uuid_as_binary() {

    MySqlPlatform platform = new MySqlPlatform();
    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.AUTO_BINARY);
    platform.configure(config, false);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("binary(16)");
  }

}
