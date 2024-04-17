package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.platform.clickhouse.ClickHousePlatform;
import io.ebeaninternal.dbmigration.migration.Column;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClickHouseDdlTest {

  private final ClickHouseDdl ddl = new ClickHouseDdl(new ClickHousePlatform());

  @Test
  void convert() {
    assertThat(ddl.convert("boolean")).isEqualTo("Bool");
    assertThat(ddl.convert("integer")).isEqualTo("UInt32");
    assertThat(ddl.convert("bigint")).isEqualTo("UInt64");
    assertThat(ddl.convert("decimal(20,3)")).isEqualTo("Decimal(20,3)");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("String");
    assertThat(ddl.convert("hstore")).isEqualTo("Map(String,String)");
    assertThat(ddl.convert("json")).isEqualTo("JSON");
    assertThat(ddl.convert("varchar[]")).isEqualTo("Array(String)");
  }

  @Test
  void columnDefnNullable() {
    Column column = new Column();
    column.setType("varchar(20)");
    column.setNotnull(null);
    assertThat(ddl.columnDefn(column)).isEqualTo("Nullable(String)");
  }

  @Test
  void columnDefnNullable2() {
    Column column = new Column();
    column.setType("varchar(20)");
    column.setNotnull(Boolean.FALSE);
    assertThat(ddl.columnDefn(column)).isEqualTo("Nullable(String)");
  }

  @Test
  void columnDefnNotNull() {
    Column column = new Column();
    column.setType("varchar(20)");
    column.setNotnull(Boolean.TRUE);
    assertThat(ddl.columnDefn(column)).isEqualTo("String");
  }

  @Test
  void columnDefnNotNullPrimaryKey() {
    Column column = new Column();
    column.setType("varchar(20)");
    column.setPrimaryKey(Boolean.TRUE);
    assertThat(ddl.columnDefn(column)).isEqualTo("String");
  }
}
