package io.ebeaninternal.dbmigration;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlserverPlatformTest {

  SqlServer17Platform platform = new SqlServer17Platform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob")).isEqualTo("nvarchar(max)");
    assertThat(ddl.convert("blob")).isEqualTo("varbinary(max)");
    assertThat(ddl.convert("json")).isEqualTo("nvarchar(max)");
    assertThat(ddl.convert("jsonb")).isEqualTo("nvarchar(max)");

    assertThat(ddl.convert("double")).isEqualTo("float(32)");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("nvarchar(20)");
    assertThat(ddl.convert("decimal(10)")).isEqualTo("numeric(10)");
    assertThat(ddl.convert("decimal(8,4)")).isEqualTo("numeric(8,4)");
    assertThat(ddl.convert("decimal")).isEqualTo("numeric(16,3)");
    assertThat(ddl.convert("boolean")).isEqualTo("bit");
    assertThat(ddl.convert("bit")).isEqualTo("bit");
    assertThat(ddl.convert("tinyint")).isEqualTo("smallint");
    assertThat(ddl.convert("binary(16)")).isEqualTo("binary(16)");

    assertThat(ddl.convert("varchar")).isEqualTo("nvarchar(255)");
    assertThat(ddl.convert("varchar(4000)")).isEqualTo("nvarchar(4000)");
    assertThat(ddl.convert("varchar(4001)")).isEqualTo("nvarchar(max)");
  }

  @Test
  public void uuid_default() {

    platform.configure(new PlatformConfig());
    DbPlatformType dbType = platform.dbTypeMap().get(DbPlatformType.UUID);

    assertThat(dbType.renderType(0, 0)).isEqualTo("uniqueidentifier");
  }


  @Test
  public void uuid_as_binary() {

    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.BINARY);

    platform.configure(config);

    DbPlatformType dbType = platform.dbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("binary(16)");
  }

  @Test
  public void uuid_as_varchar() {

    PlatformConfig config = new PlatformConfig();
    config.setDbUuid(PlatformConfig.DbUuid.VARCHAR);

    platform.configure(config);

    DbPlatformType dbType = platform.dbTypeMap().get(DbPlatformType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("nvarchar(40)");
  }
}
