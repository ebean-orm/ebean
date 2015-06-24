package com.avaje.ebeaninternal.api;

import com.avaje.ebean.SqlUpdate;

public interface SpiSqlUpdate extends SqlUpdate {

	BindParams getBindParams();

  void setGeneratedSql(String sql);
}
