package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.platform.postgres.PostgresPlatform;
import io.ebeaninternal.dbmigration.migration.Column;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresDdlTest {

  final PostgresDdl postgresDdl = new PostgresDdl(new PostgresPlatform());

  @Test
  void setLockTimeout() {
    final String sql = postgresDdl.setLockTimeout(5);
    assertThat(sql).isEqualTo("set lock_timeout = 5000");
  }

  @Test
  void sortColumns_noAdjustment() {
    List<Column> cols = postgresDdl.sortColumns(columns("integer", "bigint"));
    assertThat(cols).extracting("type").containsExactly("integer", "bigint");

    List<Column> colsReverse = postgresDdl.sortColumns(columns("bigint", "integer"));
    assertThat(colsReverse).extracting("type").containsExactly("bigint", "integer");
  }

  @Test
  void sortColumns_decimalVarchar() {
    List<Column> cols = postgresDdl.sortColumns(columns("decimal(1)", "varchar(1)", "varchar(2)", "int", "decimal(2)"));
    assertThat(cols).extracting("type").containsExactly("int", "decimal(1)", "decimal(2)", "varchar(1)", "varchar(2)");
  }

  @Test
  void sortColumns_varbinary() {
    List<Column> cols = postgresDdl.sortColumns(columns("decimal(1)", "varbinary(1)", "varbinary(2)", "int", "decimal(2)"));
    assertThat(cols).extracting("type").containsExactly("int", "decimal(1)", "decimal(2)", "varbinary(1)", "varbinary(2)");
  }

  @Test
  void sortColumns_json() {
    List<Column> cols = postgresDdl.sortColumns(columns("json", "jsonb", "int"));
    assertThat(cols).extracting("type").containsExactly("int", "json", "jsonb");
  }

  @Test
  void sortColumns_clobs() {
    List<Column> cols = postgresDdl.sortColumns(columns("clob", "blob", "longvarchar(1)", "int", "longvarbinary(2)"));
    assertThat(cols).extracting("type").containsExactly("int", "clob", "blob", "longvarchar(1)", "longvarbinary(2)");
  }

  private List<Column> columns(String... types) {
    int counter = 0;
    List<Column> cols = new ArrayList<>();
    for (String s : types) {
      Column col = new Column();
      col.setName("c" + counter++);
      col.setType(s);
      cols.add(col);
    }
    return cols;
  }
}
