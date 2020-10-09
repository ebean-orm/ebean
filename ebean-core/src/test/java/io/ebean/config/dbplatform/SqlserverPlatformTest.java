package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlserverPlatformTest {

  SqlServer17Platform platform = new SqlServer17Platform();

  @Test
  public void uuid_default() {

    platform.configure(new PlatformConfig());
    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);

    assertThat(dbType.renderType(0, 0)).isEqualTo("uniqueidentifier");
  }


  @Test
  public void uuid_as_binary() {

    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.BINARY);

    platform.configure(config);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("binary(16)");
  }

  @Test
  public void uuid_as_varchar() {

    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.VARCHAR);

    platform.configure(config);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("nvarchar(40)");
  }
}
