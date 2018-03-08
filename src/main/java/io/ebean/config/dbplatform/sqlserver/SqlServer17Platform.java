package io.ebean.config.dbplatform.sqlserver;

import io.ebean.annotation.Platform;

/**
 * Microsoft SQL Server platform - NVarchar UTF types and Sequence preference.
 */
public class SqlServer17Platform extends SqlServerBasePlatform {

  public SqlServer17Platform() {
    super();
    this.platform = Platform.SQLSERVER17;
    this.columnAliasPrefix = null;
  }

}
