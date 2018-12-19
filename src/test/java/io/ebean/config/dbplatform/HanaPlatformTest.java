package io.ebean.config.dbplatform;

import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HanaPlatformTest {

  HanaPlatform platform = new HanaPlatform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob", false)).isEqualTo("nclob");
    assertThat(ddl.convert("blob", false)).isEqualTo("blob");
    assertThat(ddl.convert("json", false)).isEqualTo("nclob");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("nclob");
    assertThat(ddl.convert("jsonvarchar", false)).isEqualTo("nvarchar(255)");

    assertThat(ddl.convert("double", false)).isEqualTo("double");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("nvarchar(20)");
    assertThat(ddl.convert("decimal(10)", false)).isEqualTo("decimal(10)");
    assertThat(ddl.convert("decimal(8,4)", false)).isEqualTo("decimal(8,4)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("boolean");
    assertThat(ddl.convert("bit", false)).isEqualTo("smallint");
    assertThat(ddl.convert("tinyint", false)).isEqualTo("smallint");
    assertThat(ddl.convert("binary", false)).isEqualTo("varbinary(255)");
    assertThat(ddl.convert("binary(16)", false)).isEqualTo("varbinary(16)");
    
    assertThat(ddl.convert("point", false)).isEqualTo("st_point");
    
    assertThat(ddl.convert("multilinestring", false)).isEqualTo("st_geometry");
    assertThat(ddl.convert("multipolygon", false)).isEqualTo("st_geometry");
    assertThat(ddl.convert("multipoint", false)).isEqualTo("st_geometry");
    assertThat(ddl.convert("linestring", false)).isEqualTo("st_geometry");
    assertThat(ddl.convert("polygon", false)).isEqualTo("st_geometry");
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
