package io.ebeaninternal.server.query;

import io.ebean.CountDistinctOrder;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;
import io.ebean.event.readaudit.ReadAuditQueryPlan;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.OrmQueryLimitRequest;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.rawsql.SpiRawSql.ColumnMapping;
import io.ebeaninternal.server.rawsql.SpiRawSql.ColumnMapping.Column;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generates the SQL SELECT statements taking into account the physical
 * deployment properties.
 */
class CQueryBuilder {

  protected final String tableAliasPlaceHolder;
  protected final String columnAliasPrefix;

  private final SqlLimiter sqlLimiter;

  private final CQueryBuilderRawSql rawSqlHandler;

  private final Binder binder;

  private final boolean selectCountWithAlias;

  private final CQueryHistorySupport historySupport;

  private final CQueryDraftSupport draftSupport;

  private final DatabasePlatform dbPlatform;

  /**
   * Create the SqlGenSelect.
   */
  CQueryBuilder(DatabasePlatform dbPlatform, Binder binder, CQueryHistorySupport historySupport, CQueryDraftSupport draftSupport) {

    this.dbPlatform = dbPlatform;
    this.binder = binder;
    this.draftSupport = draftSupport;
    this.historySupport = historySupport;
    this.tableAliasPlaceHolder = dbPlatform.getTableAliasPlaceHolder();
    this.columnAliasPrefix = dbPlatform.getColumnAliasPrefix();
    this.sqlLimiter = dbPlatform.getSqlLimiter();
    this.rawSqlHandler = new CQueryBuilderRawSql(sqlLimiter, dbPlatform);
    this.selectCountWithAlias = dbPlatform.isSelectCountWithAlias();
  }

  /**
   * split the order by claus on the field delimiter and prefix each field with
   * the relation name
   */
  static String prefixOrderByFields(String name, String orderBy) {
    StringBuilder sb = new StringBuilder();
    for (String token : orderBy.split(",")) {
      if (sb.length() > 0) {
        sb.append(", ");
      }

      sb.append(name);
      sb.append(".");
      sb.append(token.trim());
    }

    return sb.toString();
  }

  /**
   * Build the delete query.
   */
  <T> CQueryUpdate buildUpdateQuery(String type, OrmQueryRequest<T> request) {

    SpiQuery<T> query = request.getQuery();
    String rootTableAlias = query.getAlias();
    query.setDelete();

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      // skip building the SqlTree and Sql string
      predicates.prepare(false);
      return new CQueryUpdate(type, request, predicates, queryPlan);
    }

    predicates.prepare(true);

    SqlTree sqlTree = createSqlTree(request, predicates);

    String sql;
    if (type.equals("Delete")) {
      sql = buildDeleteSql(request, rootTableAlias, predicates, sqlTree);
    } else {
      sql = buildUpdateSql(request, rootTableAlias, predicates, sqlTree);
    }

    // cache the query plan
    queryPlan = new CQueryPlan(request, sql, sqlTree, false, false, predicates.getLogWhereSql());
    request.putQueryPlan(queryPlan);
    return new CQueryUpdate(type, request, predicates, queryPlan);
  }

  private <T> String buildDeleteSql(OrmQueryRequest<T> request, String rootTableAlias, CQueryPredicates predicates, SqlTree sqlTree) {

    if (!sqlTree.isIncludeJoins()) {
      // simple - delete from table ...
      return aliasStrip(buildSql("delete", request, predicates, sqlTree).getSql());
    }
    // wrap as - delete from table where id in (select id ...)
    String sql = buildSql(null, request, predicates, sqlTree).getSql();
    sql = request.getBeanDescriptor().getDeleteByIdInSql() + "in (" + sql + ")";
    sql = aliasReplace(sql, alias(rootTableAlias));
    return sql;
  }

  private String alias(String rootTableAlias) {
    return (rootTableAlias == null) ? "t0" : rootTableAlias;
  }

  private <T> String buildUpdateSql(OrmQueryRequest<T> request, String rootTableAlias, CQueryPredicates predicates, SqlTree sqlTree) {

    StringBuilder sb = new StringBuilder(200);
    sb.append("update ").append(request.getBeanDescriptor().getBaseTable());
    if (rootTableAlias != null) {
      sb.append(" ").append(rootTableAlias);
    }
    sb.append(" set ").append(predicates.getDbUpdateClause());
    String updateClause = sb.toString();

    if (!sqlTree.isIncludeJoins()) {
      // simple - update table set ... where ...
      return aliasStrip(buildSqlUpdate(updateClause, request, predicates, sqlTree).getSql());
    }
    // wrap as - update table set ... where id in (select id ...)
    String sql = buildSqlUpdate(null, request, predicates, sqlTree).getSql();
    sql = updateClause + " " + request.getBeanDescriptor().getWhereIdInSql() + "in (" + sql + ")";
    sql = aliasReplace(sql, alias(rootTableAlias));
    return sql;
  }

  /**
   * Strip the root table alias.
   */
  private String aliasStrip(String sql) {
    sql = StringHelper.replaceString(sql, "${RTA}.", "");
    return StringHelper.replaceString(sql, " ${RTA}", "");
  }

  /**
   * Replace the root table alias.
   */
  private String aliasReplace(String sql, String replaceWith) {
    sql = StringHelper.replaceString(sql, "${RTA}.", replaceWith + ".");
    return StringHelper.replaceString(sql, "${RTA}", replaceWith);
  }

  CQueryFetchSingleAttribute buildFetchAttributeQuery(OrmQueryRequest<?> request) {

    SpiQuery<?> query = request.getQuery();
    query.setSingleAttribute();

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      predicates.prepare(false);
      return new CQueryFetchSingleAttribute(request, predicates, queryPlan, query.isCountDistinct());
    }

    // use RawSql or generated Sql
    predicates.prepare(true);

    SqlTree sqlTree = createSqlTree(request, predicates);
    SqlLimitResponse s = buildSql(null, request, predicates, sqlTree);

    queryPlan = new CQueryPlan(request, s.getSql(), sqlTree, false, s.isIncludesRowNumberColumn(), predicates.getLogWhereSql());
    request.putQueryPlan(queryPlan);
    return new CQueryFetchSingleAttribute(request, predicates, queryPlan, query.isCountDistinct());
  }

  /**
   * Build the find ids query.
   */
  <T> CQueryFetchSingleAttribute buildFetchIdsQuery(OrmQueryRequest<T> request) {

    SpiQuery<T> query = request.getQuery();
    query.setSelectId();
    BeanDescriptor<T> desc = request.getBeanDescriptor();
    if (desc.isSoftDelete()) {
      query.addSoftDeletePredicate(desc.getSoftDeletePredicate(alias(query.getAlias())));
    }
    return buildFetchAttributeQuery(request);
  }

  /**
   * Return the history support if this query needs it (is a 'as of' type query).
   */
  <T> CQueryHistorySupport getHistorySupport(SpiQuery<T> query) {
    return query.getTemporalMode().isHistory() ? historySupport : null;
  }

  /**
   * Return the draft support (or null) for a 'asDraft' query.
   */
  <T> CQueryDraftSupport getDraftSupport(SpiQuery<T> query) {
    return query.getTemporalMode() == SpiQuery.TemporalMode.DRAFT ? draftSupport : null;
  }

  /**
   * Build the row count query.
   */
  <T> CQueryRowCount buildRowCountQuery(OrmQueryRequest<T> request) {

    SpiQuery<T> query = request.getQuery();

    // always set the order by to null for row count query
    query.setOrder(null);
    query.setFirstRow(0);
    query.setMaxRows(0);

    ManyWhereJoins manyWhereJoins = query.getManyWhereJoins();

    boolean countDistinct = query.isDistinct();
    if (!countDistinct) {
      // minimise select clause for standard count
      if (manyWhereJoins.isFormulaWithJoin()) {
        query.select(manyWhereJoins.getFormulaProperties());
      } else {
        query.setSelectId();
      }
    }

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      // skip building the SqlTree and Sql string
      predicates.prepare(false);
      return new CQueryRowCount(queryPlan, request, predicates);
    }

    predicates.prepare(true);

    SqlTree sqlTree = createSqlTree(request, predicates);
    if (SpiQuery.TemporalMode.CURRENT == query.getTemporalMode()) {
      sqlTree.addSoftDeletePredicate(query);
    }

    boolean hasMany = sqlTree.hasMany();

    String sqlSelect = null;
    if (countDistinct) {
      if (sqlTree.isSingleProperty()) {
        request.setInlineCountDistinct();
      }
    } else {
      if (hasMany) {
        // need to count distinct id's ...
        query.setSqlDistinct(true);
      } else {
        sqlSelect = "select count(*)";
      }
    }

    SqlLimitResponse s = buildSql(sqlSelect, request, predicates, sqlTree);
    String sql = s.getSql();

    if (!request.isInlineCountDistinct()) {
      if (countDistinct) {
        sql = wrapSelectCount(sql);

      } else if (hasMany || query.isRawSql()) {
        // remove order by - mssql does not accept order by in subqueries
        int pos = sql.lastIndexOf(" order by ");
        if (pos != -1) {
          sql = sql.substring(0, pos);
        }
        sql = wrapSelectCount(sql);
      }
    }

    // cache the query plan
    queryPlan = new CQueryPlan(request, sql, sqlTree, false, s.isIncludesRowNumberColumn(), predicates.getLogWhereSql());
    request.putQueryPlan(queryPlan);

    return new CQueryRowCount(queryPlan, request, predicates);
  }

  private String wrapSelectCount(String sql) {
    sql = "select count(*) from ( " + sql + ")";
    if (selectCountWithAlias) {
      sql += " as c";
    }
    return sql;
  }

  /**
   * Return the SQL Select statement as a String. Converts logical property
   * names to physical deployment column names.
   */
  <T> CQuery<T> buildQuery(OrmQueryRequest<T> request) {

    CQueryPredicates predicates = new CQueryPredicates(binder, request);

    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      // Reuse the query plan so skip generating SqlTree and SQL.
      // We do prepare and bind the new parameters
      predicates.prepare(false);
      return new CQuery<>(request, predicates, queryPlan);
    }

    // RawSql or Generated Sql query

    // Prepare the where, having and order by clauses.
    // This also parses them from logical property names to
    // database columns and determines 'includes'.

    // We need to check these 'includes' for extra joins
    // that are not included via select
    predicates.prepare(true);

    // Build the tree structure that represents the query.
    SpiQuery<T> query = request.getQuery();

    SqlTree sqlTree = createSqlTree(request, predicates);
    if (query.isAsOfQuery()) {
      sqlTree.addAsOfTableAlias(query);
    } else if (SpiQuery.TemporalMode.CURRENT == query.getTemporalMode()) {
      sqlTree.addSoftDeletePredicate(query);
    }

    SqlLimitResponse res = buildSql(null, request, predicates, sqlTree);

    boolean rawSql = request.isRawSql();
    if (rawSql) {
      queryPlan = new CQueryPlanRawSql(request, res, sqlTree, predicates.getLogWhereSql());

    } else {
      queryPlan = new CQueryPlan(request, res, sqlTree, false, predicates.getLogWhereSql());
    }

    BeanDescriptor<T> desc = request.getBeanDescriptor();
    if (desc.isReadAuditing()) {
      // log the query plan based bean type (i.e. ignoring query disabling for logging the sql/plan)
      desc.getReadAuditLogger().queryPlan(new ReadAuditQueryPlan(desc.getFullName(), queryPlan.getAuditQueryKey(), queryPlan.getSql()));
    }

    // cache the query plan because we can reuse it and also
    // gather query performance statistics based on it.
    request.putQueryPlan(queryPlan);

    return new CQuery<>(request, predicates, queryPlan);
  }

  /**
   * Build the SqlTree.
   * <p>
   * The SqlTree is immutable after construction and so is safe to use by
   * concurrent threads.
   * </p>
   * <p>
   * The predicates is used to add additional joins that come from the where or
   * order by clauses that are not already included for the select clause.
   * </p>
   */
  private SqlTree createSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {

    if (request.isNativeSql()) {
      return createNativeSqlTree(request, predicates);
    }
    if (request.isRawSql()) {
      return createRawSqlSqlTree(request, predicates);
    }
    return new SqlTreeBuilder(this, request, predicates).build();
  }

  private String nativeQueryPaging(SpiQuery<?> query, String sql) {
    return dbPlatform.getBasicSqlLimiter().limit(sql, query.getFirstRow(), query.getMaxRows());
  }

  /**
   * Create the SqlTree by reading the ResultSetMetaData and mapping table/columns to bean property paths.
   */
  private SqlTree createNativeSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {

    SpiQuery<?> query = request.getQuery();

    // parse named parameters returning the final sql to execute
    String sql = predicates.parseBindParams(query.getNativeSql());
    if (query.hasMaxRowsOrFirstRow()) {
      sql = nativeQueryPaging(query, sql);
    }
    query.setGeneratedSql(sql);

    Connection connection = request.getTransaction().getConnection();

    BeanDescriptor<?> desc = request.getBeanDescriptor();
    try {
      // For SqlServer we need either "selectMethod=cursor" in the connection string or fetch explicitly a cursorable
      // statement here by specifying ResultSet.CONCUR_UPDATABLE
      PreparedStatement statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      predicates.bind(statement, connection);

      ResultSet resultSet = statement.executeQuery();
      ResultSetMetaData metaData = resultSet.getMetaData();

      int cols = 1 + metaData.getColumnCount();
      List<String> propertyNames = new ArrayList<>(cols - 1);
      for (int i = 1; i < cols; i++) {
        String tableName = metaData.getTableName(i).toLowerCase();
        String columnName = metaData.getColumnName(i).toLowerCase();
        String path = desc.findBeanPath(tableName, columnName);
        if (path != null) {
          propertyNames.add(path);
        } else {
          propertyNames.add(SpiRawSql.IGNORE_COLUMN);
        }
      }

      RawSql rawSql = RawSqlBuilder.resultSet(resultSet, propertyNames.toArray(new String[propertyNames.size()]));
      query.setRawSql(rawSql);
      return createRawSqlSqlTree(request, predicates);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private SqlTree createRawSqlSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {

    BeanDescriptor<?> descriptor = request.getBeanDescriptor();
    ColumnMapping columnMapping = request.getQuery().getRawSql().getColumnMapping();

    PathProperties pathProps = new PathProperties();

    // convert list of columns into (tree like) PathProperties
    Iterator<Column> it = columnMapping.getColumns();
    while (it.hasNext()) {
      SpiRawSql.ColumnMapping.Column column = it.next();
      String propertyName = column.getPropertyName();
      if (!SpiRawSql.IGNORE_COLUMN.equals(propertyName)) {
        ElPropertyValue el = descriptor.getElGetValue(propertyName);
        if (el == null && propertyName.endsWith("Id")) {
          // try default naming convention for foreign key columns
          String foreignIdPath = assocOneIdPath(propertyName);
          el = descriptor.getElGetValue(foreignIdPath);
          if (el != null) {
            propertyName = foreignIdPath;
          }
        }
        if (el == null) {
          throw new PersistenceException("Property [" + propertyName + "] not found on " + descriptor.getFullName());
        } else {
          BeanProperty beanProperty = el.getBeanProperty();
          if (beanProperty.isId()) {
            if (propertyName.contains(".")) {
              // For @Id properties we chop off the last part of the path
              propertyName = SplitName.parent(propertyName);
            }
          } else if (beanProperty.isDiscriminator()) {
            propertyName = SplitName.parent(propertyName);
          } else if (beanProperty instanceof BeanPropertyAssocOne<?>) {
            String msg = "Column [" + column.getDbColumn() + "] mapped to complex Property[" + propertyName + "]";
            msg += ". It should be mapped to a simple property (probably the Id property). ";
            throw new PersistenceException(msg);
          }
          if (propertyName != null) {
            boolean assocProperty = el.isAssocProperty();
            if (!assocProperty) {
              pathProps.addToPath(null, propertyName);
            } else {
              String[] pathProp = SplitName.split(propertyName);
              pathProps.addToPath(pathProp[0], pathProp[1]);
            }
          }
        }
      }
    }

    OrmQueryDetail detail = new OrmQueryDetail();

    // transfer PathProperties into OrmQueryDetail
    for (PathProperties.Props props : pathProps.getPathProps()) {
      detail.fetch(props.getPath(), props.getProperties());
    }

    // check if @Id property included in RawSql
    boolean rawNoId = true;
    BeanProperty idProperty = descriptor.getIdProperty();
    if (idProperty != null && columnMapping.contains(idProperty.getName())) {
      // contains the @Id property for the root level bean
      rawNoId = false;
    }

    // build SqlTree based on OrmQueryDetail of the RawSql
    return new SqlTreeBuilder(request, predicates, detail, rawNoId).build();
  }

  /**
   * Return a path for a foreign key property using the default naming convention.
   */
  private String assocOneIdPath(String propertyName) {
    return propertyName.substring(0, propertyName.length() - 2) + ".id";
  }

  /**
   * Return the SQL response with row limiting (when not an update statement).
   */
  private SqlLimitResponse buildSql(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select) {
    return buildSql(selectClause, request, predicates, select, false);
  }

  /**
   * Return the SQL response for update statement (stripping table alias for find by id expression).
   */
  private SqlLimitResponse buildSqlUpdate(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select) {
    return buildSql(selectClause, request, predicates, select, true);
  }

  private SqlLimitResponse buildSql(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select, boolean stripAlias) {

    SpiQuery<?> query = request.getQuery();

    if (query.isNativeSql()) {
      return new SqlLimitResponse(query.getGeneratedSql(), false);
    }
    if (query.isRawSql()) {
      return rawSqlHandler.buildSql(request, predicates, query.getRawSql().getSql());
    }

    boolean useSqlLimiter = false;
    StringBuilder sb = new StringBuilder(500);
    String dbOrderBy = predicates.getDbOrderBy();

    if (selectClause != null) {
      sb.append(selectClause);

    } else {
      useSqlLimiter = (query.hasMaxRowsOrFirstRow() && select.getManyProperty() == null);

      if (!useSqlLimiter) {
        sb.append("select ");
        if (query.isDistinctQuery()) {
          if (request.isInlineCountDistinct()) {
            sb.append("count(");
          }
          sb.append("distinct ");
          String distinctOn = select.getDistinctOn();
          if (distinctOn != null) {
            sb.append("on (");
            sb.append(distinctOn).append(") ");
          }
        }
      }
      if (query.isCountDistinct() && query.isSingleAttribute()) {
        sb.append("r1.attribute_, count(*) from (select ");
        sb.append(select.getSelectSql());
        sb.append(" as attribute_");
      } else {
        sb.append(select.getSelectSql());
      }
      if (request.isInlineCountDistinct()) {
        sb.append(")");
      }
      if (query.isDistinctQuery() && dbOrderBy != null && !query.isSingleAttribute()) {
        // add the orderBy columns to the select clause (due to distinct)
        sb.append(", ").append(DbOrderByTrim.trim(dbOrderBy));
      }
    }

    if (selectClause == null || !selectClause.startsWith("update")) {
      sb.append(" from ");
      sb.append(select.getFromSql());
    }

    String inheritanceWhere = select.getInheritanceWhereSql();

    boolean hasWhere = false;
    if (!inheritanceWhere.isEmpty()) {
      sb.append(" where");
      sb.append(inheritanceWhere);
      hasWhere = true;
    }

    if (query.isAsOfBaseTable() && !historySupport.isStandardsBased()) {
      hasWhere = appendWhere(hasWhere, sb);
      sb.append(historySupport.getAsOfPredicate(request.getBaseTableAlias()));
    }

    if (request.isFindById() || query.getId() != null) {
      appendWhere(hasWhere, sb);

      BeanDescriptor<?> desc = request.getBeanDescriptor();
      String idSql = desc.getIdBinderIdSql(query.getAlias());
      if (idSql.isEmpty()) {
        throw new IllegalStateException("Executing FindById query on entity bean " + desc.getName()
          + " that doesn't have an @Id property??");
      }
      if (stripAlias) {
        // strip the table alias for use in update statement
        idSql = StringHelper.replaceString(idSql, "t0.", "");
      }
      sb.append(idSql).append(" ");
      hasWhere = true;
    }

    String dbWhere = predicates.getDbWhere();
    if (!isEmpty(dbWhere)) {
      if (!hasWhere) {
        hasWhere = true;
        sb.append(" where ");
      } else {
        sb.append(" and ");
      }
      sb.append(dbWhere);
    }

    String dbFilterMany = predicates.getDbFilterMany();
    if (!isEmpty(dbFilterMany)) {
      if (!hasWhere) {
        hasWhere = true;
        sb.append(" where ");
      } else {
        sb.append("and ");
      }
      sb.append(dbFilterMany);
    }

    if (!query.isIncludeSoftDeletes()) {
      List<String> softDeletePredicates = query.getSoftDeletePredicates();
      if (softDeletePredicates != null) {
        if (!hasWhere) {
          sb.append(" where ");
        } else {
          sb.append(" and ");
        }
        for (int i = 0; i < softDeletePredicates.size(); i++) {
          if (i > 0) {
            sb.append(" and ");
          }
          sb.append(softDeletePredicates.get(i));
        }
      }
    }

    String groupBy = select.getGroupBy();
    if (groupBy != null) {
      sb.append(" group by ").append(groupBy);
    }

    String dbHaving = predicates.getDbHaving();
    if (!isEmpty(dbHaving)) {
      sb.append(" having ").append(dbHaving);
    }

    if (dbOrderBy != null && !query.isCountDistinct()) {
      sb.append(" order by ").append(dbOrderBy);
    }

    if (query.isCountDistinct() && query.isSingleAttribute()) {
      sb.append(") r1 group by r1.attribute_");
      sb.append(toSql(query.getCountDistinctOrder()));
    }

    if (useSqlLimiter) {
      // use LIMIT/OFFSET, ROW_NUMBER() or rownum type SQL query limitation
      SqlLimitRequest r = new OrmQueryLimitRequest(sb.toString(), dbOrderBy, query, dbPlatform);
      return sqlLimiter.limit(r);

    } else {
      return new SqlLimitResponse(dbPlatform.completeSql(sb.toString(), query), false);
    }

  }

  private String toSql(CountDistinctOrder orderBy) {
    switch(orderBy) {
    case ATTR_ASC:
      return " order by r1.attribute_";
    case ATTR_DESC:
      return " order by r1.attribute_ desc";
    case COUNT_ASC_ATTR_ASC:
      return " order by count(*), r1.attribute_";
    case COUNT_ASC_ATTR_DESC:
      return " order by count(*), r1.attribute_ desc";
    case COUNT_DESC_ATTR_ASC:
      return " order by count(*) desc, r1.attribute_";
    case COUNT_DESC_ATTR_DESC:
      return " order by count(*) desc, r1.attribute_ desc";
    default:
      throw new IllegalArgumentException("Illegal enum: "+ orderBy);
    }
  }

  /**
   * Append where or and based on the hasWhere flag.
   */
  private boolean appendWhere(boolean hasWhere, StringBuilder sb) {
    if (hasWhere) {
      sb.append(" and ");
    } else {
      sb.append(" where ");
    }
    return true;
  }

  private boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  boolean isPlatformDistinctOn() {
    return dbPlatform.isPlatform(Platform.POSTGRES);
  }

  /**
   * Return the 'for update' FROM hint (sql server).
   */
  public String fromForUpdate(SpiQuery<?> query) {
    Query.ForUpdate mode = query.getForUpdateMode();
    if (mode == null) {
      return null;
    } else {
      return dbPlatform.fromForUpdate(mode);
    }
  }
}
