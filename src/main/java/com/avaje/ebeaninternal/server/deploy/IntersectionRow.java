package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.expression.IdInExpression;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

public class IntersectionRow {

  private final String tableName;

  private final LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

  private ArrayList<Object> excludeIds;
  private BeanDescriptor<?> excludeDescriptor;

  public IntersectionRow(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Set Id's to exclude. This is for deleting non-attached detail Id's.
   */
  public void setExcludeIds(ArrayList<Object> excludeIds, BeanDescriptor<?> excludeDescriptor) {
    this.excludeIds = excludeIds;
    this.excludeDescriptor = excludeDescriptor;
  }

  public void put(String key, Object value) {
    values.put(key, value);
  }

  public SqlUpdate createInsert(EbeanServer server) {

    BindParams bindParams = new BindParams();

    StringBuilder sb = new StringBuilder();
    sb.append("insert into ").append(tableName).append(" (");

    int count = 0;
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      if (count++ > 0) {
        sb.append(", ");
      }
      sb.append(entry.getKey());
      bindParams.setParameter(count, entry.getValue());
    }

    sb.append(") values (");
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append("?");
    }
    sb.append(")");

    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }

  public SqlUpdate createDelete(EbeanServer server) {

    BindParams bindParams = new BindParams();

    StringBuilder sb = new StringBuilder();
    sb.append("delete from ").append(tableName).append(" where ");

    int count = 0;
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      if (count++ > 0) {
        sb.append(" and ");
      }
      sb.append(entry.getKey());
      sb.append(" = ?");
      bindParams.setParameter(count, entry.getValue());
    }

    if (excludeIds != null) {
      IdInExpression idIn = new IdInExpression(excludeIds);

      DefaultExpressionRequest er = new DefaultExpressionRequest(excludeDescriptor);
      idIn.addSqlNoAlias(er);
      idIn.addBindValues(er);

      sb.append(" and not ( ");
      sb.append(er.getSql());
      sb.append(" ) ");

      ArrayList<Object> bindValues = er.getBindValues();
      for (int i = 0; i < bindValues.size(); i++) {
        bindParams.setParameter(++count, bindValues.get(i));
      }
    }

    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }

  public SqlUpdate createDeleteChildren(EbeanServer server) {

    BindParams bindParams = new BindParams();

    StringBuilder sb = new StringBuilder();
    sb.append("delete from ").append(tableName).append(" where ");

    int count = 0;
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      if (count++ > 0) {
        sb.append(" and ");
      }

      sb.append(entry.getKey());
      sb.append(" = ?");

      bindParams.setParameter(count, entry.getValue());
    }

    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }
}
