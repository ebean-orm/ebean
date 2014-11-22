package com.avaje.ebeaninternal.api;

import com.avaje.ebean.SqlUpdate;

public interface SpiSqlUpdate extends SqlUpdate {

	public BindParams getBindParams();

  public void setGeneratedSql(String sql);
}
