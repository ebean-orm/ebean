package io.ebeaninternal.server.deploy;

import io.ebean.SqlUpdate;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.expression.DefaultExpressionRequest;
import io.ebeaninternal.server.expression.IdInExpression;
import io.ebeaninternal.server.persist.DeleteMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntersectionRow {

  private final String tableName;

  private final BeanDescriptor<?> targetDescriptor;

  private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();

  private List<Object> excludeIds;
  private BeanDescriptor<?> excludeDescriptor;

  IntersectionRow(String tableName, BeanDescriptor<?> targetDescriptor) {
    this.tableName = tableName;
    this.targetDescriptor = targetDescriptor;
  }

  IntersectionRow(String tableName) {
    this.tableName = tableName;
    this.targetDescriptor = null;
  }

  /**
   * Set Id's to exclude. This is for deleting non-attached detail Id's.
   */
  void setExcludeIds(List<Object> excludeIds, BeanDescriptor<?> excludeDescriptor) {
    this.excludeIds = excludeIds;
    this.excludeDescriptor = excludeDescriptor;
  }

  public void put(String key, Object value) {
    values.put(key, value);
  }

  public SqlUpdate createInsert(SpiEbeanServer server) {

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

  public SqlUpdate createDelete(SpiEbeanServer server, DeleteMode deleteMode) {

    BindParams bindParams = new BindParams();

    StringBuilder sb = new StringBuilder();
    if (deleteMode.isHard()) {
      sb.append("delete from ").append(tableName);
    } else {
      sb.append("update ").append(tableName).append(" set ");
      sb.append(targetDescriptor.getSoftDeleteDbSet());
    }
    sb.append(" where ");

    int count = setBindParams(bindParams, sb);

    if (excludeIds != null) {
      IdInExpression idIn = new IdInExpression(excludeIds);

      DefaultExpressionRequest er = new DefaultExpressionRequest(excludeDescriptor);
      idIn.addSqlNoAlias(er);
      idIn.addBindValues(er);

      sb.append(" and not ( ");
      sb.append(er.getSql());
      sb.append(" ) ");

      List<Object> bindValues = er.getBindValues();
      for (Object bindValue : bindValues) {
        bindParams.setParameter(++count, bindValue);
      }
    }

    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }

  public SqlUpdate createDeleteChildren(SpiEbeanServer server) {

    BindParams bindParams = new BindParams();

    StringBuilder sb = new StringBuilder();
    sb.append("delete from ").append(tableName).append(" where ");

    setBindParams(bindParams, sb);

    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }

  private int setBindParams(BindParams bindParams, StringBuilder sb) {

    int count = 0;
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      if (count++ > 0) {
        sb.append(" and ");
      }

      sb.append(entry.getKey());
      sb.append(" = ?");

      bindParams.setParameter(count, entry.getValue());
    }

    return count;
  }
}
