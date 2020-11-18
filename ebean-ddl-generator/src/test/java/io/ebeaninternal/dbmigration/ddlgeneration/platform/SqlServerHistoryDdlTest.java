package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlServerHistoryDdlTest {

  @Test
  public void getHistoryTable() {

    SqlServerHistoryDdl ddl = new SqlServerHistoryDdl();
    assertThat(ddl.getHistoryTable("foo")).isEqualTo("dbo.foo_history");
    assertThat(ddl.getHistoryTable("bar.foo")).isEqualTo("bar.foo_history");
    assertThat(ddl.getHistoryTable("[Foo]")).isEqualTo("dbo.[Foo_history]");
  }
}
