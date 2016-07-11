package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OraclePlatformTest {

  OraclePlatform platform = new OraclePlatform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = platform.getPlatformDdl();

    assertThat(ddl.convert("clob", false)).isEqualTo("clob");
    assertThat(ddl.convert("blob", false)).isEqualTo("blob");
    assertThat(ddl.convert("json", false)).isEqualTo("clob");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("clob");

    assertThat(ddl.convert("double", false)).isEqualTo("number(19,4)");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("varchar2(20)");
    assertThat(ddl.convert("decimal(10)", false)).isEqualTo("number(10)");
    assertThat(ddl.convert("decimal(8,4)", false)).isEqualTo("number(8,4)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("number(1) default 0");
    assertThat(ddl.convert("bit", false)).isEqualTo("bit");
    assertThat(ddl.convert("tinyint", false)).isEqualTo("number(3)");

  }

  @Test
  public void uuid_default() {

    OraclePlatform platform = new OraclePlatform();
    platform.configure(new ServerConfig());
    DbType dbType = platform.getDbTypeMap().get(DbType.UUID);

    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar2(40)");
  }


  @Test
  public void uuid_as_binary() {

    OraclePlatform platform = new OraclePlatform();
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDbUuid(ServerConfig.DbUuid.AUTO_BINARY);

    platform.configure(serverConfig);

    DbType dbType = platform.getDbTypeMap().get(DbType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("binary(16)");
  }
}