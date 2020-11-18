package io.ebeaninternal.dbmigration;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HanaPlatformTest {

  HanaPlatform platform = new HanaPlatform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob")).isEqualTo("nclob");
    assertThat(ddl.convert("blob")).isEqualTo("blob");
    assertThat(ddl.convert("json")).isEqualTo("nclob");
    assertThat(ddl.convert("jsonb")).isEqualTo("nclob");
    assertThat(ddl.convert("jsonvarchar")).isEqualTo("nvarchar(255)");

    assertThat(ddl.convert("double")).isEqualTo("double");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("nvarchar(20)");
    assertThat(ddl.convert("decimal(10)")).isEqualTo("decimal(10)");
    assertThat(ddl.convert("decimal(8,4)")).isEqualTo("decimal(8,4)");
    assertThat(ddl.convert("decimal")).isEqualTo("decimal(16,3)");
    assertThat(ddl.convert("boolean")).isEqualTo("boolean");
    assertThat(ddl.convert("bit")).isEqualTo("smallint");
    assertThat(ddl.convert("tinyint")).isEqualTo("smallint");
    assertThat(ddl.convert("binary")).isEqualTo("varbinary(255)");
    assertThat(ddl.convert("binary(16)")).isEqualTo("varbinary(16)");

    assertThat(ddl.convert("point")).isEqualTo("st_point");

    assertThat(ddl.convert("multilinestring")).isEqualTo("st_geometry");
    assertThat(ddl.convert("multipolygon")).isEqualTo("st_geometry");
    assertThat(ddl.convert("multipoint")).isEqualTo("st_geometry");
    assertThat(ddl.convert("linestring")).isEqualTo("st_geometry");
    assertThat(ddl.convert("polygon")).isEqualTo("st_geometry");
  }

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
