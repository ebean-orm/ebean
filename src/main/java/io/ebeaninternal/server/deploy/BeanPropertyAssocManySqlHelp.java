package io.ebeaninternal.server.deploy;

import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.dbmigration.model.visitor.BaseTablePropertyVisitor;
import io.ebeaninternal.dbmigration.model.visitor.VisitAllUsing;
import io.ebeaninternal.server.core.DefaultSqlUpdate;

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

  BeanPropertyAssocManySqlHelp(BeanPropertyAssocMany<T> many, ExportedProperty[] exportedProperties) {
    this.many = many;
    this.exportedProperties = exportedProperties;
    this.hasJoinTable = many.hasJoinTable();
    this.descriptor = many.getBeanDescriptor();
    this.exportedPropertyBindProto = deriveExportedPropertyBindProto();

    String delStmt;
    if (hasJoinTable) {
      delStmt = "delete from " + many.inverseJoin.getTable() + " where ";
    } else {
      delStmt = "delete from " + many.targetTable() + " where ";
    }
    deleteByParentIdSql = delStmt + deriveWhereParentIdSql(false, "");
    deleteByParentIdInSql = delStmt + deriveWhereParentIdSql(true, "");
    if (many.isElementCollection()) {
      elementCollectionInsertSql = elementCollectionInsert();
    } else {
      elementCollectionInsertSql = null;
    }
  }

  private String elementCollectionInsert() {

    StringBuilder sb = new StringBuilder(200);
    sb.append("insert into ").append(many.targetTable()).append(" (");
    append(sb, "", ",", "");

    Cols cols = new Cols(sb);
    VisitAllUsing.visitOne(many.targetDescriptor, cols);
    sb.append(") values (");
    appendBind(sb, exportedProperties.length, true);
    appendBind(sb, cols.colCount, false);
    sb.append(")");

    return sb.toString();
  }

  public String insertElementCollection() {
    return elementCollectionInsertSql;
  }

  private static class Cols extends BaseTablePropertyVisitor {

    int colCount;

    private final StringBuilder sb;

    private Cols(StringBuilder sb) {
      this.sb = sb;
    }

    @Override
    public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {
      sb.append(",").append(p.getDbColumn());
      colCount++;
    }

    @Override
    public void visitOneImported(BeanPropertyAssocOne<?> p) {

    }

    @Override
    public void visitScalar(BeanProperty p) {
      sb.append(",").append(p.getDbColumn());
      colCount++;
    }

    @Override
    public void visitEnd() {

    }
  }

  String lazyFetchOrderBy(String fetchOrderBy) {

    // derive lazyFetchOrderBy
    StringBuilder sb = new StringBuilder(50);
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      // these fk columns are either on the intersection (int_) or base table (t0)
      String fkTableAlias = hasJoinTable ? "int_" : "t0";
      sb.append(fkTableAlias).append(".").append(exportedProperties[i].getForeignDbColumn());
    }
    sb.append(", ").append(fetchOrderBy);
    return sb.toString().trim();
  }

  /**
   * Add a where clause to the query for a given list of parent Id's.
   */
  void addWhereParentIdIn(SpiQuery<?> query, List<Object> parentIds) {

    String tableAlias = hasJoinTable ? "int_." : "t0.";
    if (hasJoinTable) {
      query.setM2MIncludeJoin(many.inverseJoin);
    }
    String rawWhere = deriveWhereParentIdSql(true, tableAlias);
    String expr = descriptor.getParentIdInExpr(parentIds.size(), rawWhere);

    many.bindParentIdsIn(expr, parentIds, query);
  }

  List<Object> findIdsByParentId(Object parentId, Transaction t, List<Object> excludeDetailIds) {

    String rawWhere = deriveWhereParentIdSql(false, "");

    SpiEbeanServer server = descriptor.getEbeanServer();
    SpiQuery<?> q = many.newQuery(server);
    many.bindParentIdEq(rawWhere, parentId, q);

    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      q.where().not(q.getExpressionFactory().idIn(excludeDetailIds));
    }

    return server.findIds(q, t);
  }

  List<Object> findIdsByParentIdList(List<Object> parentIds, Transaction t, List<Object> excludeDetailIds) {

    String rawWhere = deriveWhereParentIdSql(true, "");
    String inClause = buildInClauseBinding(parentIds.size(), exportedPropertyBindProto);

    String expr = rawWhere + inClause;

    SpiEbeanServer server = descriptor.getEbeanServer();
    SpiQuery<?> q = many.newQuery(server);
    //Query<?> q = server.find(propertyType);
    many.bindParentIdsIn(expr, parentIds, q);

    if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
      q.where().not(q.getExpressionFactory().idIn(excludeDetailIds));
    }

    return server.findIds(q, t);
  }

  SqlUpdate deleteByParentId(Object parentId) {
    DefaultSqlUpdate sqlDelete = new DefaultSqlUpdate(deleteByParentIdSql);
    many.bindParentId(sqlDelete, parentId);
    return sqlDelete;
  }

  SqlUpdate deleteByParentIdList(List<Object> parentIds) {

    StringBuilder sb = new StringBuilder(100);
    sb.append(deleteByParentIdInSql);
    sb.append(buildInClauseBinding(parentIds.size(), exportedPropertyBindProto));

    DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
    many.bindParentIds(delete, parentIds);
    return delete;
  }

  private void appendBind(StringBuilder sb, int count, boolean skipComma) {
    for (int i = 0; i < count; i++) {
      if (!skipComma || i > 0) {
        sb.append(",");
      }
      sb.append("?");
    }
  }

  private void append(StringBuilder sb, String tableAlias, String prefix, String suffix) {
    for (int i = 0; i < exportedProperties.length; i++) {
      String fkColumn = exportedProperties[i].getForeignDbColumn();
      if (i > 0) {
        sb.append(prefix);
      }
      sb.append(tableAlias).append(fkColumn);
      sb.append(suffix);
    }
  }
  private String deriveWhereParentIdSql(boolean inClause, String tableAlias) {

    StringBuilder sb = new StringBuilder();

    if (inClause) {
      sb.append("(");
    }
    for (int i = 0; i < exportedProperties.length; i++) {
      String fkColumn = exportedProperties[i].getForeignDbColumn();
      if (i > 0) {
        String s = inClause ? "," : " and ";
        sb.append(s);
      }
      sb.append(tableAlias).append(fkColumn);
      if (!inClause) {
        sb.append("=? ");
      }
    }
    if (inClause) {
      sb.append(")");
    }
    return sb.toString();
  }

  private String buildInClauseBinding(int size, String bindProto) {

    if (descriptor.isSimpleId()) {
      return descriptor.getIdBinder().getIdInValueExpr(false, size);
    }
    StringBuilder sb = new StringBuilder(10 + (size * (bindProto.length() + 1)));
    sb.append(" in");
    sb.append(" (");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(bindProto);
    }
    sb.append(") ");
    return sb.toString();
  }

  private String deriveExportedPropertyBindProto() {
    if (exportedProperties.length == 1) {
      return "?";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < exportedProperties.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("?");
    }
    sb.append(")");
    return sb.toString();
  }

}
