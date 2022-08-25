package io.ebean.platform.mysql;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DbPlatformType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlPlatformTest {

  @Test
  public void uuid_default() {

    MySqlPlatform platform = new MySqlPlatform();
    platform.configure(new PlatformConfig());

    DbPlatformType dbType = platform.dbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar(40)");
  }


  @Test
  public void uuid_as_binary() {

    MySqlPlatform platform = new MySqlPlatform();
    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.AUTO_BINARY);
    platform.configure(config);

    DbPlatformType dbType = platform.dbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("binary(16)");
  }

}
