package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OraclePlatformTest {

  OraclePlatform platform = new OraclePlatform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob", false)).isEqualTo("clob");
    assertThat(ddl.convert("blob", false)).isEqualTo("blob");
    assertThat(ddl.convert("json", false)).isEqualTo("clob");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("clob");

    assertThat(ddl.convert("double", false)).isEqualTo("number(19,4)");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("varchar2(20)");
    assertThat(ddl.convert("decimal(10)", false)).isEqualTo("number(10)");
    assertThat(ddl.convert("decimal(8,4)", false)).isEqualTo("number(8,4)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("number(1)");
    assertThat(ddl.convert("bit", false)).isEqualTo("bit");
    assertThat(ddl.convert("tinyint", false)).isEqualTo("number(3)");
    assertThat(ddl.convert("binary(16)", false)).isEqualTo("raw(16)");
  }

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
