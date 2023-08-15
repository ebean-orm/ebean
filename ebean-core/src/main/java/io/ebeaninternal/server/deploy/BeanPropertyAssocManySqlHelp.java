package io.ebeaninternal.server.deploy;

import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.server.deploy.visitor.BaseTablePropertyVisitor;
import io.ebeaninternal.server.deploy.visitor.VisitProperties;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.util.Str;

import java.util.List;

class BeanPropertyAssocManySqlHelp<T> {

  private final BeanPropertyAssocMany<T> many;
  private final ExportedProperty[] exportedProperties;
  private final boolean hasJoinTable;
  private final BeanDescriptor<?> descriptor;
  private final String exportedPropertyBindProto;
  private final String deleteByParentIdSql;
  private final String deleteByParentIdInSql;
  private final String elementCollectionInsertSql;
  private final boolean idInExpandedForm;

  BeanPropertyAssocManySqlHelp(BeanPropertyAssocMany<T> many, ExportedProperty[] exportedProperties) {
    this.many = many;
    this.exportedProperties = exportedProperties;
    this.hasJoinTable = many.hasJoinTable();
    this.descriptor = many.descriptor();
    this.exportedPropertyBindProto = initExportedBindProto();
    this.idInExpandedForm = descriptor.idBinder().isIdInExpandedForm();

    String delStmt;
    if (hasJoinTable) {
      delStmt = "delete from " + many.inverseJoin.getTable() + " where ";
    } else {
      delStmt = "delete from " + many.targetTable() + " where ";
    }
    deleteByParentIdSql = delStmt + rawParentIdEQ("");
    deleteByParentIdInSql = delStmt;
    if (many.isElementCollection()) {
      elementCollectionInsertSql = elementCollectionInsert();
    } else {
      elementCollectionInsertSql = null;
    }
  }

  private String elementCollectionInsert() {
    final StringBuilder sb = new StringBuilder(200);
    sb.append("insert into ").append(many.targetTable()).append(" (");
    append(sb);

    Cols cols = new Cols(sb);
    VisitProperties.visit(many.targetDescriptor, cols);
    sb.append(") values (");
    appendBind(sb, exportedProperties.length, true);
    appendBind(sb, cols.colCount, false);
    sb.append(')');
    return sb.toString();
  }

  SpiSqlUpdate insertElementCollection() {
    return new DefaultSqlUpdate(elementCollectionInsertSql);
  }

  private static class Cols extends BaseTablePropertyVisitor {

    int colCount;
    private final StringBuilder sb;

    private Cols(StringBuilder sb) {
      this.sb = sb;
    }

    @Override
    public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {
      sb.append(',').append(p.dbColumn());
      colCount++;
    }

    @Override
    public void visitOneImported(BeanPropertyAssocOne<?> p) {
      // do nothing
    }

    @Override
    public void visitScalar(BeanProperty p, boolean allowNonNull) {
      sb.append(",").append(p.dbColumn());
      colCount++;
    }

    @Override
    public void visitEnd() {
      // do nothing
    }
  }

  String lazyFetchOrderBy(String fetchOrderBy) {
    // derive lazyFetchOrderBy
    final String fkTableAlias = hasJoinTable ? "int_" : "t0";
    final var sb = new StringBuilder(50);
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      // these fk columns are either on the intersection (int_) or base table (t0)
      sb.append(fkTableAlias).append('.').append(exportedProperties[i].getForeignDbColumn());
    }
    sb.append(", ").append(fetchOrderBy);
    return sb.toString().trim();
  }

  /**
   * Add a where clause to the query for a given list of parent Id's.
   */
  void addWhereParentIdIn(SpiQuery<?> query, List<Object> parentIds) {
    final String tableAlias = hasJoinTable ? "int_." : "t0.";
    if (hasJoinTable) {
      query.setM2MIncludeJoin(many.inverseJoin);
    }
    final String rawWhere = rawParentIdIN(tableAlias, parentIds.size());
    many.bindParentIdsIn(rawWhere, parentIds, query);
  }

  List<Object> findIdsByParentId(Object parentId, Transaction t, List<Object> excludeDetailIds, boolean hard) {
    final SpiEbeanServer server = descriptor.ebeanServer();
    final SpiQuery<?> query = many.newQuery(server);
    many.bindParentIdEq(rawParentIdEQ(""), parentId, query);
    if (hard) {
      query.setIncludeSoftDeletes();
    }
    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      query.where().not(query.getExpressionFactory().idIn(excludeDetailIds));
    }
    return server.findIds(query, t);
  }

  List<Object> findIdsByParentIdList(List<Object> parentIds, Transaction t, List<Object> excludeDetailIds, boolean hard) {
    final SpiEbeanServer server = descriptor.ebeanServer();
    final SpiQuery<?> query = many.newQuery(server);
    many.bindParentIdsIn(rawParentIdIN("", parentIds.size()), parentIds, query);
    if (hard) {
      query.setIncludeSoftDeletes();
    }
    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      query.where().not(query.getExpressionFactory().idIn(excludeDetailIds));
    }
    return server.findIds(query, t);
  }

  SpiSqlUpdate deleteByParentId(Object parentId) {
    final var sqlDelete = new DefaultSqlUpdate(deleteByParentIdSql);
    many.bindParentId(sqlDelete, parentId);
    return sqlDelete;
  }

  SpiSqlUpdate deleteByParentIdList(List<Object> parentIds) {
    final String rawWhere = rawParentIdIN("", parentIds.size());
    final String sql = Str.add(deleteByParentIdInSql, rawWhere);
    final var delete = new DefaultSqlUpdate(sql);
    many.bindParentIds(delete, parentIds);
    return delete;
  }

  private void appendBind(StringBuilder sb, int count, boolean skipComma) {
    for (int i = 0; i < count; i++) {
      if (!skipComma || i > 0) {
        sb.append(",");
      }
      sb.append('?');
    }
  }

  private void append(StringBuilder sb) {
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(exportedProperties[i].getForeignDbColumn());
    }
  }

  private String rawParentIdIN(String tableAlias, int size) {
    if (idInExpandedForm) {
      return rawParentIdExpanded(tableAlias, size);
    } else {
      return rawParentIdStandard(tableAlias, size);
    }
  }

  private String rawParentIdStandard(String tableAlias, int size) {
    if (descriptor.isSimpleId()) {
      return rawParentIdMultiBinder(tableAlias, size);
    }
    final var sb = new StringBuilder(100 + size * exportedPropertyBindProto.length());
    sb.append('(');
    for (int i = 0; i < exportedProperties.length; i++) {
      String fkColumn = exportedProperties[i].getForeignDbColumn();
      if (i > 0) {
        sb.append(',');
      }
      sb.append(tableAlias).append(fkColumn);
    }
    sb.append(") in (");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(exportedPropertyBindProto);
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * Potentially a MultiValue binder like Postgres ANY Array binding.
   */
  private String rawParentIdMultiBinder(String tableAlias, int size) {
    final String property = '(' + tableAlias + exportedProperties[0].getForeignDbColumn() + ')';
    final String inValueExpr = descriptor.idBinder().getIdInValueExpr(false, size);
    return property + inValueExpr;
  }

  private String rawParentIdExpanded(String tableAlias, int size) {
    final String proto = parentIdExpandedProto(tableAlias);
    final var result = new StringBuilder(size * (proto.length() + 2) + 10).append('(');
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        result.append(" or ");
      }
      result.append(proto);
    }
    return result.append(')').toString();
  }

  private String parentIdExpandedProto(String tableAlias) {
    final var sb = new StringBuilder(60).append('(');
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      sb.append(tableAlias).append(exportedProperties[i].getForeignDbColumn()).append("=?");
    }
    return sb.append(')').toString();
  }

  private String rawParentIdEQ(String tableAlias) {
    final var sb = new StringBuilder(80);
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      sb.append(tableAlias).append(exportedProperties[i].getForeignDbColumn()).append("=?");
    }
    return sb.toString();
  }

  private String initExportedBindProto() {
    if (exportedProperties.length == 1) {
      return "?";
    }
    final var sb = new StringBuilder(exportedProperties.length * 2 + 2);
    sb.append("(");
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('?');
    }
    sb.append(')');
    return sb.toString();
  }

}
