package com.avaje.ebean.config.dbplatform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


public class DbTypeMapTest {

  @Test
  public void testLookupRender_given_postgresPlatformType() throws Exception {

    PostgresPlatform pg = new PostgresPlatform();
    DbTypeMap dbTypeMap = pg.getDbTypeMap();

    assertThat(dbTypeMap.lookup("clob").renderType(0,0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("CLOB").renderType(0, 0)).isEqualTo("text");

    assertThat(dbTypeMap.lookup("varchar").renderType(20,0)).isEqualTo("varchar(20)");

    assertThat(dbTypeMap.lookup("json").renderType(0,0)).isEqualTo("json");
    assertThat(dbTypeMap.lookup("jsonb").renderType(0,0)).isEqualTo("jsonb");
    assertThat(dbTypeMap.lookup("jsonclob").renderType(0,0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("jsonblob").renderType(0,0)).isEqualTo("bytea");
    assertThat(dbTypeMap.lookup("jsonvarchar").renderType(200,0)).isEqualTo("varchar(200)");

  }

  @Test
  public void testPlatformTypes() {

    DbTypeMap dbTypeMap = DbTypeMap.logicalTypes();
    DbType dbType = dbTypeMap.get(DbType.JSON);
    DbType json = dbTypeMap.lookup("json");

    assertThat(dbType).isSameAs(json);
  }
}