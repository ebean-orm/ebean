package io.ebeaninternal.server.persist.platform;

import io.ebeaninternal.server.type.ScalarType;

/**
 * Multi value binder that uses SqlServers Table-value parameters
 * @author Roland Praml, FOCONIS AG
 *
 */
public class H2MultiValueBind extends AbstractMultiValueBind {

  @Override
  public String getInExpression(ScalarType<?> type, int size) {
    String arrayType = getArrayType(type.getJdbcType());
    if (arrayType == null) {
      return super.getInExpression(type, size);
    } else {
      StringBuilder sb = new StringBuilder(50);
      sb.append(" in (select * from table(x ").append(arrayType).append(" = ?)) ");
      return sb.toString();
    }
  }
}
