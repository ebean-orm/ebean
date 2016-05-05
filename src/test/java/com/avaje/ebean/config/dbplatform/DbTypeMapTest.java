package com.avaje.ebean.config.dbplatform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


public class DbTypeMapTest {

  @Test
  public void testLookupRender_given_postgresPlatformType() throws Exception {

    PostgresPlatform pg = new PostgresPlatform();
    DbTypeMap dbTypeMap = pg.getDbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0,0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("text");

    assertThat(dbTypeMap.lookup("varchar", true).renderType(20,0)).isEqualTo("varchar(20)");

    assertThat(dbTypeMap.lookup("json", false).renderType(0,0)).isEqualTo("json");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0,0)).isEqualTo("jsonb");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0,0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0,0)).isEqualTo("bytea");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200,0)).isEqualTo("varchar(200)");

  }

  @Test
  public void testPlatformTypes() {

    DbTypeMap dbTypeMap = DbTypeMap.logicalTypes();
    DbType dbType = dbTypeMap.get(DbType.JSON);
    DbType json = dbTypeMap.lookup("json", false);

    assertThat(dbType).isSameAs(json);
  }
}