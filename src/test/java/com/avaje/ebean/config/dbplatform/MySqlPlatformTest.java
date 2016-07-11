package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlPlatformTest {

  MySqlPlatform mySqlPlatform = new MySqlPlatform();

  @Test
  public void testTypeConversion() {
    PlatformDdl ddl = mySqlPlatform.getPlatformDdl();
    assertThat(ddl.convert("clob", false)).isEqualTo("longtext");
    assertThat(ddl.convert("json", false)).isEqualTo("longtext");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("longtext");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("varchar(20)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("tinyint(1) default 0");
    assertThat(ddl.convert("bit", false)).isEqualTo("tinyint(1) default 0");
  }

  @Test
  public void uuid_default() {

    MySqlPlatform platform = new MySqlPlatform();
    platform.configure(new ServerConfig());

    DbType dbType = platform.getDbTypeMap().get(DbType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("varchar(40)");
  }


  @Test
  public void uuid_as_binary() {

    MySqlPlatform platform = new MySqlPlatform();
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDbUuid(ServerConfig.DbUuid.AUTO_BINARY);
    platform.configure(serverConfig);

    DbType dbType = platform.getDbTypeMap().get(DbType.UUID);
    assertThat(dbType.renderType(0, 0)).isEqualTo("binary(16)");
  }

}