package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlserverPlatformTest {

  SqlServer17Platform platform = new SqlServer17Platform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob", false)).isEqualTo("nvarchar(max)");
    assertThat(ddl.convert("blob", false)).isEqualTo("image");
    assertThat(ddl.convert("json", false)).isEqualTo("nvarchar(max)");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("nvarchar(max)");

    assertThat(ddl.convert("double", false)).isEqualTo("float(32)");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("nvarchar(20)");
    assertThat(ddl.convert("decimal(10)", false)).isEqualTo("numeric(10)");
    assertThat(ddl.convert("decimal(8,4)", false)).isEqualTo("numeric(8,4)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("bit");
    assertThat(ddl.convert("bit", false)).isEqualTo("bit");
    assertThat(ddl.convert("tinyint", false)).isEqualTo("smallint");
    assertThat(ddl.convert("binary(16)", false)).isEqualTo("binary(16)");
  }

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
