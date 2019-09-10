package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlPlatformTest {

  MySqlPlatform mySqlPlatform = new MySqlPlatform();

  @Test
  public void testTypeConversion() {
    PlatformDdl ddl = PlatformDdlBuilder.create(mySqlPlatform);
    assertThat(ddl.convert("clob", false)).isEqualTo("longtext");
    assertThat(ddl.convert("json", false)).isEqualTo("longtext");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("longtext");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("varchar(20)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("bit", false)).isEqualTo("tinyint(1)");
  }

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
