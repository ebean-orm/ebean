package io.ebeaninternal.server.query;

import io.ebean.*;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;
import io.ebean.event.readaudit.ReadAuditQueryPlan;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
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

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

/**
 * Generates the SQL SELECT statements taking into account the physical
 * deployment properties.
 */
final class CQueryBuilder {

  private final String columnAliasPrefix;
  private final SqlLimiter sqlLimiter;
  private final CQueryBuilderRawSql rawSqlHandler;
  private final Binder binder;
  private final boolean selectCountWithAlias;
  private final CQueryHistorySupport historySupport;
  private final CQueryDraftSupport draftSupport;
  private final DatabasePlatform dbPlatform;
  private final boolean selectCountWithColumnAlias;
  private final boolean includeLabelInSql;

  /**
   * Create the SqlGenSelect.
   */
  CQueryBuilder(DatabaseBuilder.Settings config, DatabasePlatform dbPlatform, Binder binder, CQueryHistorySupport historySupport, CQueryDraftSupport draftSupport) {
    this.includeLabelInSql = config.isIncludeLabelInSql();
    this.dbPlatform = dbPlatform;
    this.binder = binder;
    this.draftSupport = draftSupport;
    this.historySupport = historySupport;
    this.columnAliasPrefix = dbPlatform.columnAliasPrefix();
    this.sqlLimiter = dbPlatform.sqlLimiter();
    this.rawSqlHandler = new CQueryBuilderRawSql(sqlLimiter, dbPlatform);
    this.selectCountWithAlias = dbPlatform.selectCountWithAlias();
    this.selectCountWithColumnAlias = dbPlatform.selectCountWithColumnAlias();
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
      sb.append('.');
      sb.append(token.trim());
    }
    return sb.toString();
  }

  /**
   * Build the delete query.
   */
  <T> CQueryUpdate buildUpdateQuery(boolean deleteRequest, OrmQueryRequest<T> request) {
    SpiQuery<T> query = request.query();
    String rootTableAlias = query.alias();
    query.setupForDeleteOrUpdate();

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.queryPlan();
    if (queryPlan != null) {
      // skip building the SqlTree and Sql string
      predicates.prepare(false);
      return new CQueryUpdate(request, predicates, queryPlan);
    }

    predicates.prepare(true);
    SqlTree sqlTree = createSqlTree(request, predicates);

    String sql;
    if (deleteRequest) {
      sql = buildDeleteSql(request, rootTableAlias, predicates, sqlTree);
    } else {
      sql = buildUpdateSql(request, rootTableAlias, predicates, sqlTree);
    }
    // cache the query plan
    queryPlan = new CQueryPlan(request, sql, sqlTree.plan(), predicates.logWhereSql());
    request.putQueryPlan(queryPlan);
    return new CQueryUpdate(request, predicates, queryPlan);
  }

  private <T> String buildDeleteSql(OrmQueryRequest<T> request, String rootTableAlias, CQueryPredicates predicates, SqlTree sqlTree) {
    String alias = alias(rootTableAlias);
    if (sqlTree.noJoins() && !request.query().hasMaxRowsOrFirstRow()) {
      if (dbPlatform.supportsDeleteTableAlias()) {
        // delete from table <alias> ...
        return aliasReplace(buildSqlDelete("delete", request, predicates, sqlTree).getSql(), alias);
      } else if (isMySql(dbPlatform.platform())) {
        return aliasReplace(buildSqlDelete("delete " + alias, request, predicates, sqlTree).getSql(), alias);
      } else {
        // simple - delete from table ...
        return aliasStrip(buildSqlDelete("delete", request, predicates, sqlTree).getSql());
      }
    }
    // wrap as - delete from table where id in (select id ...)
    String sql = buildSqlDelete(null, request, predicates, sqlTree).getSql();
    sql = request.descriptor().deleteByIdInSql() + "in (" + sql + ")";
    sql = aliasReplace(sql, alias);
    return sql;
  }

  private boolean isMySql(Platform platform) {
    return platform.base() == Platform.MYSQL;
  }

  private String alias(String rootTableAlias) {
    return (rootTableAlias == null) ? "t0" : rootTableAlias;
  }

  private <T> String buildUpdateSql(OrmQueryRequest<T> request, String rootTableAlias, CQueryPredicates predicates, SqlTree sqlTree) {
    StringBuilder sb = new StringBuilder(200);
    sb.append("update ").append(request.descriptor().baseTable());
    if (rootTableAlias != null) {
      sb.append(' ').append(rootTableAlias);
    }
    sb.append(" set ").append(predicates.dbUpdateClause());
    String updateClause = sb.toString();
    if (sqlTree.noJoins() && request.isInlineSqlUpdateLimit()) {
      // simple - update table set ... where ...
      return aliasStrip(buildSqlUpdate(updateClause, request, predicates, sqlTree).getSql());
    }
    // wrap as - update table set ... where id in (select id ...)
    String sql = buildSqlUpdate(null, request, predicates, sqlTree).getSql();
    sql = updateClause + " " + request.descriptor().whereIdInSql() + "in (" + sql + ")";
    sql = aliasReplace(sql, alias(rootTableAlias));
    return sql;
  }

  /**
   * Strip the root table alias.
   */
  private String aliasStrip(String sql) {
    return sql.replace("${RTA}.", "").replace(" ${RTA}", "");
  }

  /**
   * Replace the root table alias.
   */
  private String aliasReplace(String sql, String replaceWith) {
    return sql.replace("${RTA}.", replaceWith + ".").replace("${RTA}", replaceWith);
  }

  CQueryFetchSingleAttribute buildFetchAttributeQuery(OrmQueryRequest<?> request) {
    SpiQuery<?> query = request.query();
    if (!query.isIncludeSoftDeletes()) {
      BeanDescriptor<?> desc = request.descriptor();
      if (desc.isSoftDelete()) {
        query.addSoftDeletePredicate(desc.softDeletePredicate(alias(query.alias())));
      }
    }
    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.queryPlan();
    if (queryPlan != null) {
      predicates.prepare(false);
      return new CQueryFetchSingleAttribute(request, predicates, queryPlan, query.isCountDistinct());
    }

    // use RawSql or generated Sql
    predicates.prepare(true);
    SqlTree sqlTree = createSqlTree(request, predicates);
    SqlLimitResponse s = buildSql(null, request, predicates, sqlTree);

    queryPlan = new CQueryPlan(request, s.getSql(), sqlTree.plan(), predicates.logWhereSql());
    request.putQueryPlan(queryPlan);
    return new CQueryFetchSingleAttribute(request, predicates, queryPlan, query.isCountDistinct());
  }

  /**
   * Build the find ids query.
   */
  <T> CQueryFetchSingleAttribute buildFetchIdsQuery(OrmQueryRequest<T> request) {
    SpiQuery<T> query = request.query();
    query.setSelectId();
    BeanDescriptor<T> desc = request.descriptor();
    if (!query.isIncludeSoftDeletes() && desc.isSoftDelete()) {
      query.addSoftDeletePredicate(desc.softDeletePredicate(alias(query.alias())));
    }
    return buildFetchAttributeQuery(request);
  }

  /**
   * Return the history support if this query needs it (is a 'as of' type query).
   */
  <T> CQueryHistorySupport historySupport(SpiQuery<T> query) {
    return query.temporalMode().isHistory() ? historySupport : null;
  }

  /**
   * Return the draft support (or null) for a 'asDraft' query.
   */
  <T> CQueryDraftSupport draftSupport(SpiQuery<T> query) {
    return query.temporalMode() == SpiQuery.TemporalMode.DRAFT ? draftSupport : null;
  }

  /**
   * Build the row count query.
   */
  <T> CQueryRowCount buildRowCountQuery(OrmQueryRequest<T> request) {
    SpiQuery<T> query = request.query();
    // always set the order by to null for row count query
    query.setOrderBy(null);
    query.setFirstRow(0);
    query.setMaxRows(0);

    boolean countDistinct = query.isDistinct();
    boolean useColumnAlias = selectCountWithColumnAlias;
    boolean withAgg = false;
    if (!countDistinct) {
      withAgg = includesAggregation(request, query);
      if (!withAgg && request.descriptor().hasId()) {
        // minimise select clause for standard count
        query.setSelectId();
        useColumnAlias = false;
      }
    }

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.queryPlan();
    if (queryPlan != null) {
      // skip building the SqlTree and Sql string
      predicates.prepare(false);
      return new CQueryRowCount(queryPlan, request, predicates);
    }

    predicates.prepare(true);
    SqlTree sqlTree = createSqlTree(request, predicates, useColumnAlias);
    if (SpiQuery.TemporalMode.CURRENT == query.temporalMode()) {
      sqlTree.addSoftDeletePredicate(query);
    }

    boolean wrap = sqlTree.hasMany() || withAgg;
    String sqlSelect = null;
    if (countDistinct) {
      if (sqlTree.isSingleProperty()) {
        request.setInlineCountDistinct();
      }
    } else if (!wrap) {
      sqlSelect = "select count(*)";
    }

    SqlLimitResponse s = buildSql(sqlSelect, request, predicates, sqlTree);
    String sql = s.getSql();
    if (!request.isInlineCountDistinct()) {
      if (countDistinct) {
        sql = wrapSelectCount(sql);
      } else if (wrap || query.isRawSql()) {
        // remove order by - mssql does not accept order by in subqueries
        int pos = sql.lastIndexOf(" order by ");
        if (pos != -1) {
          sql = sql.substring(0, pos);
        }
        sql = wrapSelectCount(sql);
      }
    }
    // cache the query plan
    queryPlan = new CQueryPlan(request, sql, sqlTree.plan(), predicates.logWhereSql());
    request.putQueryPlan(queryPlan);
    return new CQueryRowCount(queryPlan, request, predicates);
  }

  /**
   * Return true if the query includes an aggregation property.
   */
  private <T> boolean includesAggregation(OrmQueryRequest<T> request, SpiQuery<T> query) {
    return request.descriptor().includesAggregation(query.detail());
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
    CQueryPlan queryPlan = request.queryPlan();
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
    SpiQuery<T> query = request.query();
    SqlTree sqlTree = createSqlTree(request, predicates);
    if (query.isAsOfQuery()) {
      sqlTree.addAsOfTableAlias(query);
    } else if (SpiQuery.TemporalMode.CURRENT == query.temporalMode()) {
      sqlTree.addSoftDeletePredicate(query);
    }

    SqlLimitResponse res = buildSql(null, request, predicates, sqlTree);
    boolean rawSql = request.isRawSql();
    if (rawSql) {
      queryPlan = new CQueryPlanRawSql(request, res, sqlTree, predicates.logWhereSql());
    } else {
      queryPlan = new CQueryPlan(request, res, sqlTree.plan(), false, predicates.logWhereSql());
    }
    BeanDescriptor<T> desc = request.descriptor();
    if (desc.isReadAuditing()) {
      // log the query plan based bean type (i.e. ignoring query disabling for logging the sql/plan)
      desc.readAuditLogger().queryPlan(new ReadAuditQueryPlan(desc.fullName(), queryPlan.auditQueryKey(), queryPlan.sql()));
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
   * <p>
   * The predicates is used to add additional joins that come from the where or
   * order by clauses that are not already included for the select clause.
   */
  private SqlTree createSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {
    return createSqlTree(request, predicates, false);
  }

  private SqlTree createSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates, boolean forceColumnAlias) {
    if (request.isNativeSql()) {
      return createNativeSqlTree(request, predicates);
    }
    if (request.isRawSql()) {
      return createRawSqlSqlTree(request, predicates);
    }
    String colAliasPrefix = forceColumnAlias ? "c" : columnAliasPrefix;
    return new SqlTreeBuilder(colAliasPrefix, this, request, predicates).build();
  }

  private String nativeQueryPaging(SpiQuery<?> query, String sql) {
    return dbPlatform.basicSqlLimiter().limit(sql, query.getFirstRow(), query.getMaxRows());
  }

  /**
   * Create the SqlTree by reading the ResultSetMetaData and mapping table/columns to bean property paths.
   */
  private SqlTree createNativeSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {
    SpiQuery<?> query = request.query();
    // parse named parameters returning the final sql to execute
    String sql = predicates.parseBindParams(query.nativeSql());
    if (query.hasMaxRowsOrFirstRow()) {
      sql = nativeQueryPaging(query, sql);
    }
    query.setGeneratedSql(sql);
    Connection connection = request.transaction().internalConnection();
    BeanDescriptor<?> desc = request.descriptor();
    try {
      // For SqlServer we need either "selectMethod=cursor" in the connection string or fetch explicitly a cursorable
      // statement here by specifying ResultSet.CONCUR_UPDATABLE
      PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, dbPlatform.supportsResultSetConcurrencyModeUpdatable() ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
      predicates.bind(statement, connection);
      ResultSet resultSet = statement.executeQuery();
      ResultSetMetaData metaData = resultSet.getMetaData();

      int cols = 1 + metaData.getColumnCount();
      List<String> propertyNames = new ArrayList<>(cols - 1);
      for (int i = 1; i < cols; i++) {
        String schemaName = lower(metaData.getSchemaName(i));
        String tableName = lower(metaData.getTableName(i));
        String columnName = lower(metaData.getColumnName(i));
        String path = desc.findBeanPath(schemaName, tableName, columnName);
        propertyNames.add(requireNonNullElse(path, SpiRawSql.IGNORE_COLUMN));
      }
      RawSql rawSql = RawSqlBuilder.resultSet(resultSet, propertyNames.toArray(new String[0]));
      query.setRawSql(rawSql);
      return createRawSqlSqlTree(request, predicates);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String lower(String value) {
    return value == null ? "" : value.toLowerCase();
  }

  private SqlTree createRawSqlSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {
    BeanDescriptor<?> descriptor = request.descriptor();
    ColumnMapping columnMapping = request.query().rawSql().getColumnMapping();
    PathProperties pathProps = new PathProperties();

    // convert list of columns into (tree like) PathProperties
    Iterator<Column> it = columnMapping.getColumns();
    while (it.hasNext()) {
      SpiRawSql.ColumnMapping.Column column = it.next();
      String propertyName = column.getPropertyName();
      if (!SpiRawSql.IGNORE_COLUMN.equals(propertyName)) {
        ElPropertyValue el = descriptor.elGetValue(propertyName);
        if (el == null && propertyName.endsWith("Id")) {
          // try default naming convention for foreign key columns
          String foreignIdPath = assocOneIdPath(propertyName);
          el = descriptor.elGetValue(foreignIdPath);
          if (el != null) {
            propertyName = foreignIdPath;
          }
        }
        if (el == null) {
          throw new PersistenceException("Property [" + propertyName + "] not found on " + descriptor.fullName());
        }
        addRawColumnMapping(pathProps, column, propertyName, el);
      }
    }

    OrmQueryDetail detail = new OrmQueryDetail();
    // transfer PathProperties into OrmQueryDetail
    for (PathProperties.Props props : pathProps.getPathProps()) {
      detail.fetch(props.getPath(), props.getProperties());
    }
    // check if @Id property included in RawSql
    boolean rawNoId = true;
    BeanProperty idProperty = descriptor.idProperty();
    if (idProperty != null && columnMapping.contains(idProperty.name())) {
      // contains the @Id property for the root level bean
      rawNoId = false;
    }
    // build SqlTree based on OrmQueryDetail of the RawSql
    return new SqlTreeBuilder(request, predicates, detail, rawNoId).build();
  }

  private void addRawColumnMapping(PathProperties pathProps, Column column, String propertyName, ElPropertyValue el) {
    BeanProperty beanProperty = el.beanProperty();
    if (beanProperty.isId()) {
      if (propertyName.contains(".")) {
        // For @Id properties we chop off the last part of the path
        propertyName = SplitName.parent(propertyName);
      }
    } else if (beanProperty.isDiscriminator()) {
      propertyName = SplitName.parent(propertyName);
    } else if (beanProperty instanceof BeanPropertyAssocOne<?>) {
      String msg = "Column " + column.getDbColumn() + " mapped to complex property " + propertyName +
        ". It should be mapped to a simple property (probably the Id property).";
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
    SpiQuery<?> query = request.query();
    if (query.isNativeSql()) {
      return new SqlLimitResponse(query.getGeneratedSql());
    }
    if (query.isRawSql()) {
      return rawSqlHandler.buildSql(request, predicates, query.rawSql().getSql());
    }
    return new BuildReq(selectClause, request, predicates, select).buildSql();
  }

  private SqlLimitResponse buildSqlDelete(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select) {
    return new BuildReq(selectClause, request, predicates, select).buildSql();
  }

  private SqlLimitResponse buildSqlUpdate(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select) {
    return new BuildReq(selectClause, request, predicates, select, true).buildSql();
  }

  private class BuildReq {
    private final StringBuilder sb = new StringBuilder(500);
    private final String selectClause;
    private final OrmQueryRequest<?> request;
    private final SpiQuery<?> query;
    private final CQueryPredicates predicates;
    private final SqlTree select;
    private final boolean updateStatement;
    private final boolean distinct;
    private final boolean countSingleAttribute;
    private final String dbOrderBy;
    private final boolean useSqlLimiter;
    private boolean hasWhere;

    private BuildReq(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select) {
      this(selectClause, request, predicates, select, false);
    }

    private BuildReq(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select, boolean updateStatement) {
      this.selectClause = selectClause;
      this.request = request;
      this.query = request.query();
      this.predicates = predicates;
      this.select = select;
      this.updateStatement = updateStatement;
      this.distinct = query.isDistinct() || select.isSqlDistinct();
      this.dbOrderBy = predicates.dbOrderBy();
      this.countSingleAttribute = query.isCountDistinct() && query.isSingleAttribute();
      this.useSqlLimiter = selectClause == null
        && query.hasMaxRowsOrFirstRow()
        && (select.distinctOn() != null || select.manyProperty() == null || query.isSingleAttribute());
    }

    private void appendSelect() {
      if (selectClause != null) {
        sb.append(selectClause);
      } else {
        if (!useSqlLimiter) {
          appendSelectDistinct();
        }
        if (countSingleAttribute) {
          sb.append("r1.attribute_, count(*) from (select ");
          if (distinct) {
            sb.append("distinct ");
            BeanProperty idProp = request.descriptor().idProperty();
            if (idProp.isEmbedded()) {
              BeanProperty[] props = ((BeanPropertyAssocOne<?>) idProp).properties();
              for (BeanProperty prop : props) {
                sb.append("t0.").append(prop.dbColumn()).append(", ");
              }
            } else {
              sb.append("t0.").append(idProp.dbColumn()).append(", ");
            }
          }
          sb.append(select.selectSql()).append(" as attribute_");
        } else {
          sb.append(select.selectSql());
        }
        if (request.isInlineCountDistinct()) {
          sb.append(')');
        }
        if (distinct && dbOrderBy != null && query.distinctOn() == null) {
          // add the orderBy columns to the select clause (due to distinct)
          String[] tokens = DbOrderByTrim.trim(dbOrderBy).split(",");
          for (String token : tokens) {
            token = token.trim();
            if (!DbOrderByTrim.contains(select.selectSql(), token)) {
              sb.append(", ").append(token);
            }
          }
        }
      }
    }

    private void appendSelectDistinct() {
      sb.append("select ").append(hint()).append(inlineSqlComment());
      if (distinct && !countSingleAttribute) {
        if (request.isInlineCountDistinct()) {
          sb.append("count(");
        }
        sb.append("distinct ");
        String distinctOn = select.distinctOn();
        if (distinctOn != null) {
          sb.append("on (").append(distinctOn).append(") ");
        }
      }
    }

    private String hint() {
      String hint = query.hint();
      return hint == null ? "" : dbPlatform.inlineSqlHint(hint);
    }

    private String inlineSqlComment() {
      if (!includeLabelInSql) {
        return "";
      }
      SpiQuery.Type type = query.type();
      if (type == SpiQuery.Type.SQ_EX || type == SpiQuery.Type.SQ_EXISTS) {
        return "";
      }
      final var label = query.label();
      if (label != null) {
        return dbPlatform.inlineSqlComment(label);
      }
      final var profileLocation = query.profileLocation();
      if (profileLocation != null) {
        return dbPlatform.inlineSqlComment(profileLocation.label());
      }
      return "";
    }

    private void appendFrom() {
      if (selectClause == null || !selectClause.startsWith("update")) {
        sb.append(" from ");
        sb.append(select.fromSql());
      }
    }

    private void appendAndOrWhere() {
      if (hasWhere) {
        sb.append(" and ");
      } else {
        sb.append(" where ");
        hasWhere = true;
      }
    }

    private void appendInheritanceWhere() {
      String inheritanceWhere = select.inheritanceWhereSql();
      if (!inheritanceWhere.isEmpty()) {
        sb.append(" where");
        sb.append(inheritanceWhere);
        hasWhere = true;
      }
    }

    private void appendHistoryAsOfPredicate() {
      if (query.isAsOfBaseTable()) {
        query.incrementAsOfTableCount();
        if (!historySupport.isStandardsBased()) {
          appendAndOrWhere();
          sb.append(historySupport.asOfPredicate(request.baseTableAlias()));
        }
      }
    }

    private void appendFindId() {
      if (request.isFindById() || query.getId() != null) {
        appendAndOrWhere();
        BeanDescriptor<?> desc = request.descriptor();
        String idSql = desc.idBinderIdSql(query.alias());
        if (idSql.isEmpty()) {
          throw new IllegalStateException("Executing FindById query on entity bean " + desc.name()
            + " that doesn't have an @Id property??");
        }
        if (updateStatement) {
          // strip the table alias for use in update statement
          idSql = idSql.replace("t0.", "");
        }
        sb.append(idSql);
        hasWhere = true;
      }
    }

    private void appendToWhere(String predicate) {
      if (hasValue(predicate)) {
        appendAndOrWhere();
        sb.append(predicate);
      }
    }

    private void appendSoftDelete() {
      List<String> softDeletePredicates = query.softDeletePredicates();
      if (softDeletePredicates != null) {
        appendAndOrWhere();
        for (int i = 0; i < softDeletePredicates.size(); i++) {
          if (i > 0) {
            sb.append(" and ");
          }
          sb.append(softDeletePredicates.get(i));
        }
      }
    }

    private SqlLimitResponse buildSql() {
      appendSelect();
      appendFrom();
      appendInheritanceWhere();
      appendHistoryAsOfPredicate();
      appendFindId();
      appendToWhere(predicates.dbWhere());
      appendToWhere(predicates.dbFilterManyWhere());
      if (!query.isIncludeSoftDeletes()) {
        appendSoftDelete();
      }
      String groupBy = select.groupBy();
      if (groupBy != null) {
        sb.append(" group by ").append(groupBy);
      }
      String dbHaving = predicates.dbHaving();
      if (hasValue(dbHaving)) {
        sb.append(" having ").append(dbHaving);
      }
      if (dbOrderBy != null && !query.isCountDistinct()) {
        sb.append(" order by ").append(dbOrderBy);
      }
      if (countSingleAttribute) {
        sb.append(") r1 group by r1.attribute_");
        sb.append(toSql(query.countDistinctOrder()));
      }
      if (useSqlLimiter) {
        // use LIMIT/OFFSET, ROW_NUMBER() or rownum type SQL query limitation
        var r = new OrmQueryLimitRequest(sb.toString(), dbOrderBy, query, dbPlatform, distinct, select.distinctOn(), hint(), inlineSqlComment());
        return sqlLimiter.limit(r);
      } else {
        if (updateStatement) {
          final int maxRows = query.getMaxRows();
          if (maxRows > 0) {
            // limit on update statement only support on platforms with supportsMaxRowsOnUpdate
            sb.append(" limit ").append(maxRows);
          }
        }
        return new SqlLimitResponse(dbPlatform.completeSql(sb.toString(), query));
      }
    }

    private String toSql(CountDistinctOrder orderBy) {
      switch (orderBy) {
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
        case NO_ORDERING:
          return "";
        default:
          throw new IllegalArgumentException("Illegal enum: " + orderBy);
      }
    }

    private boolean hasValue(String s) {
      return s != null && !s.isEmpty();
    }
  }

  boolean isPlatformDistinctOn() {
    return dbPlatform.isPlatform(Platform.POSTGRES) || dbPlatform.isPlatform(Platform.YUGABYTE) || dbPlatform.isPlatform(Platform.COCKROACH);
  }

  boolean isPlatformDistinctNoLobs() {
    return dbPlatform.isPlatform(Platform.DB2); // CHECKME: Also oracle?
  }

  /**
   * Return the 'for update' FROM hint (sql server).
   */
  String fromForUpdate(SpiQuery<?> query) {
    Query.LockWait mode = query.getForUpdateLockWait();
    if (mode == null) {
      return null;
    } else {
      return dbPlatform.fromForUpdate(mode);
    }
  }
}
