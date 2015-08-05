package com.avaje.ebean.config.dbplatform;

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

}