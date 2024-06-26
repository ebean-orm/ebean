package io.ebean.xtest.config.dbplatform;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbPlatformTypeMapping;
import io.ebean.platform.clickhouse.ClickHousePlatform;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.oracle.OraclePlatform;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebean.platform.sqlserver.SqlServer16Platform;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DbTypeMapTest {

  final DbPlatformTypeMapping logicalTypeMap = DbPlatformTypeMapping.logicalTypes();

  @Test
  void testLookupRender_given_postgresPlatformType() {
    PostgresPlatform pg = new PostgresPlatform();
    DbPlatformTypeMapping dbTypeMap = pg.dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("varchar(20)");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("timestamptz");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("timestamp");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("json");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("jsonb");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("bytea");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("varchar(200)");
  }

  @Test
  void testLookupRender_given_mysql() {
    DbPlatformTypeMapping dbTypeMap = new MySqlPlatform().dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("longtext");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("longtext");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("varchar(20)");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("datetime(6)");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("datetime(6)");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("json");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("json");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("longtext");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("longblob");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("varchar(200)");
  }

  @Test
  void testLookupRender_given_sqlserver17() {
    DbPlatformTypeMapping dbTypeMap = new SqlServer17Platform().dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("nvarchar(max)");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("nvarchar(max)");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("nvarchar(20)");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("datetime2");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("datetime2");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("nvarchar(max)");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("nvarchar(max)");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("nvarchar(max)");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("varbinary(max)");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("nvarchar(200)");
  }

  @Test
  void testLookupRender_given_sqlserver16() {
    DbPlatformTypeMapping dbTypeMap = new SqlServer16Platform().dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("varchar(20)");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("datetime2");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("datetime2");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("text");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("image");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("varchar(200)");
  }

  @Test
  void testLookupRender_given_oracle() {
    DbPlatformTypeMapping dbTypeMap = new OraclePlatform().dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("varchar2(20)");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("timestamp");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("timestamp");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("blob");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("varchar2(200)");
  }

  @Test
  void testLookupRender_given_h2() {
    DbPlatformTypeMapping dbTypeMap = new H2Platform().dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("varchar(20)");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("timestamp");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("timestamp");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("clob");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("blob");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("varchar(200)");
  }

  @Test
  void testLookupRender_given_clickhouse() {
    DbPlatformTypeMapping dbTypeMap = new ClickHousePlatform().dbTypeMap();

    assertThat(dbTypeMap.lookup("clob", false).renderType(0, 0)).isEqualTo("String");
    assertThat(dbTypeMap.lookup("CLOB", false).renderType(0, 0)).isEqualTo("String");
    assertThat(dbTypeMap.lookup("varchar", true).renderType(20, 0)).isEqualTo("String");
    assertThat(dbTypeMap.lookup("timestamp", false).renderType(0, 0)).isEqualTo("DateTime");
    assertThat(dbTypeMap.lookup("localdatetime", false).renderType(0, 0)).isEqualTo("DateTime");
    assertThat(dbTypeMap.lookup("json", false).renderType(0, 0)).isEqualTo("JSON");
    assertThat(dbTypeMap.lookup("jsonb", false).renderType(0, 0)).isEqualTo("JSON");
    assertThat(dbTypeMap.lookup("jsonclob", false).renderType(0, 0)).isEqualTo("String");
    assertThat(dbTypeMap.lookup("jsonblob", false).renderType(0, 0)).isEqualTo("blob");
    assertThat(dbTypeMap.lookup("jsonvarchar", false).renderType(200, 0)).isEqualTo("String");
  }

  @Test
  void testPlatformTypes_logical_json() {
    DbPlatformType dbType = logicalTypeMap.get(DbPlatformType.JSON);
    DbPlatformType json = logicalTypeMap.lookup("json", false);
    assertThat(dbType).isSameAs(json);
  }

  @Test
  void testPlatformTypes_logical_localDateTime() {
    DbPlatformType dbType = logicalTypeMap.get(DbPlatformType.LOCALDATETIME);
    DbPlatformType localDateTime = logicalTypeMap.lookup("localdatetime", false);
    assertThat(dbType).isSameAs(localDateTime);
  }
}
