package io.ebeaninternal.api;

import io.ebean.SqlUpdate;

public interface SpiSqlUpdate extends SqlUpdate {

  BindParams getBindParams();

  void setGeneratedSql(String sql);
}
