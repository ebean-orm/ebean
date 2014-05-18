package com.avaje.ebeaninternal.server.query;

import java.util.Iterator;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSql.ColumnMapping;
import com.avaje.ebean.RawSql.ColumnMapping.Column;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.SqlLimitRequest;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebean.config.dbplatform.SqlLimiter;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryLimitRequest;

/**
 * Generates the SQL SELECT statements taking into account the physical
 * deployment properties.
 */
public class CQueryBuilder implements Constants {

  private final String tableAliasPlaceHolder;
  private final String columnAliasPrefix;

  private final SqlLimiter sqlLimiter;

  private final RawSqlSelectClauseBuilder sqlSelectBuilder;
  private final CQueryBuilderRawSql rawSqlHandler;

  private final Binder binder;

  private final boolean selectCountWithAlias;

  private DatabasePlatform dbPlatform;

  /**
   * Create the SqlGenSelect.
   */
  public CQueryBuilder(DatabasePlatform dbPlatform, Binder binder) {

    this.binder = binder;
    this.tableAliasPlaceHolder = GlobalProperties.get("ebean.tableAliasPlaceHolder", "${ta}");
    this.columnAliasPrefix = GlobalProperties.get("ebean.columnAliasPrefix", "c");
    this.sqlSelectBuilder = new RawSqlSelectClauseBuilder(dbPlatform, binder);

    this.sqlLimiter = dbPlatform.getSqlLimiter();
    this.rawSqlHandler = new CQueryBuilderRawSql(sqlLimiter, dbPlatform);

    this.selectCountWithAlias = dbPlatform.isSelectCountWithAlias();

    this.dbPlatform = dbPlatform;
  }

  /**
   * split the order by claus on the field delimiter and prefix each field with
   * the relation name
   */
  public static String prefixOrderByFields(String name, String orderBy) {
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
   * Build the row count query.
   */
  public <T> CQueryFetchIds buildFetchIdsQuery(OrmQueryRequest<T> request) {

    SpiQuery<T> query = request.getQuery();

    query.setSelectId();

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      // skip building the SqlTree and Sql string
      predicates.prepare(false);
      String sql = queryPlan.getSql();
      return new CQueryFetchIds(request, predicates, sql);
    }

    // use RawSql or generated Sql
    predicates.prepare(true);

    SqlTree sqlTree = createSqlTree(request, predicates);
    SqlLimitResponse s = buildSql(null, request, predicates, sqlTree);
    String sql = s.getSql();

    // cache the query plan
    queryPlan = new CQueryPlan(request, sql, sqlTree, false, s.isIncludesRowNumberColumn(), predicates.getLogWhereSql());

    request.putQueryPlan(queryPlan);
    return new CQueryFetchIds(request, predicates, sql);
  }

  /**
   * Build the row count query.
   */
  public <T> CQueryRowCount buildRowCountQuery(OrmQueryRequest<T> request) {

    SpiQuery<T> query = request.getQuery();

    // always set the order by to null for row count query
    query.setOrder(null);

    ManyWhereJoins manyWhereJoins = query.getManyWhereJoins();
    
    boolean hasMany = manyWhereJoins.isHasMany();
    if (manyWhereJoins.isSelectId()) {
      // just select the id property
      query.setSelectId();
    } else {
      // select the id and the required formula properties
      query.select(manyWhereJoins.getFormulaProperties());
    }

    String sqlSelect = "select count(*)";
    if (hasMany) {
      // need to count distinct id's ...
      query.setDistinct(true);
      sqlSelect = null;
    }

    CQueryPredicates predicates = new CQueryPredicates(binder, request);
    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      // skip building the SqlTree and Sql string
      predicates.prepare(false);
      String sql = queryPlan.getSql();
      return new CQueryRowCount(request, predicates, sql);
    }

    predicates.prepare(true);

    SqlTree sqlTree = createSqlTree(request, predicates);
    SqlLimitResponse s = buildSql(sqlSelect, request, predicates, sqlTree);
    String sql = s.getSql();
    if (hasMany) {
      sql = "select count(*) from ( " + sql + ")";
      if (selectCountWithAlias) {
        sql += " as c";
      }
    }

    // cache the query plan
    queryPlan = new CQueryPlan(request, sql, sqlTree, false, s.isIncludesRowNumberColumn(), predicates.getLogWhereSql());
    request.putQueryPlan(queryPlan);

    return new CQueryRowCount(request, predicates, sql);
  }

  /**
   * Return the SQL Select statement as a String. Converts logical property
   * names to physical deployment column names.
   */
  public <T> CQuery<T> buildQuery(OrmQueryRequest<T> request) {

    if (request.isSqlSelect()) {
      return sqlSelectBuilder.build(request);
    }

    CQueryPredicates predicates = new CQueryPredicates(binder, request);

    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      // Reuse the query plan so skip generating SqlTree and SQL.
      // We do prepare and bind the new parameters
      predicates.prepare(false);
      return new CQuery<T>(request, predicates, queryPlan);
    }

    // RawSql or Generated Sql query

    // Prepare the where, having and order by clauses.
    // This also parses them from logical property names to
    // database columns and determines 'includes'.

    // We need to check these 'includes' for extra joins
    // that are not included via select
    predicates.prepare(true);

    // Build the tree structure that represents the query.
    SqlTree sqlTree = createSqlTree(request, predicates);
    SqlLimitResponse res = buildSql(null, request, predicates, sqlTree);

    boolean rawSql = request.isRawSql();
    if (rawSql) {
      queryPlan = new CQueryPlanRawSql(request, res, sqlTree, predicates.getLogWhereSql());

    } else {
      queryPlan = new CQueryPlan(request, res, sqlTree, rawSql, predicates.getLogWhereSql());
    }

    // cache the query plan because we can reuse it and also
    // gather query performance statistics based on it.
    request.putQueryPlan(queryPlan);

    return new CQuery<T>(request, predicates, queryPlan);
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

    if (request.isRawSql()) {
      return createRawSqlSqlTree(request, predicates);
    }

    return new SqlTreeBuilder(tableAliasPlaceHolder, columnAliasPrefix, request, predicates).build();
  }

  private SqlTree createRawSqlSqlTree(OrmQueryRequest<?> request, CQueryPredicates predicates) {

    BeanDescriptor<?> descriptor = request.getBeanDescriptor();
    ColumnMapping columnMapping = request.getQuery().getRawSql().getColumnMapping();

    PathProperties pathProps = new PathProperties();

    // convert list of columns into (tree like) PathProperties
    Iterator<Column> it = columnMapping.getColumns();
    while (it.hasNext()) {
      RawSql.ColumnMapping.Column column = it.next();
      String propertyName = column.getPropertyName();
      if (!RawSqlBuilder.IGNORE_COLUMN.equals(propertyName)) {

        ElPropertyValue el = descriptor.getElGetValue(propertyName);
        if (el == null) {
            throw new PersistenceException("Property [" + propertyName + "] not found on " + descriptor.getFullName());
        } else {
          BeanProperty beanProperty = el.getBeanProperty();
          if (beanProperty.isId() || beanProperty.isDiscriminator()) {
            // For @Id properties we chop off the last part of the path
            propertyName = SplitName.parent(propertyName);
          } else if (beanProperty instanceof BeanPropertyAssocOne<?>) {
            String msg = "Column [" + column.getDbColumn() + "] mapped to complex Property[" + propertyName + "]";
            msg += ". It should be mapped to a simple property (proably the Id property). ";
            throw new PersistenceException(msg);
          }
          if (propertyName != null) {
            String[] pathProp = SplitName.split(propertyName);
            pathProps.addToPath(pathProp[0], pathProp[1]);
          }
        }
      }
    }

    OrmQueryDetail detail = new OrmQueryDetail();

    // transfer PathProperties into OrmQueryDetail
    Iterator<String> pathIt = pathProps.getPaths().iterator();
    while (pathIt.hasNext()) {
      String path = pathIt.next();
      Set<String> props = pathProps.get(path);
      detail.getChunk(path, true).setDefaultProperties(null, props);
    }

    // build SqlTree based on OrmQueryDetail of the RawSql
    return new SqlTreeBuilder(request, predicates, detail).build();
  }

  private SqlLimitResponse buildSql(String selectClause, OrmQueryRequest<?> request, CQueryPredicates predicates, SqlTree select) {

    SpiQuery<?> query = request.getQuery();

    RawSql rawSql = query.getRawSql();
    if (rawSql != null) {
      return rawSqlHandler.buildSql(request, predicates, rawSql.getSql());
    }

    BeanPropertyAssocMany<?> manyProp = select.getManyProperty();

    boolean useSqlLimiter = false;

    StringBuilder sb = new StringBuilder(500);

    String dbOrderBy = predicates.getDbOrderBy();
    
    if (selectClause != null) {
      sb.append(selectClause);

    } else {

      useSqlLimiter = (query.hasMaxRowsOrFirstRow() && manyProp == null);

      if (!useSqlLimiter) {
        sb.append("select ");
        if (query.isDistinct()) {
          sb.append("distinct ");
        }
      }

      sb.append(select.getSelectSql());
      if (query.isDistinct() && dbOrderBy != null) {
        // add the orderby columns to the select clause (due to distinct)
        sb.append(", ").append(convertDbOrderByForSelect(dbOrderBy));
      }
    }

    sb.append(" from ");

    // build the from clause potentially with joins
    // required only for the predicates
    sb.append(select.getFromSql());

    String inheritanceWhere = select.getInheritanceWhereSql();

    boolean hasWhere = false;
    if (inheritanceWhere.length() > 0) {
      sb.append(" where");
      sb.append(inheritanceWhere);
      hasWhere = true;
    }

    if (request.isFindById() || query.getId() != null) {
      if (hasWhere) {
        sb.append(" and ");
      } else {
        sb.append(" where ");
      }

      BeanDescriptor<?> desc = request.getBeanDescriptor();
      String idSql = desc.getIdBinderIdSql();
      if (idSql.isEmpty()) {
        throw new IllegalStateException("Executing FindById query on entity bean " + desc.getName()
            + " that doesn't have an @Id property??");
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
        sb.append(" where ");
      } else {
        sb.append("and ");
      }
      sb.append(dbFilterMany);
    }

    
    if (dbOrderBy != null) {
      sb.append(" order by ").append(dbOrderBy);
    }

    if (useSqlLimiter) {
      // use LIMIT/OFFSET, ROW_NUMBER() or rownum type SQL query limitation
      SqlLimitRequest r = new OrmQueryLimitRequest(sb.toString(), dbOrderBy, query, dbPlatform);
      return sqlLimiter.limit(r);

    } else {
      return new SqlLimitResponse(dbPlatform.completeSql(sb.toString(), query), false);
    }

  }

  /**
   * Convert the dbOrderBy clause to be safe for adding to select. This is done when 'distinct' is
   * used.
   */
  private String convertDbOrderByForSelect(String dbOrderBy) {
    // just remove the ASC and DESC keywords
    return dbOrderBy.replaceAll("(?i)\\b asc\\b|\\b desc\\b", "");
  }
  
  private boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

}
