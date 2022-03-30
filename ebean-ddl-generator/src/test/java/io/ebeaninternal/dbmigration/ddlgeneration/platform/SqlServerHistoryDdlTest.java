package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import org.junit.jupiter.api.Test;

import io.ebean.config.DatabaseConfig;
import io.ebean.platform.sqlserver.SqlServer17Platform;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlServerHistoryDdlTest {

  @Test
  public void getHistoryTable() {

    SqlServerHistoryDdl ddl = new SqlServerHistoryDdl();
    ddl.configure(new DatabaseConfig(), new SqlServerDdl(new SqlServer17Platform()));
    assertThat(ddl.historyTableWithSchema("foo")).isEqualTo("dbo.foo_history");
    assertThat(ddl.historyTableWithSchema("bar.foo")).isEqualTo("bar.foo_history");
    // test with reserved keywords in quotes
    assertThat(ddl.historyTableWithSchema("[select]")).isEqualTo("dbo.select_history");
    assertThat(ddl.historyTableWithSchema("\"select\"")).isEqualTo("dbo.select_history");
    assertThat(ddl.historyTableWithSchema("`select`")).isEqualTo("dbo.select_history");
  }
}
