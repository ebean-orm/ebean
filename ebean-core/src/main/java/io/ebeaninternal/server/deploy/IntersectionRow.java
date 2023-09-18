package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.expression.DefaultExpressionRequest;
import io.ebeaninternal.server.expression.IdInExpression;
import io.ebeaninternal.server.persist.DeleteMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class IntersectionRow {

  private final String tableName;
  private final BeanDescriptor<?> targetDescriptor;
  private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
  private Set<Object> excludeIds;
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
  void setExcludeIds(Set<Object> excludeIds, BeanDescriptor<?> excludeDescriptor) {
    this.excludeIds = excludeIds;
    this.excludeDescriptor = excludeDescriptor;
  }

  public void put(String key, Object value) {
    values.put(key, value);
  }

  public SpiSqlUpdate createInsert(SpiEbeanServer server) {
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
      sb.append('?');
    }
    sb.append(')');
    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }

  public SpiSqlUpdate createDelete(SpiEbeanServer server, DeleteMode deleteMode, String extraWhere) {
    BindParams bindParams = new BindParams();
    StringBuilder sb = new StringBuilder();
    if (deleteMode.isHard()) {
      sb.append("delete from ").append(tableName);
    } else {
      sb.append("update ").append(tableName).append(" set ");
      sb.append(targetDescriptor.softDeleteDbSet());
    }
    sb.append(" where ");
    int count = setBindParams(bindParams, sb);
    if (excludeIds != null) {
      IdInExpression idIn = new IdInExpression(excludeIds);
      DefaultExpressionRequest er = new DefaultExpressionRequest(excludeDescriptor);
      idIn.addSqlNoAlias(er);
      idIn.addBindValues(er);
      sb.append(" and not ( ");
      sb.append(er.sql());
      sb.append(" ) ");
      List<Object> bindValues = er.bindValues();
      for (Object bindValue : bindValues) {
        bindParams.setParameter(++count, bindValue);
      }
    }
    addExtraWhere(sb, extraWhere);

    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }


  public SpiSqlUpdate createDeleteChildren(SpiEbeanServer server, String extraWhere) {
    BindParams bindParams = new BindParams();
    StringBuilder sb = new StringBuilder();
    sb.append("delete from ").append(tableName).append(" where ");
    setBindParams(bindParams, sb);
    addExtraWhere(sb, extraWhere);
    return new DefaultSqlUpdate(server, sb.toString(), bindParams);
  }

  private void addExtraWhere(StringBuilder sb, String extraWhere) {
    if (extraWhere != null) {
      if (extraWhere.indexOf("${ta}") == -1) {
        // no table alias append ${mta} to query.
        sb.append(" and ").append(extraWhere.replace("${mta}", tableName));
      } else if (extraWhere.indexOf("${mta}") != -1) {
        // we have a table alias - this is not interesting for deletion.
        // but if have also a m2m table alias - this is a problem now!
        throw new UnsupportedOperationException("extraWhere \'" + extraWhere + "\' has both ${ta} and ${mta} - this is not yet supported");
      }
    }
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
