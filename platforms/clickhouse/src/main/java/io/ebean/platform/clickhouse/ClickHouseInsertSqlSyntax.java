package io.ebean.platform.clickhouse;

import io.ebean.config.dbplatform.InsertSqlSyntaxExtension;

public final class ClickHouseInsertSqlSyntax implements InsertSqlSyntaxExtension {

  // insert into mytable select col1, col2 from input('col1 String, col2 DateTime64(3), col3 Int32')

  @Override
  public String startColumns() {
    return " select ";
  }

  @Override
  public String endColumns() {
    return " from";
  }

  @Override
  public boolean useBinding() {
    return false;
  }

  @Override
  public String startTypes() {
    return " input('";
  }

  @Override
  public String endTypes() {
    return "')";
  }
}
