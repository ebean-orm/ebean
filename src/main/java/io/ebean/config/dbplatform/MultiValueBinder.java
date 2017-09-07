package io.ebean.config.dbplatform;

import java.sql.SQLException;

import io.ebeaninternal.server.type.DataBind;

public interface MultiValueBinder {

  void bindObjects(DataBind dataBind, Object[] values, int dbType) throws SQLException;

  String getPlaceholder(int length);

}
