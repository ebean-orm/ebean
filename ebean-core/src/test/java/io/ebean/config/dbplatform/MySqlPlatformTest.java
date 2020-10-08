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
    assertThat(ddl.convert("clob")).isEqualTo("longtext");
    assertThat(ddl.convert("json")).isEqualTo("json");
    assertThat(ddl.convert("jsonb")).isEqualTo("json");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("varchar(20)");
    assertThat(ddl.convert("boolean")).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("bit")).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("decimal")).isEqualTo("decimal(16,3)");
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
