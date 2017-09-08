package io.ebeaninternal.server.persist.platform;

import java.sql.SQLException;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;
/**
 * Default implementation for multi value help.
 */
public class MultiValueHelp {
  /**
   * Default for multi values. They are appended one by one.
   */
  public void bindMultiValues(Binder binder, DataBind dataBind, Object[] values, int dbType) throws SQLException {
    for (Object value : values) {
      binder.bindObject(dataBind, value, dbType);
    }
  };
  
  /**
   * Appends the 'in' expression to the request. Must add trailing space!
   */
  public void appendInExpression(Binder binder, SpiExpressionRequest request, String propName, boolean not, Object[] bindValues) {
    request.append(propName);
    if (not) {
      request.append(" not");
    }
    request.append(" in (?");
    for (int i = 1; i < bindValues.length; i++) {
      request.append(", ").append("?");
    }
    request.append(" ) ");
  }
}
